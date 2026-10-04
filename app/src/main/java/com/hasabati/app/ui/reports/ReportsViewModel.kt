package com.hasabati.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasabati.app.data.db.entities.*
import com.hasabati.app.data.repository.HasabatiRepository
import com.hasabati.app.domain.FinanceEngine
import kotlinx.coroutines.flow.*
import java.util.Calendar

class ReportsViewModel(repository: HasabatiRepository) : ViewModel() {

    enum class Period(val label: String) { TODAY("اليوم"), WEEK("هذا الأسبوع"), MONTH("هذا الشهر"), ALL("الكل") }

    private val _period = MutableStateFlow(Period.MONTH)
    val period: StateFlow<Period> = _period
    fun setPeriod(p: Period) { _period.value = p }

    data class UiState(
        val salesUsd: Double = 0.0,
        val costUsd: Double = 0.0,
        val profitUsd: Double = 0.0,
        val expensesUsd: Double = 0.0,
        val withdrawalsUsd: Double = 0.0,
        val ordersCount: Int = 0,
        val customersCount: Int = 0,
        val agentPaidUsd: Double = 0.0
    )

    private fun periodStart(p: Period): Long {
        val cal = Calendar.getInstance()
        return when (p) {
            Period.TODAY -> { cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.timeInMillis }
            Period.WEEK -> { cal.add(Calendar.DAY_OF_YEAR, -7); cal.timeInMillis }
            Period.MONTH -> { cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.timeInMillis }
            Period.ALL -> 0L
        }
    }

    val uiState: StateFlow<UiState> = combine(
        repository.observeOrders(),
        repository.observeTransactions(),
        _period
    ) { orders, txs, p ->
        val start = periodStart(p)
        val periodOrders = orders.filter { it.createdAt >= start && it.status != OrderStatus.CANCELLED }
        val periodTxs = txs.filter { it.createdAt >= start }

        val sales = periodOrders.sumOf { it.saleTotalUsd }
        val cost = periodOrders.sumOf { it.actualCostUsd ?: it.expectedCostUsd }
        val expenses = periodTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.usdEquivalent }
        val withdrawals = periodTxs.filter { it.type == TransactionType.PERSONAL_WITHDRAWAL }.sumOf { it.usdEquivalent }
        val agentPaid = periodTxs.filter { it.type == TransactionType.AGENT_PAYMENT }.sumOf { it.usdEquivalent }

        UiState(
            salesUsd = sales,
            costUsd = cost,
            profitUsd = sales - cost,
            expensesUsd = expenses,
            withdrawalsUsd = withdrawals,
            ordersCount = periodOrders.size,
            customersCount = periodOrders.map { it.customerId }.distinct().size,
            agentPaidUsd = agentPaid
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())
}
