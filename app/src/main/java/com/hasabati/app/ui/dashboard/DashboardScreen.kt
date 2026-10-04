package com.hasabati.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasabati.app.data.db.entities.TransactionType
import com.hasabati.app.ui.common.*
import com.hasabati.app.ui.theme.*

@Composable
fun DashboardScreen(
    onNewOrder: () -> Unit,
    onCollectPayment: () -> Unit,
    onAddExpense: () -> Unit,
    onPayAgent: () -> Unit,
    onOpenTreasury: () -> Unit,
    onOpenAgent: () -> Unit,
    onOpenCustomersWithBalance: () -> Unit,
    onOpenArrivedOrders: () -> Unit,
    onOpenNotifications: () -> Unit = {} // إضافة اختيارية آمنة — لا تكسر أي استدعاء قديم لهذه الشاشة
) {
    val vm = hasabatiViewModel { DashboardViewModel(it) }
    val state by vm.uiState.collectAsState()
    val snap = state.snapshot

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------------- ترويسة ترحيب ----------------
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("مرحباً 👋", style = MaterialTheme.typography.headlineMedium, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                    Text("إليك ملخص حساباتك اليوم", color = TextSecondaryGray, style = MaterialTheme.typography.bodyMedium)
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceLavender)
                        .clickable(onClick = onOpenNotifications),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.NotificationsNone, contentDescription = "الإشعارات", tint = PurpleAccent)
                }
            }
        }

        // ---------------- بطاقة الوضع المالي ----------------
        item {
            GradientHeroCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("الوضع المالي", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("💼", fontSize = 22.sp)
                }
                Spacer(Modifier.height(18.dp))

                // "معي" هو الرقم الأهم بصرياً — يظهر منفرداً وبخط أكبر (تسلسل هرمي واضح)
                val cashTotal = (snap?.treasury?.cashUsd ?: 0.0) + (snap?.treasury?.shamCashUsd ?: 0.0)
                Text("معي", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                MoneyText(Formatters.usd(cashTotal), color = Color.White, style = MaterialTheme.typography.displaySmall)

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MiniStat("لي", Formatters.usd(snap?.customerReceivablesUsd ?: 0.0), Color(0xFFB9F5D8))
                    MiniStat("عليّ", Formatters.usd(snap?.agentPayableUsd ?: 0.0), Color(0xFFFFD1DC))
                }

                val cashSypTotal = (snap?.treasury?.cashSyp ?: 0.0) + (snap?.treasury?.shamCashSyp ?: 0.0)
                val cashSarTotal = (snap?.treasury?.cashSar ?: 0.0) + (snap?.treasury?.shamCashSar ?: 0.0)
                Spacer(Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.2f))
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    MiniStat("معي بالليرة", Formatters.syp(cashSypTotal), Color.White.copy(alpha = 0.9f))
                    MiniStat("معي بالريال", Formatters.sar(cashSarTotal), Color.White.copy(alpha = 0.9f))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "عرض التفاصيل",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable(onClick = onOpenTreasury)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        item {
            SectionTitle("الإجراءات السريعة")
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { QuickActionButton("طلب جديد", Icons.Filled.AddShoppingCart, PurpleAccent, onNewOrder) }
                item { QuickActionButton("تحصيل دفعة", Icons.Filled.Payments, SuccessGreen, onCollectPayment) }
                item { QuickActionButton("إضافة مصروف", Icons.Filled.Receipt, WarningAmber, onAddExpense) }
                item { QuickActionButton("دفع للوكيلة", Icons.Filled.LocalShipping, StatusBlue, onPayAgent) }
            }
        }

        item { SectionTitle("الأرصدة") }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(450.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { StatCard("النقد بالدولار", Formatters.usd(snap?.treasury?.cashUsd ?: 0.0), "💵", SuccessGreen, Modifier.clickable2(onOpenTreasury)) }
                item { StatCard("النقد بالليرة", Formatters.syp(snap?.treasury?.cashSyp ?: 0.0), "💴", WarningAmber, Modifier.clickable2(onOpenTreasury)) }
                item { StatCard("النقد بالريال", Formatters.sar(snap?.treasury?.cashSar ?: 0.0), "💰", StatusBlue, Modifier.clickable2(onOpenTreasury)) }
                item { StatCard("Sham Cash دولار", Formatters.usd(snap?.treasury?.shamCashUsd ?: 0.0), "📱", PurpleAccent, Modifier.clickable2(onOpenTreasury)) }
                item { StatCard("Sham Cash ليرة", Formatters.syp(snap?.treasury?.shamCashSyp ?: 0.0), "📱", PurpleAccentLight, Modifier.clickable2(onOpenTreasury)) }
                item { StatCard("Sham Cash ريال", Formatters.sar(snap?.treasury?.shamCashSar ?: 0.0), "📱", StatusTeal, Modifier.clickable2(onOpenTreasury)) }
                item { StatCard("مستحقات العملاء", Formatters.usd(snap?.customerReceivablesUsd ?: 0.0), "👥", StatusBlue, Modifier.clickable2(onOpenCustomersWithBalance)) }
                item { StatCard("المستحق للوكيلة", Formatters.usd(snap?.agentPayableUsd ?: 0.0), "🏢", DangerRed, Modifier.clickable2(onOpenAgent)) }
                item { StatCard("طلبات وصلت ولم تُسلّم", (snap?.arrivedNotDeliveredCount ?: 0).toString(), "📦", StatusTeal, Modifier.clickable2(onOpenArrivedOrders)) }
                item { StatCard("الربح المحقق", Formatters.usd(snap?.realizedProfitUsd ?: 0.0), "💰", SuccessGreen) }
            }
        }

        if ((snap?.arrivedNotDeliveredCount ?: 0) > 0 || (snap?.customersWithBalanceCount ?: 0) > 0 || (snap?.agentPayableUsd ?: 0.0) > 0.5) {
            item { SectionTitle("يحتاج متابعة") }
            item {
                Card(
                    shape = AppShapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                ) {
                    Column(Modifier.padding(6.dp)) {
                        if ((snap?.arrivedNotDeliveredCount ?: 0) > 0) {
                            FollowUpRow("📦 لديك ${snap?.arrivedNotDeliveredCount} طلبات وصلت ولم تُسلَّم بعد", StatusTeal, onClick = onOpenArrivedOrders)
                        }
                        if ((snap?.shippingCount ?: 0) > 0) {
                            FollowUpRow("🚚 لديك ${snap?.shippingCount} طلبات قيد الشحن", WarningAmber)
                        }
                        if ((snap?.customersWithBalanceCount ?: 0) > 0) {
                            FollowUpRow("👥 لديك ${snap?.customersWithBalanceCount} عميلات عليهن مبالغ", DangerRed, onClick = onOpenCustomersWithBalance)
                        }
                        if ((snap?.agentPayableUsd ?: 0.0) > 0.5) {
                            FollowUpRow("🏢 المستحق للوكيلة ${Formatters.usd(snap?.agentPayableUsd ?: 0.0)}", PurpleAccent, onClick = onOpenAgent)
                        }
                    }
                }
            }
        }

        item { SectionTitle("الحركات الأخيرة") }
        if (state.recentTransactions.isEmpty()) {
            item { EmptyState("لا توجد حركات بعد") }
        } else {
            items(state.recentTransactions) { tx ->
                RecentTxRow(tx)
            }
        }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
        Text(value, color = color, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecentTxRow(tx: com.hasabati.app.data.db.entities.Transaction) {
    val isPositive = tx.type == TransactionType.CUSTOMER_PAYMENT || tx.type == TransactionType.CAPITAL_ADDITION ||
        tx.type == TransactionType.CAPITAL_INITIAL || tx.type == TransactionType.TRANSFER_IN
    val color = if (isPositive) SuccessGreen else DangerRed
    val sign = if (isPositive) "+" else "-"
    TransactionRow(
        title = tx.type.arabicLabel,
        subtitle = Formatters.dateTime(tx.createdAt),
        note = tx.note.takeIf { it.isNotBlank() },
        amountText = "$sign${Formatters.amount(tx.amount, tx.currency)}",
        amountColor = color,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun Modifier.clickable2(onClick: () -> Unit): Modifier =
    this.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick)
