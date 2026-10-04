package com.hasabati.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.PaymentMethod
import com.hasabati.app.data.repository.HasabatiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewOrderViewModel(private val repository: HasabatiRepository) : ViewModel() {

    val customers = repository.observeCustomers()

    data class ItemDraft(
        var productName: String = "",
        var quantity: String = "1",
        var unitCost: String = "",
        var unitPrice: String = ""
    )

    data class UiState(
        val selectedCustomerId: Long? = null,
        val newCustomerName: String = "",
        val newCustomerPhone: String = "",
        val items: List<ItemDraft> = listOf(ItemDraft()),
        val notes: String = "",
        val hasDeposit: Boolean = false,
        val depositAmount: String = "",
        val depositCurrency: Currency = Currency.USD,
        val depositMethod: PaymentMethod = PaymentMethod.CASH,
        val depositExchangeRate: String = "",
        val saving: Boolean = false,
        val error: String? = null,
        val savedOrderId: Long? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun selectCustomer(id: Long?) { _state.value = _state.value.copy(selectedCustomerId = id) }
    fun setNewCustomerName(v: String) { _state.value = _state.value.copy(newCustomerName = v) }
    fun setNewCustomerPhone(v: String) { _state.value = _state.value.copy(newCustomerPhone = v) }
    fun setNotes(v: String) { _state.value = _state.value.copy(notes = v) }

    fun updateItem(index: Int, item: ItemDraft) {
        val list = _state.value.items.toMutableList()
        if (index in list.indices) list[index] = item
        _state.value = _state.value.copy(items = list)
    }

    fun addItem() {
        _state.value = _state.value.copy(items = _state.value.items + ItemDraft())
    }

    fun removeItem(index: Int) {
        val list = _state.value.items.toMutableList()
        if (list.size > 1 && index in list.indices) list.removeAt(index)
        _state.value = _state.value.copy(items = list)
    }

    fun setHasDeposit(v: Boolean) { _state.value = _state.value.copy(hasDeposit = v) }
    fun setDepositAmount(v: String) { _state.value = _state.value.copy(depositAmount = v) }
    fun setDepositCurrency(v: Currency) { _state.value = _state.value.copy(depositCurrency = v) }
    fun setDepositMethod(v: PaymentMethod) { _state.value = _state.value.copy(depositMethod = v) }
    fun setDepositExchangeRate(v: String) { _state.value = _state.value.copy(depositExchangeRate = v) }

    val itemsTotalCost: (List<ItemDraft>) -> Double = { items ->
        items.sumOf { (it.quantity.toIntOrNull() ?: 0) * (it.unitCost.toDoubleOrNull() ?: 0.0) }
    }
    val itemsTotalSale: (List<ItemDraft>) -> Double = { items ->
        items.sumOf { (it.quantity.toIntOrNull() ?: 0) * (it.unitPrice.toDoubleOrNull() ?: 0.0) }
    }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            try {
                _state.value = s.copy(saving = true, error = null)

                val customerId = s.selectedCustomerId ?: run {
                    if (s.newCustomerName.isBlank()) throw IllegalArgumentException("اختر عميلة أو أدخل اسم عميلة جديدة")
                    repository.addCustomer(s.newCustomerName.trim(), s.newCustomerPhone.trim(), "")
                }

                val items = s.items.map {
                    HasabatiRepository.NewOrderItemInput(
                        productName = it.productName.trim(),
                        quantity = it.quantity.toIntOrNull() ?: 0,
                        expectedUnitCostUsd = it.unitCost.toDoubleOrNull() ?: 0.0,
                        unitSalePriceUsd = it.unitPrice.toDoubleOrNull() ?: 0.0
                    )
                }

                val depositAmount = if (s.hasDeposit) s.depositAmount.toDoubleOrNull() else null
                val depositRate = if (s.hasDeposit && s.depositCurrency != Currency.USD) s.depositExchangeRate.toDoubleOrNull() else null

                val orderId = repository.createOrder(
                    customerId = customerId,
                    items = items,
                    notes = s.notes,
                    depositAmount = depositAmount,
                    depositCurrency = if (s.hasDeposit) s.depositCurrency else null,
                    depositMethod = if (s.hasDeposit) s.depositMethod else null,
                    depositExchangeRate = depositRate
                )
                _state.value = _state.value.copy(saving = false, savedOrderId = orderId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(saving = false, error = e.message ?: "حدث خطأ غير متوقع")
            }
        }
    }
}
