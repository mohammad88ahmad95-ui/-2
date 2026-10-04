package com.hasabati.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasabati.app.data.db.AppDatabase
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.PaymentMethod
import com.hasabati.app.data.repository.BackupManager
import com.hasabati.app.data.repository.HasabatiRepository
import com.hasabati.app.domain.FinanceEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val repository: HasabatiRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    data class UiState(
        val capital: FinanceEngine.CapitalSnapshot = FinanceEngine.CapitalSnapshot(0.0, 0.0, 0.0),
        val lastBackupPath: String? = null,
        val message: String? = null,
        val isWorking: Boolean = false
    )

    private val _extra = MutableStateFlow(UiState())

    val uiState: StateFlow<UiState> = combine(
        repository.observeTransactions(),
        _extra
    ) { txs, extra ->
        extra.copy(capital = FinanceEngine.capital(txs))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun addCapital(isInitial: Boolean, amount: Double, currency: Currency, method: PaymentMethod, rate: Double?, note: String) {
        viewModelScope.launch { repository.addCapital(isInitial, amount, currency, method, rate, note) }
    }

    fun addPersonalWithdrawal(amount: Double, currency: Currency, method: PaymentMethod, rate: Double?, note: String) {
        viewModelScope.launch { repository.addPersonalWithdrawal(amount, currency, method, rate, note) }
    }

    fun exportBackup(onResult: (File?) -> Unit) {
        viewModelScope.launch {
            _extra.value = _extra.value.copy(isWorking = true)
            try {
                val file = backupManager.exportToFile()
                _extra.value = _extra.value.copy(isWorking = false, message = "تم إنشاء نسخة احتياطية بنجاح", lastBackupPath = file.absolutePath)
                onResult(file)
            } catch (e: Exception) {
                _extra.value = _extra.value.copy(isWorking = false, message = "فشل إنشاء النسخة الاحتياطية: ${e.message}")
                onResult(null)
            }
        }
    }

    fun importBackup(file: File, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _extra.value = _extra.value.copy(isWorking = true)
            try {
                backupManager.importFromFile(file)
                _extra.value = _extra.value.copy(isWorking = false, message = "تمت استعادة البيانات بنجاح")
                onResult(true)
            } catch (e: Exception) {
                _extra.value = _extra.value.copy(isWorking = false, message = "فشل استيراد النسخة الاحتياطية: ${e.message}")
                onResult(false)
            }
        }
    }

    fun clearMessage() { _extra.value = _extra.value.copy(message = null) }
}
