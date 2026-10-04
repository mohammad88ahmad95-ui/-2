package com.hasabati.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = PurpleAccent,
    onPrimary = SurfaceWhite,
    primaryContainer = PurpleAccentLight,
    secondary = NavySurface,
    background = BackgroundLight,
    surface = SurfaceWhite,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    error = DangerRed,
    outline = BorderGray
)

val HasabatiTypography = Typography(
    // رقم مالي كبير جداً (مثال: "معي" في بطاقة الوضع المالي، الرصيد الكبير في الخزينة)
    displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp)
)

/**
 * مسافات موحّدة (Spacing tokens) — تُستخدم بدل أرقام dp متفرقة في كل شاشة،
 * حتى تكون المسافات بين العناصر متسقة بصرياً في كامل التطبيق.
 */
object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

/**
 * أشكال موحّدة (Corner radius tokens) — تُستخدم بدل RoundedCornerShape(x.dp) متكرر يدوياً.
 */
object AppShapes {
    val small = RoundedCornerShape(12.dp)
    val medium = RoundedCornerShape(16.dp)
    val large = RoundedCornerShape(20.dp)
    val xlarge = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(50)
}

@Composable
fun HasabatiTheme(content: @Composable () -> Unit) {
    // نستخدم نفس النسق دائماً (فاتح) للحفاظ على وضوح الأرقام المالية كما طُلب في القسم 28
    // ملاحظة: بنية الألوان والـ Typography أعلاه مبنية بحيث يسهل إضافة نسق داكن لاحقاً
    // (بإضافة darkColorScheme واستخدام isSystemInDarkTheme()) دون كسر أي شاشة حالية.
    MaterialTheme(
        colorScheme = LightColors,
        typography = HasabatiTypography,
        content = content
    )
}
