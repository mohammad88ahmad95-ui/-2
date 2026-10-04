package com.hasabati.app.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.OrderStatus
import com.hasabati.app.data.db.entities.PaymentMethod
import com.hasabati.app.ui.common.*
import com.hasabati.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(orderId: Long, onBack: () -> Unit) {
    val vm = hasabatiViewModel { OrderDetailViewModel(it, orderId) }
    val state by vm.uiState.collectAsState()
    val order = state.order

    var showArrivalSheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(order?.orderNumber ?: "الطلب") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (order == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().background(BackgroundLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(state.customer?.name ?: "—", style = MaterialTheme.typography.titleLarge)
                        Text(Formatters.date(order.createdAt), color = TextSecondaryGray)
                    }
                    StatusChip(order.status.arabicLabel, order.status.color())
                }
            }

            if (order.status != OrderStatus.CANCELLED && order.status != OrderStatus.DELIVERED) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (order.status == OrderStatus.SHIPPING) {
                            PrimaryButton(text = "تأكيد الوصول", onClick = { showArrivalSheet = true })
                        } else {
                            val next = order.status.next()
                            if (next != null) {
                                PrimaryButton(text = "نقل إلى: ${next.arabicLabel}", onClick = { vm.advanceStatus() })
                            }
                        }
                        SecondaryButton(text = "إلغاء الطلب", onClick = { showCancelDialog = true }, contentColor = DangerRed)
                    }
                }
            }

            item { SectionTitle("ملخص الطلب") }
            item {
                GradientHeroCard(modifier = Modifier.fillMaxWidth()) {
                    SummaryRow("إجمالي البيع", Formatters.usd(order.saleTotalUsd), OnGradientText)
                    SummaryRow("المدفوع", Formatters.usd(state.paidUsd), OnGradientSuccess)
                    SummaryRow("المتبقي", Formatters.usd(state.remainingUsd), if (state.remainingUsd > 0.009) OnGradientDanger else OnGradientSuccess)

                    Spacer(Modifier.height(10.dp))
                    val progress = if (order.saleTotalUsd > 0.0) (state.paidUsd / order.saleTotalUsd).toFloat().coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = progress,
                        color = OnGradientSuccess,
                        trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.height(14.dp))
                    Divider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f))
                    Spacer(Modifier.height(10.dp))

                    if (order.actualCostUsd != null) {
                        SummaryRow("الربح الفعلي", Formatters.usd(order.saleTotalUsd - order.actualCostUsd), OnGradientSuccess)
                    } else {
                        SummaryRow("الربح المتوقع", Formatters.usd(order.saleTotalUsd - order.expectedCostUsd), OnGradientSuccess)
                    }
                }
            }

            item { SectionTitle("المنتجات") }
            items(state.items) { item ->
                val profit = item.saleTotal - (item.actualCostTotal ?: item.expectedCostTotal)
                Card(shape = AppShapes.medium, border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(item.productName, fontWeight = FontWeight.SemiBold)
                        Text("الكمية: ${item.quantity}", color = TextSecondaryGray, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تكلفة: ${Formatters.usd(item.actualCostTotal ?: item.expectedCostTotal)}", style = MaterialTheme.typography.bodyMedium, color = if (item.actualCostTotal != null) WarningAmber else TextSecondaryGray)
                            Text("بيع: ${Formatters.usd(item.saleTotal)}", style = MaterialTheme.typography.bodyMedium)
                            Text("ربح: ${Formatters.usd(profit)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = if (profit >= 0) SuccessGreen else DangerRed)
                        }
                    }
                }
            }

            if (order.status != OrderStatus.CANCELLED && state.remainingUsd > 0.009) {
                item {
                    PrimaryButton(
                        text = "تحصيل دفعة",
                        onClick = { showPaymentSheet = true },
                        containerColor = SuccessGreen,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (order.status == OrderStatus.CANCELLED) {
                item {
                    val label = when (order.depositRefunded) {
                        true -> "تم رد العربون للعميلة"
                        false -> "لم يُرد العربون بعد"
                        null -> "لا يوجد عربون على هذا الطلب"
                    }
                    Text(label, color = TextSecondaryGray)
                }
            }

            item { SectionTitle("سجل العمليات على هذا الطلب") }
            if (state.transactions.isEmpty()) {
                item { EmptyState("لا توجد عمليات بعد") }
            } else {
                items(state.transactions) { tx ->
                    TransactionRow(
                        title = tx.type.arabicLabel,
                        subtitle = Formatters.dateTime(tx.createdAt),
                        amountText = Formatters.amount(tx.amount, tx.currency),
                        amountColor = TextPrimaryDark,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item { Spacer(Modifier.height(50.dp)) }
        }
    }

    if (showArrivalSheet) {
        ArrivalBottomSheet(items = state.items, onDismiss = { showArrivalSheet = false }, onConfirm = {
            vm.confirmArrival(it); showArrivalSheet = false
        })
    }
    if (showPaymentSheet) {
        PaymentBottomSheet(maxAmount = state.remainingUsd, onDismiss = { showPaymentSheet = false }, onConfirm = { amount, currency, method, rate ->
            vm.collectPayment(amount, currency, method, rate); showPaymentSheet = false
        })
    }
    if (showCancelDialog) {
        CancelOrderDialog(hasPayments = state.paidUsd > 0.0, onDismiss = { showCancelDialog = false }, onConfirm = {
            vm.cancelOrder(it); showCancelDialog = false
        })
    }
}

@Composable
private fun SummaryRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f))
        Text(value, color = color, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArrivalBottomSheet(
    items: List<com.hasabati.app.data.db.entities.OrderItem>,
    onDismiss: () -> Unit,
    onConfirm: (Map<Long, Double>) -> Unit
) {
    val costs = remember { mutableStateMapOf<Long, String>().apply { items.forEach { put(it.id, it.expectedUnitCostUsd.toString()) } } }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceWhite) {
        Column(Modifier.padding(20.dp).padding(bottom = 24.dp)) {
            Text("تأكيد وصول الطلب", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("أدخلي التكلفة الفعلية لكل منتج", color = TextSecondaryGray, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            items.forEach { item ->
                OutlinedTextField(
                    value = costs[item.id] ?: "",
                    onValueChange = { costs[item.id] = it },
                    label = { Text("${item.productName} — تكلفة الوحدة $") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = AppShapes.small,
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                )
            }
            Spacer(Modifier.height(10.dp))
            PrimaryButton(
                text = "تأكيد الوصول",
                onClick = { onConfirm(items.associate { it.id to (costs[it.id]?.toDoubleOrNull() ?: it.expectedUnitCostUsd) }) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentBottomSheet(
    maxAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Currency, PaymentMethod, Double?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currency.USD) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    var rate by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceWhite) {
        Column(Modifier.padding(20.dp).padding(bottom = 24.dp)) {
            Text("تحصيل دفعة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("المتبقي: ${Formatters.usd(maxAmount)}", color = TextSecondaryGray)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") }, shape = AppShapes.small, modifier = Modifier.fillMaxWidth())
            Row(Modifier.padding(top = 10.dp)) {
                Currency.values().forEach { c ->
                    FilterChip(selected = currency == c, onClick = { currency = c }, label = { Text(c.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                }
            }
            Row(Modifier.padding(top = 10.dp)) {
                PaymentMethod.values().forEach { m ->
                    FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                }
            }
            if (currency != Currency.USD) {
                OutlinedTextField(
                    value = rate, onValueChange = { rate = it },
                    label = { Text("سعر الصرف (${currency.symbol} لكل 1$)") },
                    shape = AppShapes.small,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "تأكيد",
                containerColor = SuccessGreen,
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@PrimaryButton
                    onConfirm(amt, currency, method, if (currency != Currency.USD) rate.toDoubleOrNull() else null)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CancelOrderDialog(hasPayments: Boolean, onDismiss: () -> Unit, onConfirm: (Boolean?) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إلغاء الطلب") },
        text = {
            Text(if (hasPayments) "هذا الطلب عليه دفعات مسجلة. هل تم رد العربون للعميلة؟" else "هل أنت متأكدة من إلغاء هذا الطلب؟")
        },
        confirmButton = {
            if (hasPayments) {
                TextButton(onClick = { onConfirm(true) }) { Text("نعم، تم الرد") }
            } else {
                TextButton(onClick = { onConfirm(null) }) { Text("تأكيد الإلغاء") }
            }
        },
        dismissButton = {
            if (hasPayments) TextButton(onClick = { onConfirm(false) }) { Text("لا، لم يُرد بعد") }
            else TextButton(onClick = onDismiss) { Text("تراجع") }
        }
    )
}
