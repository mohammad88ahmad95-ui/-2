package com.hasabati.app.ui.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hasabati.app.ui.common.*
import com.hasabati.app.ui.theme.*

@Composable
fun CustomersListScreen(onOpenCustomer: (Long) -> Unit) {
    val vm = hasabatiViewModel { CustomersListViewModel(it) }
    val rows by vm.rows.collectAsState()
    val query by vm.query.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = PurpleAccent, shape = AppShapes.large) {
                Icon(Icons.Filled.Add, contentDescription = "عميلة جديدة")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(BackgroundLight)) {
            Column(Modifier.padding(16.dp)) {
                Text("العملاء", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Spacer(Modifier.height(12.dp))
                SearchBarField(value = query, onValueChange = { vm.setQuery(it) }, placeholder = "ابحث بالاسم أو رقم الهاتف")
            }
            if (rows.isEmpty()) {
                EmptyState("لا يوجد عملاء بعد", icon = Icons.Filled.People)
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(rows) { row ->
                        Card(
                            onClick = { onOpenCustomer(row.customer.id) },
                            shape = AppShapes.medium,
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                        ) {
                            Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                InitialAvatar(row.customer.name, color = if (row.remainingUsd > 0.009) DangerRed else PurpleAccent)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(row.customer.name, fontWeight = FontWeight.Bold)
                                    if (row.customer.phone.isNotBlank()) {
                                        Text(row.customer.phone, color = TextSecondaryGray, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                    if (row.remainingUsd > 0.009) {
                                        StatusChip("مستحقات", DangerRed)
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    MoneyText(Formatters.usd(row.remainingUsd), style = MaterialTheme.typography.titleMedium, color = if (row.remainingUsd > 0.009) DangerRed else SuccessGreen)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(onDismiss = { showAddDialog = false }, onSave = { name, phone ->
            vm.addCustomer(name, phone, "") { showAddDialog = false }
        })
    }
}

@Composable
private fun AddCustomerDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("عميلة جديدة") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, shape = AppShapes.small, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف") },
                    shape = AppShapes.small,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
        },
        confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim(), phone.trim()) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
