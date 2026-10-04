package com.hasabati.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasabati.app.data.db.entities.Customer
import com.hasabati.app.data.db.entities.Order
import com.hasabati.app.data.db.entities.OrderStatus
import com.hasabati.app.data.db.entities.Transaction
import com.hasabati.app.data.repository.HasabatiRepository
import com.hasabati.app.domain.FinanceEngine
import kotlinx.coroutines.flow.*

class OrdersListViewModel(private val repository: HasabatiRepository) : ViewModel() {

    enum class Filter(val label: String) {
        ALL("الكل"), CONFIRMED("مؤكد"), SHIPPING("قيد الشحن"), ARRIVED("وصل"),
        READY("جاهز للتسليم"), DELIVERED("تم التسليم"), CANCELLED("ملغي"),
        HAS_BALANCE("عليه مبلغ"), FULLY_PAID("مدفوع بالكامل")
    }

    private val _filter = MutableStateFlow(Filter.ALL)
    val filter: StateFlow<Filter> = _filter
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    data class OrderRow(
        val order: Order,
        val customerName: String,
        val paidUsd: Double,
        val remainingUsd: Double,
        val profitUsd: Double
    )

    val rows: StateFlow<List<OrderRow>> = combine(
        repository.observeOrders(),
        repository.observeCustomers(),
        repository.observeTransactions(),
        _filter,
        _query
    ) { orders, customers, txs, filterVal, queryVal ->
        val customerMap = customers.associateBy { it.id }
        var list = orders.map { o ->
            val paidForOrder = txs.filter { it.orderId == o.id }
                .sumOf {
                    when (it.type) {
                        com.hasabati.app.data.db.entities.TransactionType.CUSTOMER_PAYMENT -> it.usdEquivalent
                        com.hasabati.app.data.db.entities.TransactionType.REFUND -> -it.usdEquivalent
                        else -> 0.0
                    }
                }
            val remaining = (o.saleTotalUsd - paidForOrder).coerceAtLeast(0.0)
            val profit = if (o.actualCostUsd != null) o.saleTotalUsd - o.actualCostUsd else o.saleTotalUsd - o.expectedCostUsd
            OrderRow(o, customerMap[o.customerId]?.name ?: "—", paidForOrder, remaining, profit)
        }

        list = when (filterVal) {
            Filter.ALL -> list
            Filter.CONFIRMED -> list.filter { it.order.status == OrderStatus.CONFIRMED }
            Filter.SHIPPING -> list.filter { it.order.status == OrderStatus.SHIPPING }
            Filter.ARRIVED -> list.filter { it.order.status == OrderStatus.ARRIVED }
            Filter.READY -> list.filter { it.order.status == OrderStatus.READY_FOR_DELIVERY }
            Filter.DELIVERED -> list.filter { it.order.status == OrderStatus.DELIVERED }
            Filter.CANCELLED -> list.filter { it.order.status == OrderStatus.CANCELLED }
            Filter.HAS_BALANCE -> list.filter { it.remainingUsd > 0.009 && it.order.status != OrderStatus.CANCELLED }
            Filter.FULLY_PAID -> list.filter { it.remainingUsd <= 0.009 && it.order.status != OrderStatus.CANCELLED }
        }

        if (queryVal.isNotBlank()) {
            list = list.filter {
                it.order.orderNumber.contains(queryVal, true) || it.customerName.contains(queryVal, true)
            }
        }

        list.sortedByDescending { it.order.createdAt }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(f: Filter) { _filter.value = f }
    fun setQuery(q: String) { _query.value = q }
}
