package com.hasabati.app.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hasabati.app.ui.common.*
import com.hasabati.app.ui.theme.*

@Composable
fun OrdersListScreen(onOpenOrder: (Long) -> Unit, onNewOrder: () -> Unit) {
    val vm = hasabatiViewModel { OrdersListViewModel(it) }
    val rows by vm.rows.collectAsState()
    val filter by vm.filter.collectAsState()
    val query by vm.query.collectAsState()

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(onClick = onNewOrder, containerColor = PurpleAccent, shape = AppShapes.large) {
                Icon(Icons.Filled.Add, contentDescription = "طلب جديد")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(BackgroundLight)) {
            Column(Modifier.padding(16.dp)) {
                Text("الطلبات", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Spacer(Modifier.height(12.dp))
                SearchBarField(
                    value = query,
                    onValueChange = { vm.setQuery(it) },
                    placeholder = "ابحث برقم الطلب أو اسم العميلة"
                )
            }
            LazyRowFilters(filter) { vm.setFilter(it) }
            Spacer(Modifier.height(6.dp))
            if (rows.isEmpty()) {
                EmptyState("لا توجد طلبات مطابقة", icon = Icons.Filled.Inventory2)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(rows) { row ->
                        OrderRowCard(row, onClick = { onOpenOrder(row.order.id) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun LazyRowFilters(selected: OrdersListViewModel.Filter, onSelect: (OrdersListViewModel.Filter) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(OrdersListViewModel.Filter.entries) { f ->
            FilterChip(
                selected = f == selected,
                onClick = { onSelect(f) },
                label = { Text(f.label) },
                shape = AppShapes.pill,
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PurpleAccent.copy(alpha = 0.16f))
            )
        }
    }
}

@Composable
private fun OrderRowCard(row: OrdersListViewModel.OrderRow, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth()) {
            // أيقونة بديلة بدل صورة منتج حقيقية (لا نملك صوراً فعلية للمنتجات)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(row.order.status.color().copy(alpha = 0.12f), AppShapes.small),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(Icons.Filled.Inventory2, contentDescription = null, tint = row.order.status.color())
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(row.order.orderNumber, fontWeight = FontWeight.Bold)
                        Text(row.customerName, color = TextSecondaryGray, style = MaterialTheme.typography.bodyMedium)
                    }
                    StatusChip(row.order.status.arabicLabel, row.order.status.color())
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("الإجمالي", color = TextSecondaryGray, style = MaterialTheme.typography.bodySmall)
                        Text(Formatters.usd(row.order.saleTotalUsd), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column {
                        Text("المدفوع", color = TextSecondaryGray, style = MaterialTheme.typography.bodySmall)
                        Text(Formatters.usd(row.paidUsd), fontWeight = FontWeight.SemiBold, color = SuccessGreen, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column {
                        Text("المتبقي", color = TextSecondaryGray, style = MaterialTheme.typography.bodySmall)
                        Text(
                            Formatters.usd(row.remainingUsd),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (row.remainingUsd > 0.009) DangerRed else SuccessGreen
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(Formatters.date(row.order.createdAt), color = TextSecondaryGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
