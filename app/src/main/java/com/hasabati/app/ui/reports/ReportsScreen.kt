package com.hasabati.app.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hasabati.app.ui.common.*
import com.hasabati.app.ui.theme.*

@Composable
fun ReportsScreen() {
    val vm = hasabatiViewModel { ReportsViewModel(it) }
    val state by vm.uiState.collectAsState()
    val period by vm.period.collectAsState()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("التقارير", style = MaterialTheme.typography.headlineMedium) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportsViewModel.Period.values().forEach { p ->
                    FilterChip(selected = period == p, onClick = { vm.setPeriod(p) }, label = { Text(p.label) })
                }
            }
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(430.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { StatCard("إجمالي المبيعات", Formatters.usd(state.salesUsd), "🧮", Color2.blue) }
                item { StatCard("التكلفة", Formatters.usd(state.costUsd), "📦", WarningAmber) }
                item { StatCard("الربح", Formatters.usd(state.profitUsd), "💰", SuccessGreen) }
                item { StatCard("المصاريف", Formatters.usd(state.expensesUsd), "🧾", DangerRed) }
                item { StatCard("السحوبات الشخصية", Formatters.usd(state.withdrawalsUsd), "🏠", Color2.blue) }
                item { StatCard("المدفوع للوكيلة", Formatters.usd(state.agentPaidUsd), "🏢", PurpleAccent) }
                item { StatCard("عدد الطلبات", state.ordersCount.toString(), "📑", PurpleAccentLight) }
                item { StatCard("عدد العميلات", state.customersCount.toString(), "👥", Color2.blue) }
            }
        }
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = BackgroundLight), border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)) {
                Text(
                    "تنبيه: الربح هنا رقم محاسبي، وهو ليس نفسه النقد الموجود فعلياً في الخزينة.",
                    modifier = Modifier.padding(14.dp), color = TextSecondaryGray
                )
            }
        }
        item { Spacer(Modifier.height(60.dp)) }
    }
}

private object Color2 {
    val blue = androidx.compose.ui.graphics.Color(0xFF2E86DE)
}
