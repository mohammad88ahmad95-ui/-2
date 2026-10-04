package com.hasabati.app.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.PaymentMethod
import com.hasabati.app.data.db.entities.Transaction
import com.hasabati.app.data.repository.HasabatiRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpensesViewModel(private val repository: HasabatiRepository) : ViewModel() {

    val expenses: StateFlow<List<Transaction>> = repository.observeExpenses()
        .map { it.sortedByDescending { t -> t.createdAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalUsd: StateFlow<Double> = expenses.map { list -> list.sumOf { it.usdEquivalent } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addExpense(amount: Double, currency: Currency, method: PaymentMethod, rate: Double?, category: String, note: String) {
        viewModelScope.launch { repository.addExpense(amount, currency, method, rate, category, note) }
    }

    fun addPersonalWithdrawal(amount: Double, currency: Currency, method: PaymentMethod, rate: Double?, note: String) {
        viewModelScope.launch { repository.addPersonalWithdrawal(amount, currency, method, rate, note) }
    }
}
