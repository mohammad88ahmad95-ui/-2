package com.hasabati.app.ui.treasury

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.PaymentMethod
import com.hasabati.app.data.db.entities.Transaction
import com.hasabati.app.data.db.entities.TransactionType
import com.hasabati.app.ui.common.*
import com.hasabati.app.ui.theme.*

private enum class WalletTab { CASH, SHAM_CASH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasuryScreen() {
    val vm = hasabatiViewModel { TreasuryViewModel(it) }
    val state by vm.uiState.collectAsState()
    var showTransferDialog by remember { mutableStateOf(false) }
    var showConvertDialog by remember { mutableStateOf(false) }

    // حالة عرض فقط (لا تلمس أي بيانات) لتحديد أي رصيد كبير يُعرض في الأعلى
    var walletTab by remember { mutableStateOf(WalletTab.CASH) }
    var currencyTab by remember { mutableStateOf(Currency.USD) }

    val snap = state.snapshot
    val bigBalance: Double = when (walletTab to currencyTab) {
        WalletTab.CASH to Currency.USD -> snap.cashUsd
        WalletTab.CASH to Currency.SYP -> snap.cashSyp
        WalletTab.CASH to Currency.SAR -> snap.cashSar
        WalletTab.SHAM_CASH to Currency.USD -> snap.shamCashUsd
        WalletTab.SHAM_CASH to Currency.SYP -> snap.shamCashSyp
        else -> snap.shamCashSar
    }
    val bigBalanceText = when (currencyTab) {
        Currency.USD -> Formatters.usd(bigBalance)
        Currency.SYP -> Formatters.syp(bigBalance)
        else -> Formatters.sar(bigBalance)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("الخزينة", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimaryDark) }

        // ---------------- بطاقة الرصيد الكبير ----------------
        item {
            GradientHeroCard(modifier = Modifier.fillMaxWidth()) {
                // نقدي / شام كاش
                SegmentedRow(
                    options = listOf("نقدي" to WalletTab.CASH, "شام كاش" to WalletTab.SHAM_CASH),
                    selected = walletTab,
                    onSelect = { walletTab = it }
                )
                Spacer(Modifier.height(14.dp))
                Text("الرصيد المتاح", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
                MoneyText(bigBalanceText, color = Color.White, style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(14.dp))
                SegmentedRow(
                    options = listOf("USD" to Currency.USD, "ل.س" to Currency.SYP, "SAR" to Currency.SAR),
                    selected = currencyTab,
                    onSelect = { currencyTab = it },
                    light = true
                )
            }
        }

        // ---------------- أزرار التحويل ----------------
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(
                    text = "Sham Cash ↔ نقد",
                    onClick = { showTransferDialog = true },
                    leadingIcon = Icons.Filled.SwapHoriz,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "تحويل عملات",
                    onClick = { showConvertDialog = true },
                    leadingIcon = Icons.Filled.CompareArrows,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ---------------- كل الأرصدة (تفاصيل) ----------------
        item { SectionTitle("كل الأرصدة") }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(330.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { StatCard("النقد بالدولار", Formatters.usd(snap.cashUsd), "💵", SuccessGreen) }
                item { StatCard("النقد بالليرة", Formatters.syp(snap.cashSyp), "💴", WarningAmber) }
                item { StatCard("النقد بالريال", Formatters.sar(snap.cashSar), "💰", StatusBlue) }
                item { StatCard("Sham Cash دولار", Formatters.usd(snap.shamCashUsd), "📱", PurpleAccent) }
                item { StatCard("Sham Cash ليرة", Formatters.syp(snap.shamCashSyp), "📱", PurpleAccentLight) }
                item { StatCard("Sham Cash ريال", Formatters.sar(snap.shamCashSar), "📱", StatusTeal) }
            }
        }

        item { SectionTitle("سجل الحركات") }
        if (state.transactions.isEmpty()) {
            item { EmptyState("لا توجد حركات بعد") }
        } else {
            items(state.transactions) { tx -> TreasuryTxRow(tx) }
        }
        item { Spacer(Modifier.height(90.dp)) }
    }

    if (showTransferDialog) {
        WalletTransferSheet(
            onDismiss = { showTransferDialog = false },
            onConfirm = { amount, currency ->
                vm.transferToCash(amount, currency)
                showTransferDialog = false
            }
        )
    }
    if (showConvertDialog) {
        CurrencyConvertSheet(
            onDismiss = { showConvertDialog = false },
            onConfirm = { amountOut, currencyOut, methodOut, amountIn, currencyIn, methodIn ->
                vm.convertCurrency(amountOut, currencyOut, methodOut, amountIn, currencyIn, methodIn)
                showConvertDialog = false
            }
        )
    }
}

/**
 * Segmented control عام صغير — تصميم فقط، بديل بصري لأزرار FilterChip المتفرقة.
 */
@Composable
private fun <T> SegmentedRow(
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
    light: Boolean = false
) {
    val bg = if (light) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.14f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(4.dp)
    ) {
        options.forEach { (label, value) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable2segmented { onSelect(value) }
                    .padding(vertical = 8.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) PurpleAccent else Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun Modifier.clickable2segmented(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletTransferSheet(onDismiss: () -> Unit, onConfirm: (Double, Currency) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currency.USD) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceWhite) {
        Column(Modifier.padding(20.dp).padding(bottom = 24.dp)) {
            Text("تحويل من Sham Cash إلى نقد", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("هذا يسحب المبلغ من رصيد Sham Cash ويضيفه إلى النقد بنفس العملة.", color = TextSecondaryGray, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") }, shape = AppShapes.small, modifier = Modifier.fillMaxWidth())
            Row(Modifier.padding(top = 10.dp)) {
                Currency.values().forEach { c ->
                    FilterChip(selected = currency == c, onClick = { currency = c }, label = { Text(c.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "تحويل",
                onClick = { amount.toDoubleOrNull()?.let { onConfirm(it, currency) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * تحويل عملة إلى عملة أخرى: تُدخل المستخدمة المبلغ الذي "خرج" (مثلاً 1,500,000 ليرة) والمبلغ
 * الذي "دخل" فعلياً بدل ذلك (مثلاً 100 دولار) — بدون أي سعر صرف مجرّد قد يلخبط الاتجاه.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyConvertSheet(
    onDismiss: () -> Unit,
    onConfirm: (Double, Currency, PaymentMethod, Double, Currency, PaymentMethod) -> Unit
) {
    var amountOut by remember { mutableStateOf("") }
    var currencyOut by remember { mutableStateOf(Currency.SYP) }
    var methodOut by remember { mutableStateOf(PaymentMethod.CASH) }

    var amountIn by remember { mutableStateOf("") }
    var currencyIn by remember { mutableStateOf(Currency.USD) }
    var methodIn by remember { mutableStateOf(PaymentMethod.CASH) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceWhite) {
        Column(Modifier.padding(20.dp).padding(bottom = 24.dp)) {
            Text("تحويل بين العملات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))

            Text("من (المبلغ الذي أخرجتِه)", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(value = amountOut, onValueChange = { amountOut = it }, label = { Text("المبلغ") }, shape = AppShapes.small, modifier = Modifier.fillMaxWidth())
            Row(Modifier.padding(top = 6.dp)) {
                Currency.values().forEach { c ->
                    FilterChip(selected = currencyOut == c, onClick = { currencyOut = c }, label = { Text(c.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                }
            }
            Row(Modifier.padding(top = 6.dp)) {
                PaymentMethod.values().forEach { m ->
                    FilterChip(selected = methodOut == m, onClick = { methodOut = m }, label = { Text(m.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = BorderGray)
            Spacer(Modifier.height(16.dp))

            Text("إلى (المبلغ الذي استلمتِه فعلياً)", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(value = amountIn, onValueChange = { amountIn = it }, label = { Text("المبلغ") }, shape = AppShapes.small, modifier = Modifier.fillMaxWidth())
            Row(Modifier.padding(top = 6.dp)) {
                Currency.values().forEach { c ->
                    FilterChip(selected = currencyIn == c, onClick = { currencyIn = c }, label = { Text(c.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                }
            }
            Row(Modifier.padding(top = 6.dp)) {
                PaymentMethod.values().forEach { m ->
                    FilterChip(selected = methodIn == m, onClick = { methodIn = m }, label = { Text(m.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                }
            }

            if (amountOut.toDoubleOrNull() != null && amountIn.toDoubleOrNull() != null && amountIn.toDoubleOrNull() != 0.0 && currencyOut != currencyIn) {
                val rate = amountOut.toDouble() / amountIn.toDouble()
                Spacer(Modifier.height(12.dp))
                Text(
                    "سعر الصرف المحسوب تلقائياً: ${Formatters.amount(rate, currencyOut)} لكل 1 ${currencyIn.arabicLabel}",
                    color = TextSecondaryGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "تحويل",
                onClick = {
                    val out = amountOut.toDoubleOrNull() ?: return@PrimaryButton
                    val inn = amountIn.toDoubleOrNull() ?: return@PrimaryButton
                    onConfirm(out, currencyOut, methodOut, inn, currencyIn, methodIn)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TreasuryTxRow(tx: Transaction) {
    val positive = tx.type == TransactionType.CUSTOMER_PAYMENT || tx.type == TransactionType.CAPITAL_ADDITION ||
        tx.type == TransactionType.CAPITAL_INITIAL || tx.type == TransactionType.TRANSFER_IN
    val color = if (positive) SuccessGreen else DangerRed
    TransactionRow(
        title = tx.type.arabicLabel,
        subtitle = "${tx.method.arabicLabel} • ${Formatters.dateTime(tx.createdAt)}",
        amountText = "${if (positive) "+" else "-"}${Formatters.amount(tx.amount, tx.currency)}",
        amountColor = color,
        modifier = Modifier.fillMaxWidth()
    )
}
