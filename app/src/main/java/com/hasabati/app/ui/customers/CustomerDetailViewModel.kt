package com.hasabati.app.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasabati.app.data.db.entities.*
import com.hasabati.app.data.repository.HasabatiRepository
import com.hasabati.app.domain.FinanceEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomerDetailViewModel(private val repository: HasabatiRepository, private val customerId: Long) : ViewModel() {

    sealed class StatementLine {
        abstract val date: Long
        data class OrderLine(val order: Order, override val date: Long) : StatementLine()
        data class PaymentLine(val tx: Transaction, override val date: Long) : StatementLine()
    }

    data class UiState(
        val customer: Customer? = null,
        val orders: List<Order> = emptyList(),
        val transactions: List<Transaction> = emptyList(),
        val statement: List<StatementLine> = emptyList(),
        val remainingUsd: Double = 0.0
    )

    val uiState: StateFlow<UiState> = combine(
        repository.observeCustomer(customerId),
        repository.observeOrdersForCustomer(customerId),
        repository.observeTransactionsForCustomer(customerId)
    ) { customer, orders, txs ->
        val activeOrders = orders.filter { it.status != OrderStatus.CANCELLED }
        val statement = (activeOrders.map { StatementLine.OrderLine(it, it.createdAt) } +
                txs.filter { it.type == TransactionType.CUSTOMER_PAYMENT || it.type == TransactionType.REFUND }
                    .map { StatementLine.PaymentLine(it, it.createdAt) })
            .sortedBy { it.date }
        val remaining = FinanceEngine.customerRemainingUsd(orders, txs, customerId)
        UiState(customer, orders, txs, statement, remaining)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun collectPayment(orderId: Long?, amount: Double, currency: Currency, method: PaymentMethod, exchangeRate: Double?) {
        viewModelScope.launch {
            repository.recordCustomerPayment(customerId, orderId, amount, currency, method, exchangeRate, "تحصيل دفعة")
        }
    }
}
