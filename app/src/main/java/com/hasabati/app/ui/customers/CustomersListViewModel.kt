package com.hasabati.app.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasabati.app.data.db.entities.Customer
import com.hasabati.app.data.db.entities.OrderStatus
import com.hasabati.app.data.repository.HasabatiRepository
import com.hasabati.app.domain.FinanceEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomersListViewModel(private val repository: HasabatiRepository) : ViewModel() {

    data class CustomerRow(
        val customer: Customer,
        val orderCount: Int,
        val totalSalesUsd: Double,
        val paidUsd: Double,
        val remainingUsd: Double
    )

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query
    fun setQuery(q: String) { _query.value = q }

    val rows: StateFlow<List<CustomerRow>> = combine(
        repository.observeCustomers(),
        repository.observeOrders(),
        repository.observeTransactions(),
        _query
    ) { customers, orders, txs, q ->
        customers
            .filter { q.isBlank() || it.name.contains(q, true) || it.phone.contains(q, true) }
            .map { c ->
                val custOrders = orders.filter { it.customerId == c.id && it.status != OrderStatus.CANCELLED }
                val sales = custOrders.sumOf { it.saleTotalUsd }
                val paid = FinanceEngine.customerNetPaidUsd(txs, c.id)
                val remaining = (sales - paid).coerceAtLeast(0.0)
                CustomerRow(c, custOrders.size, sales, paid, remaining)
            }
            .sortedByDescending { it.remainingUsd }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomer(name: String, phone: String, notes: String, onDone: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.addCustomer(name, phone, notes)
            onDone(id)
        }
    }
}
