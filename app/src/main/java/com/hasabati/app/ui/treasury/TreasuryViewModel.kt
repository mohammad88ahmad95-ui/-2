package com.hasabati.app.ui.treasury

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.Transaction
import com.hasabati.app.data.repository.HasabatiRepository
import com.hasabati.app.domain.FinanceEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TreasuryViewModel(private val repository: HasabatiRepository) : ViewModel() {
    data class UiState(
        val snapshot: FinanceEngine.TreasurySnapshot = FinanceEngine.TreasurySnapshot(),
        val transactions: List<Transaction> = emptyList()
    )

    val uiState: StateFlow<UiState> = repository.observeTransactions().map { txs ->
        UiState(FinanceEngine.treasury(txs), txs.sortedByDescending { it.createdAt })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun transferToCash(amount: Double, currency: Currency) {
        viewModelScope.launch { repository.transferShamCashToCash(amount, currency) }
    }

    fun convertCurrency(
        amountOut: Double, currencyOut: Currency, methodOut: com.hasabati.app.data.db.entities.PaymentMethod,
        amountIn: Double, currencyIn: Currency, methodIn: com.hasabati.app.data.db.entities.PaymentMethod
    ) {
        viewModelScope.launch {
            repository.convertCurrency(amountOut, currencyOut, methodOut, amountIn, currencyIn, methodIn)
        }
    }
}
