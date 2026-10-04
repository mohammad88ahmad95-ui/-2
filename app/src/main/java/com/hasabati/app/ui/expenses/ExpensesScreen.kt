package com.hasabati.app.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.ExpenseCategories
import com.hasabati.app.data.db.entities.PaymentMethod
import com.hasabati.app.ui.common.*
import com.hasabati.app.ui.theme.*

private fun categoryIcon(category: String?): Pair<ImageVector, Color> = when (category) {
    "مواصلات" -> Icons.Filled.DirectionsCar to WarningAmber
    "تغليف" -> Icons.Filled.Inventory2 to PurpleAccent
    "اتصالات" -> Icons.Filled.Phone to StatusBlue
    "توصيل" -> Icons.Filled.LocalShipping to StatusTeal
    "عمولة" -> Icons.Filled.Payments to SuccessGreen
    else -> Icons.Filled.Receipt to TextSecondaryGray
}

@Composable
fun ExpensesScreen() {
    val vm = hasabatiViewModel { ExpensesViewModel(it) }
    val expenses by vm.expenses.collectAsState()
    val total by vm.totalUsd.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = PurpleAccent, shape = AppShapes.large) {
                Icon(Icons.Filled.Add, contentDescription = "مصروف جديد", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).background(BackgroundLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Text("المصاريف", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimaryDark) }
            item {
                GradientHeroCard(modifier = Modifier.fillMaxWidth()) {
                    Text("إجمالي المصاريف", color = Color.White.copy(alpha = 0.8f))
                    MoneyText(Formatters.usd(total), color = Color.White, style = MaterialTheme.typography.displaySmall)
                }
            }
            item { SectionTitle("السجل") }
            if (expenses.isEmpty()) {
                item { EmptyState("لا توجد مصاريف بعد", icon = Icons.Filled.Receipt) }
            } else {
                items(expenses) { tx ->
                    val (icon, color) = categoryIcon(tx.expenseCategory)
                    Card(
                        shape = AppShapes.medium,
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            CategoryIcon(icon, color)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(tx.expenseCategory ?: "مصروف", fontWeight = FontWeight.SemiBold)
                                Text(Formatters.dateTime(tx.createdAt), color = TextSecondaryGray, style = MaterialTheme.typography.bodySmall)
                                if (tx.note.isNotBlank()) Text(tx.note, color = TextSecondaryGray, style = MaterialTheme.typography.bodySmall)
                            }
                            MoneyText("-${Formatters.amount(tx.amount, tx.currency)}", color = DangerRed, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        AddExpenseDialog(onDismiss = { showDialog = false }, onSave = { amount, currency, method, rate, category, note ->
            vm.addExpense(amount, currency, method, rate, category, note)
            showDialog = false
        })
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (Double, Currency, PaymentMethod, Double?, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currency.USD) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    var rate by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategories.defaults.first()) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مصروف جديد") },
        text = {
            Column {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") }, shape = AppShapes.small, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ExpenseCategories.defaults) { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    Currency.values().forEach { c ->
                        FilterChip(selected = currency == c, onClick = { currency = c }, label = { Text(c.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    PaymentMethod.values().forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                    }
                }
                if (currency != Currency.USD) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rate, onValueChange = { rate = it },
                        label = { Text("سعر الصرف (${currency.symbol} لكل 1$)") },
                        shape = AppShapes.small, modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("ملاحظة") }, shape = AppShapes.small, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull() ?: return@TextButton
                onSave(amt, currency, method, if (currency != Currency.USD) rate.toDoubleOrNull() else null, category, note)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
