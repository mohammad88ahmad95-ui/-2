package com.hasabati.app.ui.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.ui.common.*
import com.hasabati.app.ui.customers.SimplePaymentDialog
import com.hasabati.app.ui.theme.*

@Composable
fun AgentScreen() {
    val vm = hasabatiViewModel { AgentViewModel(it) }
    val state by vm.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showDialog = true }, containerColor = PurpleAccent, contentColor = Color.White) {
                Text("دفع للوكيلة")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).background(BackgroundLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text("حساب الوكيلة", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimaryDark) }
            item {
                GradientHeroCard(modifier = Modifier.fillMaxWidth()) {
                    Text("المتبقي للوكيلة", color = Color.White.copy(alpha = 0.8f))
                    MoneyText(
                        Formatters.usd(state.remainingUsd),
                        color = if (state.remainingUsd > 0.009) OnGradientDanger else OnGradientSuccess,
                        style = MaterialTheme.typography.displaySmall
                    )
                    Spacer(Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("إجمالي المستحق", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                            Text(Formatters.usd(state.totalOwedUsd), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("المدفوع", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                            Text(Formatters.usd(state.paidUsd), color = OnGradientSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (state.paidByCurrency.isNotEmpty()) {
                item { SectionTitle("المدفوع حسب العملة") }
                item {
                    Card(
                        shape = AppShapes.medium,
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            val maxValue = state.paidByCurrency.values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
                            state.paidByCurrency.forEach { (currency, amount) ->
                                ProgressStatRow(
                                    label = currency.arabicLabel,
                                    valueText = Formatters.amount(amount, currency),
                                    progress = (amount / maxValue).toFloat(),
                                    color = when (currency) {
                                        Currency.USD -> SuccessGreen
                                        Currency.SYP -> WarningAmber
                                        Currency.SAR -> StatusBlue
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item { SectionTitle("سجل المدفوعات") }
            if (state.payments.isEmpty()) {
                item { EmptyState("لا توجد دفعات بعد") }
            } else {
                items(state.payments) { tx ->
                    TransactionRow(
                        title = "دفع للوكيلة",
                        subtitle = "${tx.method.arabicLabel} • ${Formatters.dateTime(tx.createdAt)}",
                        note = tx.note.takeIf { it.isNotBlank() },
                        amountText = "-${Formatters.amount(tx.amount, tx.currency)}",
                        amountColor = DangerRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        SimplePaymentDialog(
            title = "دفع للوكيلة",
            maxAmount = state.remainingUsd,
            onDismiss = { showDialog = false },
            onConfirm = { amount, currency, method, rate ->
                vm.payAgent(amount, currency, method, rate, "دفعة للوكيلة")
                showDialog = false
            }
        )
    }
}
