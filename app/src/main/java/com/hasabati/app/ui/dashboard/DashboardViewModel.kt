package com.hasabati.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasabati.app.data.db.entities.Transaction
import com.hasabati.app.data.repository.HasabatiRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(repository: HasabatiRepository) : ViewModel() {

    data class UiState(
        val snapshot: HasabatiRepository.FinancialSnapshot? = null,
        val recentTransactions: List<Transaction> = emptyList()
    )

    val uiState: StateFlow<UiState> = combine(
        repository.observeFinancialSnapshot(),
        repository.observeRecentTransactions(8)
    ) { snapshot, recent ->
        UiState(snapshot, recent)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())
}
