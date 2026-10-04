package com.hasabati.app.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.PaymentMethod
import com.hasabati.app.ui.common.Formatters
import com.hasabati.app.ui.common.SectionTitle
import com.hasabati.app.ui.common.hasabatiViewModel
import com.hasabati.app.ui.theme.BorderGray
import com.hasabati.app.ui.theme.PurpleAccent
import com.hasabati.app.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(onBack: () -> Unit, onSaved: (Long) -> Unit) {
    val vm = hasabatiViewModel { NewOrderViewModel(it) }
    val state by vm.state.collectAsState()
    val customers by vm.customers.collectAsState(initial = emptyList())

    LaunchedEffect(state.savedOrderId) {
        state.savedOrderId?.let { onSaved(it) }
    }

    var customerMode by remember { mutableStateOf(0) } // 0 = existing, 1 = new
    var customerMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("طلب جديد") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SectionTitle("العميلة") }
            item {
                Row {
                    FilterChip(selected = customerMode == 0, onClick = { customerMode = 0 }, label = { Text("عميلة موجودة") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = customerMode == 1, onClick = { customerMode = 1; vm.selectCustomer(null) }, label = { Text("عميلة جديدة") })
                }
            }
            if (customerMode == 0) {
                item {
                    ExposedDropdownMenuBox(expanded = customerMenuExpanded, onExpandedChange = { customerMenuExpanded = it }) {
                        OutlinedTextField(
                            value = customers.find { it.id == state.selectedCustomerId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("اختر العميلة") },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = customerMenuExpanded, onDismissRequest = { customerMenuExpanded = false }) {
                            customers.forEach { c ->
                                DropdownMenuItem(text = { Text("${c.name}  ${c.phone}") }, onClick = {
                                    vm.selectCustomer(c.id); customerMenuExpanded = false
                                })
                            }
                        }
                    }
                }
            } else {
                item {
                    OutlinedTextField(
                        value = state.newCustomerName, onValueChange = { vm.setNewCustomerName(it) },
                        label = { Text("اسم العميلة") }, modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = state.newCustomerPhone, onValueChange = { vm.setNewCustomerPhone(it) },
                        label = { Text("رقم الهاتف") }, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            }

            item { SectionTitle("المنتجات") }

            state.items.forEachIndexed { index, item ->
                item {
                    ProductItemCard(
                        item = item,
                        canDelete = state.items.size > 1,
                        onChange = { vm.updateItem(index, it) },
                        onDelete = { vm.removeItem(index) }
                    )
                }
            }
            item {
                OutlinedButton(onClick = { vm.addItem() }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ إضافة منتج آخر")
                }
            }

            item {
                val totalCost = vm.itemsTotalCost(state.items)
                val totalSale = vm.itemsTotalSale(state.items)
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = PurpleAccent.copy(alpha = 0.08f))) {
                    Column(Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي التكلفة المتوقعة"); Text(Formatters.usd(totalCost), fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إجمالي البيع"); Text(Formatters.usd(totalSale), fontWeight = FontWeight.Bold)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الربح المتوقع"); Text(Formatters.usd(totalSale - totalCost), fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }
            }

            item { SectionTitle("العربون") }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = state.hasDeposit, onCheckedChange = { vm.setHasDeposit(it) })
                    Spacer(Modifier.width(8.dp))
                    Text("هل دفعت العميلة عربوناً؟")
                }
            }
            if (state.hasDeposit) {
                item {
                    OutlinedTextField(
                        value = state.depositAmount, onValueChange = { vm.setDepositAmount(it) },
                        label = { Text("المبلغ") }, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                item {
                    Row {
                        Currency.values().forEach { c ->
                            FilterChip(
                                selected = state.depositCurrency == c, onClick = { vm.setDepositCurrency(c) },
                                label = { Text(c.arabicLabel) }, modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
                item {
                    Row {
                        PaymentMethod.values().forEach { m ->
                            FilterChip(
                                selected = state.depositMethod == m, onClick = { vm.setDepositMethod(m) },
                                label = { Text(m.arabicLabel) }, modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                }
                if (state.depositCurrency != Currency.USD) {
                    item {
                        OutlinedTextField(
                            value = state.depositExchangeRate, onValueChange = { vm.setDepositExchangeRate(it) },
                            label = { Text("سعر الصرف (${state.depositCurrency.symbol} لكل 1$)") }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }

            item { SectionTitle("ملاحظات") }
            item {
                OutlinedTextField(
                    value = state.notes, onValueChange = { vm.setNotes(it) },
                    modifier = Modifier.fillMaxWidth(), minLines = 2
                )
            }

            if (state.error != null) {
                item {
                    Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                }
            }

            item {
                Button(
                    onClick = { vm.save() },
                    enabled = !state.saving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (state.saving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = androidx.compose.ui.graphics.Color.White)
                    else Text("حفظ الطلب", style = MaterialTheme.typography.titleMedium)
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun ProductItemCard(
    item: NewOrderViewModel.ItemDraft,
    canDelete: Boolean,
    onChange: (NewOrderViewModel.ItemDraft) -> Unit,
    onDelete: () -> Unit
) {
    Card(shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.productName,
                    onValueChange = { onChange(item.copy(productName = it)) },
                    label = { Text("اسم المنتج") },
                    modifier = Modifier.weight(1f)
                )
                if (canDelete) {
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "حذف") }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.quantity, onValueChange = { onChange(item.copy(quantity = it)) },
                    label = { Text("الكمية") }, modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = item.unitCost, onValueChange = { onChange(item.copy(unitCost = it)) },
                    label = { Text("شراء الوحدة $") }, modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = item.unitPrice, onValueChange = { onChange(item.copy(unitPrice = it)) },
                    label = { Text("بيع الوحدة $") }, modifier = Modifier.weight(1f),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}
