package com.hasabati.app.ui.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.PaymentMethod
import com.hasabati.app.data.db.entities.TransactionType
import com.hasabati.app.ui.common.*
import com.hasabati.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(customerId: Long, onBack: () -> Unit, onOpenOrder: (Long) -> Unit) {
    val vm = hasabatiViewModel { CustomerDetailViewModel(it, customerId) }
    val state by vm.uiState.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text(state.customer?.name ?: "العميلة") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            if (state.remainingUsd > 0.009) {
                ExtendedFloatingActionButton(onClick = { showPaymentDialog = true }, containerColor = SuccessGreen, contentColor = Color.White) {
                    Text("تحصيل دفعة")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().background(BackgroundLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                GradientHeroCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InitialAvatar(state.customer?.name ?: "؟", color = Color.White, size = 48.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(state.customer?.name ?: "", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (state.customer?.phone?.isNotBlank() == true) {
                                Text(state.customer?.phone ?: "", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("الرصيد المتبقي", color = Color.White.copy(alpha = 0.8f))
                    MoneyText(
                        Formatters.usd(state.remainingUsd),
                        color = if (state.remainingUsd > 0.009) OnGradientDanger else OnGradientSuccess,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }

            item { SectionTitle("كشف الحساب") }
            if (state.statement.isEmpty()) {
                item { EmptyState("لا توجد عمليات بعد") }
            } else {
                items(state.statement) { line ->
                    when (line) {
                        is CustomerDetailViewModel.StatementLine.OrderLine -> {
                            StatementRow(
                                title = "طلب ${line.order.orderNumber}",
                                subtitle = Formatters.date(line.date),
                                amountText = "+${Formatters.usd(line.order.saleTotalUsd)}",
                                color = DangerRed,
                                onClick = { onOpenOrder(line.order.id) }
                            )
                        }
                        is CustomerDetailViewModel.StatementLine.PaymentLine -> {
                            val isRefund = line.tx.type == TransactionType.REFUND
                            StatementRow(
                                title = if (isRefund) "استرداد عربون" else "دفعة",
                                subtitle = Formatters.date(line.date),
                                amountText = "${if (isRefund) "+" else "-"}${Formatters.usd(line.tx.usdEquivalent)}",
                                color = if (isRefund) DangerRed else SuccessGreen,
                                onClick = null
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showPaymentDialog) {
        SimplePaymentDialog(
            title = "تحصيل دفعة",
            maxAmount = state.remainingUsd,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, currency, method, rate ->
                vm.collectPayment(null, amount, currency, method, rate)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
private fun StatementRow(title: String, subtitle: String, amountText: String, color: Color, onClick: (() -> Unit)?) {
    Card(
        onClick = { onClick?.invoke() },
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        enabled = onClick != null
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = TextSecondaryGray, style = MaterialTheme.typography.bodySmall)
            }
            MoneyText(amountText, color = color, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun SimplePaymentDialog(
    title: String,
    maxAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Currency, PaymentMethod, Double?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currency.USD) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    var rate by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (maxAmount > 0) Text("المتبقي: ${Formatters.usd(maxAmount)}", color = TextSecondaryGray)
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") }, shape = AppShapes.small, modifier = Modifier.fillMaxWidth())
                Row(Modifier.padding(top = 8.dp)) {
                    Currency.values().forEach { c ->
                        FilterChip(selected = currency == c, onClick = { currency = c }, label = { Text(c.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                    }
                }
                Row(Modifier.padding(top = 8.dp)) {
                    PaymentMethod.values().forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                    }
                }
                if (currency != Currency.USD) {
                    OutlinedTextField(
                        value = rate, onValueChange = { rate = it },
                        label = { Text("سعر الصرف (${currency.symbol} لكل 1$)") },
                        shape = AppShapes.small,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull() ?: return@TextButton
                onConfirm(amt, currency, method, if (currency != Currency.USD) rate.toDoubleOrNull() else null)
            }) { Text("تأكيد") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
