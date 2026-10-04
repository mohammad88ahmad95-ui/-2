package com.hasabati.app.data.repository

import com.hasabati.app.data.db.AppDatabase
import com.hasabati.app.data.db.entities.*
import com.hasabati.app.domain.FinanceEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * نقطة الدخول الوحيدة للبيانات والمنطق المالي. كل الشاشات تتعامل مع هذا الصف فقط،
 * ولا تصل إلى الـ DAO مباشرة، لضمان أن كل قاعدة محاسبية (القسم 36) تُطبَّق في مكان واحد.
 */
class HasabatiRepository(private val db: AppDatabase) {

    private val customerDao = db.customerDao()
    private val orderDao = db.orderDao()
    private val txDao = db.transactionDao()

    // ---------------------------------------------------------------------
    // قراءات مباشرة
    // ---------------------------------------------------------------------
    fun observeCustomers(): Flow<List<Customer>> = customerDao.observeAll()
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.search(query)
    fun observeCustomer(id: Long): Flow<Customer?> = customerDao.observeById(id)
    suspend fun getCustomer(id: Long): Customer? = customerDao.getById(id)

    fun observeOrders(): Flow<List<Order>> = orderDao.observeAll()
    fun observeOrdersForCustomer(customerId: Long): Flow<List<Order>> = orderDao.observeForCustomer(customerId)
    fun observeOrder(id: Long): Flow<Order?> = orderDao.observeById(id)
    suspend fun getOrder(id: Long): Order? = orderDao.getById(id)
    fun observeOrderItems(orderId: Long): Flow<List<OrderItem>> = orderDao.observeItems(orderId)
    fun observeArrivedNotDelivered(): Flow<List<Order>> = orderDao.observeArrivedNotDelivered()
    fun observeShipping(): Flow<List<Order>> = orderDao.observeShipping()

    fun observeTransactions(): Flow<List<Transaction>> = txDao.observeAll()
    fun observeRecentTransactions(limit: Int = 15): Flow<List<Transaction>> = txDao.observeRecent(limit)
    fun observeTransactionsForCustomer(customerId: Long): Flow<List<Transaction>> = txDao.observeForCustomer(customerId)
    fun observeTransactionsForOrder(orderId: Long): Flow<List<Transaction>> = txDao.observeForOrder(orderId)
    fun observeAgentTransactions(): Flow<List<Transaction>> = txDao.observeByType(TransactionType.AGENT_PAYMENT)
    fun observeExpenses(): Flow<List<Transaction>> = txDao.observeByType(TransactionType.EXPENSE)

    // ---------------------------------------------------------------------
    // اللقطة المالية الشاملة (تُستخدم في لوحة التحكم وشاشة "أين أموالي؟")
    // ---------------------------------------------------------------------
    data class FinancialSnapshot(
        val treasury: FinanceEngine.TreasurySnapshot,
        val customerReceivablesUsd: Double,
        val agentPayableUsd: Double,
        val capital: FinanceEngine.CapitalSnapshot,
        val realizedProfitUsd: Double,
        val expectedProfitUsd: Double,
        val totalExpensesUsd: Double,
        val arrivedNotDeliveredCount: Int,
        val shippingCount: Int,
        val customersWithBalanceCount: Int
    )

    fun observeFinancialSnapshot(): Flow<FinancialSnapshot> =
        combine(orderDao.observeAll(), txDao.observeAll()) { orders, txs ->
            val byCustomer = orders.filter { it.status != OrderStatus.CANCELLED }.groupBy { it.customerId }
            val customersWithBalance = byCustomer.keys.count {
                FinanceEngine.customerRemainingUsd(orders, txs, it) > 0.009
            }
            FinancialSnapshot(
                treasury = FinanceEngine.treasury(txs),
                customerReceivablesUsd = FinanceEngine.totalCustomerReceivablesUsd(orders, txs),
                agentPayableUsd = FinanceEngine.agentPayableUsd(orders, txs),
                capital = FinanceEngine.capital(txs),
                realizedProfitUsd = FinanceEngine.realizedProfitUsd(orders),
                expectedProfitUsd = FinanceEngine.expectedProfitUsd(orders),
                totalExpensesUsd = FinanceEngine.totalExpensesUsd(txs),
                arrivedNotDeliveredCount = orders.count { it.status == OrderStatus.ARRIVED || it.status == OrderStatus.READY_FOR_DELIVERY },
                shippingCount = orders.count { it.status == OrderStatus.SHIPPING },
                customersWithBalanceCount = customersWithBalance
            )
        }

    fun customerRemainingUsdFlow(customerId: Long): Flow<Double> =
        combine(orderDao.observeForCustomer(customerId), txDao.observeForCustomer(customerId)) { orders, txs ->
            FinanceEngine.customerRemainingUsd(orders, txs, customerId)
        }

    // ---------------------------------------------------------------------
    // العملاء
    // ---------------------------------------------------------------------
    suspend fun addCustomer(name: String, phone: String, notes: String): Long =
        customerDao.insert(Customer(name = name, phone = phone, notes = notes))

    suspend fun updateCustomer(customer: Customer) = customerDao.update(customer)

    // ---------------------------------------------------------------------
    // الطلبات
    // ---------------------------------------------------------------------
    data class NewOrderItemInput(
        val productName: String,
        val quantity: Int,
        val expectedUnitCostUsd: Double,
        val unitSalePriceUsd: Double
    )

    /** ينشئ طلباً جديداً بعناصره، ويسجل عربوناً اختيارياً كعملية مالية مستقلة (القسم 9). */
    suspend fun createOrder(
        customerId: Long,
        items: List<NewOrderItemInput>,
        notes: String,
        depositAmount: Double?,
        depositCurrency: Currency?,
        depositMethod: PaymentMethod?,
        depositExchangeRate: Double?
    ): Long {
        require(items.isNotEmpty()) { "لا يمكن حفظ طلب بدون منتجات" }
        items.forEach {
            require(it.productName.isNotBlank()) { "لا يمكن إضافة منتج بدون اسم" }
            require(it.quantity > 0) { "الكمية يجب أن تكون أكبر من صفر" }
            require(it.expectedUnitCostUsd >= 0 && it.unitSalePriceUsd >= 0) { "السعر لا يمكن أن يكون سالباً" }
        }

        val expectedCostTotal = items.sumOf { it.quantity * it.expectedUnitCostUsd }
        val saleTotal = items.sumOf { it.quantity * it.unitSalePriceUsd }

        val nextSeq = orderDao.maxOrderSequence()
        val orderNumber = FinanceEngine.generateNextOrderNumber(nextSeq)

        val orderId = orderDao.insert(
            Order(
                orderNumber = orderNumber,
                customerId = customerId,
                status = OrderStatus.CONFIRMED,
                expectedCostUsd = expectedCostTotal,
                saleTotalUsd = saleTotal,
                notes = notes
            )
        )

        orderDao.insertItems(items.map {
            OrderItem(
                orderId = orderId,
                productName = it.productName,
                quantity = it.quantity,
                expectedUnitCostUsd = it.expectedUnitCostUsd,
                unitSalePriceUsd = it.unitSalePriceUsd
            )
        })

        if (depositAmount != null && depositAmount > 0 && depositCurrency != null && depositMethod != null) {
            recordCustomerPayment(customerId, orderId, depositAmount, depositCurrency, depositMethod, depositExchangeRate, "عربون عند تأكيد الطلب")
        }

        return orderId
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) {
        val order = orderDao.getById(orderId) ?: return
        require(order.status != OrderStatus.CANCELLED) { "لا يمكن تعديل طلب ملغي" }
        orderDao.update(order.copy(status = newStatus))
    }

    /** تأكيد وصول الطلب وتسجيل التكلفة الفعلية لكل منتج (القسم 11) — يُستبدل هنا الرقم المتوقع للوكيلة بالتكلفة الفعلية. */
    suspend fun confirmArrival(orderId: Long, actualUnitCosts: Map<Long, Double>) {
        val order = orderDao.getById(orderId) ?: return
        val items = orderDao.getItems(orderId)
        val updated = items.map { item ->
            val actual = actualUnitCosts[item.id] ?: item.expectedUnitCostUsd
            require(actual >= 0) { "التكلفة الفعلية لا يمكن أن تكون سالبة" }
            item.copy(actualUnitCostUsd = actual)
        }
        orderDao.updateItems(updated)
        val actualTotal = updated.sumOf { it.quantity * (it.actualUnitCostUsd ?: 0.0) }
        orderDao.update(
            order.copy(
                status = OrderStatus.ARRIVED,
                receivedAt = System.currentTimeMillis(),
                actualCostUsd = actualTotal
            )
        )
    }

    suspend fun markDelivered(orderId: Long) {
        val order = orderDao.getById(orderId) ?: return
        orderDao.update(order.copy(status = OrderStatus.DELIVERED, deliveredAt = System.currentTimeMillis()))
    }

    /** إلغاء طلب — مع منطق العربون والاسترداد (القسم 37) */
    suspend fun cancelOrder(orderId: Long, refundDeposit: Boolean?) {
        val order = orderDao.getById(orderId) ?: return
        val paidSoFar = FinanceEngine.customerNetPaidUsd(txDao.getAllOnce(), order.customerId)
        val hasDepositOnThisOrder = txDao.getAllOnce().any {
            it.orderId == orderId && it.type == TransactionType.CUSTOMER_PAYMENT
        }
        orderDao.update(
            order.copy(
                status = OrderStatus.CANCELLED,
                cancelledAt = System.currentTimeMillis(),
                depositRefunded = if (hasDepositOnThisOrder) refundDeposit else null
            )
        )
        if (hasDepositOnThisOrder && refundDeposit == true) {
            val depositTxs = txDao.getAllOnce().filter { it.orderId == orderId && it.type == TransactionType.CUSTOMER_PAYMENT }
            depositTxs.forEach { deposit ->
                txDao.insert(
                    Transaction(
                        type = TransactionType.REFUND,
                        amount = deposit.amount,
                        currency = deposit.currency,
                        usdEquivalent = deposit.usdEquivalent,
                        exchangeRate = deposit.exchangeRate,
                        method = deposit.method,
                        customerId = order.customerId,
                        orderId = orderId,
                        note = "استرداد عربون للطلب ${order.orderNumber}"
                    )
                )
            }
        }
    }

    // ---------------------------------------------------------------------
    // العمليات المالية
    // ---------------------------------------------------------------------

    /**
     * تحصيل دفعة من عميلة (عربون أو تسديد) — القسم 16.
     * إذا حُدِّد [orderId] تُسجَّل الدفعة على هذا الطلب مباشرة (حالة العربون وتحصيل دفعة من داخل الطلب).
     * إذا لم يُحدَّد طلب (دفعة عامة من صفحة العميلة)، تُوزَّع الدفعة تلقائياً على أقدم
     * طلبات العميلة غير المسددة أولاً بأول، حتى تنعكس بدقة على كل طلب على حدة وليس فقط
     * على إجمالي حساب العميلة.
     */
    suspend fun recordCustomerPayment(
        customerId: Long,
        orderId: Long?,
        amount: Double,
        currency: Currency,
        method: PaymentMethod,
        exchangeRate: Double?,
        note: String = ""
    ): Long {
        require(amount > 0) { "المبلغ يجب أن يكون أكبر من صفر" }
        val usdEq = if (currency == Currency.USD) amount else {
            requireNotNull(exchangeRate) { "سعر الصرف مطلوب عند الدفع بالليرة" }
            FinanceEngine.sypToUsd(amount, exchangeRate)
        }

        if (orderId != null) {
            return txDao.insert(
                Transaction(
                    type = TransactionType.CUSTOMER_PAYMENT,
                    amount = amount,
                    currency = currency,
                    usdEquivalent = usdEq,
                    exchangeRate = if (currency != Currency.USD) exchangeRate else null,
                    method = method,
                    customerId = customerId,
                    orderId = orderId,
                    note = note
                )
            )
        }

        // دفعة عامة بدون طلب محدد: توزيع تلقائي على الطلبات المستحقة، الأقدم أولاً
        val orders = orderDao.getForCustomerOnce(customerId).filter { it.status != OrderStatus.CANCELLED }
        val customerTxs = txDao.getForCustomerOnce(customerId)
        var remainingUsdToDistribute = usdEq
        var lastInsertedId = 0L

        for (order in orders) {
            if (remainingUsdToDistribute <= 0.009) break
            val paidForOrder = customerTxs.filter { it.orderId == order.id }.sumOf {
                when (it.type) {
                    TransactionType.CUSTOMER_PAYMENT -> it.usdEquivalent
                    TransactionType.REFUND -> -it.usdEquivalent
                    else -> 0.0
                }
            }
            val orderRemaining = (order.saleTotalUsd - paidForOrder).coerceAtLeast(0.0)
            if (orderRemaining <= 0.009) continue

            val portionUsd = minOf(remainingUsdToDistribute, orderRemaining)
            val portionAmount = if (currency == Currency.USD) portionUsd else portionUsd * (exchangeRate ?: 1.0)

            lastInsertedId = txDao.insert(
                Transaction(
                    type = TransactionType.CUSTOMER_PAYMENT,
                    amount = portionAmount,
                    currency = currency,
                    usdEquivalent = portionUsd,
                    exchangeRate = if (currency != Currency.USD) exchangeRate else null,
                    method = method,
                    customerId = customerId,
                    orderId = order.id,
                    note = if (note.isBlank()) "دفعة موزّعة على الطلب ${order.orderNumber}" else note
                )
            )
            remainingUsdToDistribute -= portionUsd
        }

        // أي مبلغ زائد بعد تغطية كل الطلبات المستحقة يُسجَّل كرصيد عام للعميلة (دفعة زائدة)
        if (remainingUsdToDistribute > 0.009) {
            val portionAmount = if (currency == Currency.USD) remainingUsdToDistribute else remainingUsdToDistribute * (exchangeRate ?: 1.0)
            lastInsertedId = txDao.insert(
                Transaction(
                    type = TransactionType.CUSTOMER_PAYMENT,
                    amount = portionAmount,
                    currency = currency,
                    usdEquivalent = remainingUsdToDistribute,
                    exchangeRate = if (currency != Currency.USD) exchangeRate else null,
                    method = method,
                    customerId = customerId,
                    orderId = null,
                    note = if (note.isBlank()) "دفعة زائدة عن المستحق الحالي" else note
                )
            )
        }

        return lastInsertedId
    }

    /**
     * تحويل عملة إلى عملة أخرى (مثلاً ليرة سورية إلى دولار، أو ريال إلى ليرة) — تُدخل المستخدمة
     * المبلغ الذي "خرج" والمبلغ الذي "دخل" فعلياً بدل إدخال سعر صرف مجرّد، فتُحسب القيمة الحقيقية
     * دائماً من غير التباس باتجاه السعر. تُسجَّل كحركتين مرتبطتين بنفس مبدأ التحويل بين المحافظ.
     */
    suspend fun convertCurrency(
        amountOut: Double, currencyOut: Currency, methodOut: PaymentMethod,
        amountIn: Double, currencyIn: Currency, methodIn: PaymentMethod,
        note: String = ""
    ): Long {
        require(amountOut > 0 && amountIn > 0) { "المبلغين يجب أن يكونا أكبر من صفر" }
        txDao.insert(
            Transaction(
                type = TransactionType.TRANSFER_OUT,
                amount = amountOut, currency = currencyOut, usdEquivalent = amountOut,
                method = methodOut,
                note = if (note.isBlank()) "تحويل عملة" else note
            )
        )
        return txDao.insert(
            Transaction(
                type = TransactionType.TRANSFER_IN,
                amount = amountIn, currency = currencyIn, usdEquivalent = amountIn,
                method = methodIn,
                note = if (note.isBlank()) "تحويل عملة" else note
            )
        )
    }

    /**
     * تحويل مبلغ من رصيد Sham Cash إلى نقد (Cash) بنفس العملة — عملية داخلية بين
     * "جيوب" الخزينة، لا تُضيف ولا تُنقص أي مال حقيقي، فقط تُسجَّل كحركتين مرتبطتين
     * (سحب من Sham Cash + إيداع في النقد) حتى يبقى سجل العمليات دقيقاً وقابلاً للمراجعة.
     */
    suspend fun transferShamCashToCash(amount: Double, currency: Currency, note: String = ""): Long {
        require(amount > 0) { "المبلغ يجب أن يكون أكبر من صفر" }
        val usdEq = if (currency == Currency.USD) amount else amount // للعملة SYP نُبقي القيمة كما هي كمرجع فقط؛ لا تدخل في حسابات الديون
        txDao.insert(
            Transaction(
                type = TransactionType.TRANSFER_OUT,
                amount = amount,
                currency = currency,
                usdEquivalent = usdEq,
                method = PaymentMethod.SHAM_CASH,
                note = if (note.isBlank()) "تحويل إلى نقد" else note
            )
        )
        return txDao.insert(
            Transaction(
                type = TransactionType.TRANSFER_IN,
                amount = amount,
                currency = currency,
                usdEquivalent = usdEq,
                method = PaymentMethod.CASH,
                note = if (note.isBlank()) "تحويل من Sham Cash" else note
            )
        )
    }

    /** دفع للوكيلة — القسم 13 */
    suspend fun payAgent(amount: Double, currency: Currency, method: PaymentMethod, exchangeRate: Double?, note: String): Long {
        require(amount > 0) { "المبلغ يجب أن يكون أكبر من صفر" }
        val usdEq = if (currency == Currency.USD) amount else {
            requireNotNull(exchangeRate) { "سعر الصرف مطلوب عند الدفع بالليرة" }
            FinanceEngine.sypToUsd(amount, exchangeRate)
        }
        return txDao.insert(
            Transaction(
                type = TransactionType.AGENT_PAYMENT,
                amount = amount, currency = currency, usdEquivalent = usdEq,
                exchangeRate = if (currency != Currency.USD) exchangeRate else null,
                method = method, note = note
            )
        )
    }

    /** مصروف تجاري — القسم 19 */
    suspend fun addExpense(amount: Double, currency: Currency, method: PaymentMethod, exchangeRate: Double?, category: String, note: String): Long {
        require(amount > 0) { "المبلغ يجب أن يكون أكبر من صفر" }
        val usdEq = if (currency == Currency.USD) amount else {
            requireNotNull(exchangeRate) { "سعر الصرف مطلوب عند الدفع بالليرة" }
            FinanceEngine.sypToUsd(amount, exchangeRate)
        }
        return txDao.insert(
            Transaction(
                type = TransactionType.EXPENSE,
                amount = amount, currency = currency, usdEquivalent = usdEq,
                exchangeRate = if (currency != Currency.USD) exchangeRate else null,
                method = method, expenseCategory = category, note = note
            )
        )
    }

    /** سحب شخصي — منفصل تماماً عن المصاريف التجارية (القسم 20) */
    suspend fun addPersonalWithdrawal(amount: Double, currency: Currency, method: PaymentMethod, exchangeRate: Double?, note: String): Long {
        require(amount > 0) { "المبلغ يجب أن يكون أكبر من صفر" }
        val usdEq = if (currency == Currency.USD) amount else {
            requireNotNull(exchangeRate) { "سعر الصرف مطلوب عند الدفع بالليرة" }
            FinanceEngine.sypToUsd(amount, exchangeRate)
        }
        return txDao.insert(
            Transaction(
                type = TransactionType.PERSONAL_WITHDRAWAL,
                amount = amount, currency = currency, usdEquivalent = usdEq,
                exchangeRate = if (currency != Currency.USD) exchangeRate else null,
                method = method, note = note
            )
        )
    }

    /** رأس مال ابتدائي أو إضافة — القسم 21 */
    suspend fun addCapital(isInitial: Boolean, amount: Double, currency: Currency, method: PaymentMethod, exchangeRate: Double?, note: String): Long {
        require(amount > 0) { "المبلغ يجب أن يكون أكبر من صفر" }
        val usdEq = if (currency == Currency.USD) amount else {
            requireNotNull(exchangeRate) { "سعر الصرف مطلوب عند الدفع بالليرة" }
            FinanceEngine.sypToUsd(amount, exchangeRate)
        }
        return txDao.insert(
            Transaction(
                type = if (isInitial) TransactionType.CAPITAL_INITIAL else TransactionType.CAPITAL_ADDITION,
                amount = amount, currency = currency, usdEquivalent = usdEq,
                exchangeRate = if (currency != Currency.USD) exchangeRate else null,
                method = method, note = note
            )
        )
    }

    /**
     * تصحيح/إلغاء عملية سابقة عبر إنشاء عملية عكسية جديدة بدل الحذف (القاعدة 15 و23:
     * لا تُحذف العمليات المالية نهائياً).
     */
    suspend fun reverseTransaction(transactionId: Long, note: String) {
        val original = txDao.getById(transactionId) ?: return
        if (original.reversedByTransactionId != null) return // مُلغاة مسبقاً
        val reversalId = txDao.insert(
            original.copy(
                id = 0,
                amount = -original.amount,
                usdEquivalent = -original.usdEquivalent,
                note = "تصحيح: $note",
                createdAt = System.currentTimeMillis(),
                reversedByTransactionId = null,
                isReversal = true
            )
        )
        txDao.update(original.copy(reversedByTransactionId = reversalId))
    }
}
