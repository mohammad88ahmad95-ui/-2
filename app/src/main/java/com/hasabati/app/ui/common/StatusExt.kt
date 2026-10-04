package com.hasabati.app.ui.common

import androidx.compose.ui.graphics.Color
import com.hasabati.app.data.db.entities.OrderStatus
import com.hasabati.app.ui.theme.DangerRed
import com.hasabati.app.ui.theme.PurpleAccent
import com.hasabati.app.ui.theme.StatusBlue
import com.hasabati.app.ui.theme.StatusTeal
import com.hasabati.app.ui.theme.SuccessGreen
import com.hasabati.app.ui.theme.WarningAmber

// نفس الألوان تماماً كما كانت (0xFF2E86DE و0xFF0FA3B1) لكن موحّدة الآن في مصدر واحد
// (StatusBlue / StatusTeal في Color.kt) بدل تكرارها كـ hex في كل شاشة تعرض حالة الطلب.
fun OrderStatus.color(): Color = when (this) {
    OrderStatus.CONFIRMED -> PurpleAccent
    OrderStatus.SHIPPING -> WarningAmber
    OrderStatus.ARRIVED -> StatusBlue
    OrderStatus.READY_FOR_DELIVERY -> StatusTeal
    OrderStatus.DELIVERED -> SuccessGreen
    OrderStatus.CANCELLED -> DangerRed
}

fun OrderStatus.next(): OrderStatus? = when (this) {
    OrderStatus.CONFIRMED -> OrderStatus.SHIPPING
    OrderStatus.SHIPPING -> OrderStatus.ARRIVED
    OrderStatus.ARRIVED -> OrderStatus.READY_FOR_DELIVERY
    OrderStatus.READY_FOR_DELIVERY -> OrderStatus.DELIVERED
    OrderStatus.DELIVERED -> null
    OrderStatus.CANCELLED -> null
}
