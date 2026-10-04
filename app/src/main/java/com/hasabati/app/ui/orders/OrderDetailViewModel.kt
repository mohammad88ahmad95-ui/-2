package com.hasabati.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasabati.app.data.db.entities.*
import com.hasabati.app.data.repository.HasabatiRepository
import com.hasabati.app.domain.FinanceEngine
import com.hasabati.app.ui.common.next
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrderDetailViewModel(private val repository: HasabatiRepository, private val orderId: Long) : ViewModel() {

    data class UiState(
        val order: Order? = null,
        val customer: Customer? = null,
        val items: List<OrderItem> = emptyList(),
        val transactions: List<Transaction> = emptyList(),
        val paidUsd: Double = 0.0,
        val remainingUsd: Double = 0.0,
        val actionError: String? = null
    )

    val uiState: StateFlow<UiState> = combine(
        repository.observeOrder(orderId),
        repository.observeOrderItems(orderId),
        repository.observeTransactionsForOrder(orderId)
    ) { order, items, txs ->
        val customer = order?.let { repository.getCustomer(it.customerId) }
        val paid = txs.sumOf {
            when (it.type) {
                TransactionType.CUSTOMER_PAYMENT -> it.usdEquivalent
                TransactionType.REFUND -> -it.usdEquivalent
                else -> 0.0
            }
        }
        val remaining = ((order?.saleTotalUsd ?: 0.0) - paid).coerceAtLeast(0.0)
        UiState(order, customer, items, txs, paid, remaining)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun advanceStatus() {
        val order = uiState.value.order ?: return
        val next = order.status.next() ?: return
        viewModelScope.launch {
            try {
                if (next == OrderStatus.DELIVERED) repository.markDelivered(orderId)
                else repository.updateOrderStatus(orderId, next)
            } catch (e: Exception) { /* ignore */ }
        }
    }

    fun confirmArrival(actualUnitCosts: Map<Long, Double>) {
        viewModelScope.launch { repository.confirmArrival(orderId, actualUnitCosts) }
    }

    fun collectPayment(amount: Double, currency: Currency, method: PaymentMethod, exchangeRate: Double?) {
        val order = uiState.value.order ?: return
        viewModelScope.launch {
            try {
                repository.recordCustomerPayment(order.customerId, orderId, amount, currency, method, exchangeRate, "تحصيل دفعة")
            } catch (e: Exception) {
                // expose via state if needed
            }
        }
    }

    fun cancelOrder(refundDeposit: Boolean?) {
        viewModelScope.launch { repository.cancelOrder(orderId, refundDeposit) }
    }
}
