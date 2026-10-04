package com.hasabati.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hasabati.app.ui.theme.BorderGray
import com.hasabati.app.ui.theme.PurpleAccent

@Composable
fun MoreScreen(
    onOpenAgent: () -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("المزيد", style = MaterialTheme.typography.headlineMedium) }
        item { MoreItem("حساب الوكيلة", "متابعة المستحق والمدفوع للوكيلة", Icons.Filled.LocalShipping, onOpenAgent) }
        item { MoreItem("المصاريف", "تسجيل ومتابعة المصاريف التجارية", Icons.Filled.Receipt, onOpenExpenses) }
        item { MoreItem("التقارير", "المبيعات، الأرباح، والمصاريف حسب الفترة", Icons.Filled.BarChart, onOpenReports) }
        item { MoreItem("الإعدادات", "رأس المال، النسخ الاحتياطي، الحماية", Icons.Filled.Settings, onOpenSettings) }
    }
}

@Composable
private fun MoreItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = PurpleAccent)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = com.hasabati.app.ui.theme.TextSecondaryGray, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = com.hasabati.app.ui.theme.TextSecondaryGray)
        }
    }
}
