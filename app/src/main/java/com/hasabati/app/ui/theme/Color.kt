package com.hasabati.app.ui.theme

import androidx.compose.ui.graphics.Color

// هوية بصرية أخف وأكثر راحة للعين — بنفسجي/نيلي دافئ بدل الكحلي الداكن الثقيل،
// مع تباين واضح للأرقام وخلفية فاتحة مريحة للاستخدام لفترات طويلة.
val IndigoDeep = Color(0xFF4B3FBF)      // أساس التدرّج الداكن (بدل الكحلي/الأسود)
val IndigoSoft = Color(0xFF7C5CFC)      // نفس اللون البنفسجي الأساسي (متوافق مع الاسم القديم)
val NavyDark = IndigoDeep
val NavySurface = IndigoDeep            // يبقى الاسم للتوافق مع الشاشات القديمة، لكن بقيمة أخف
val PurpleAccent = Color(0xFF6C5CE7)
val PurpleAccentLight = Color(0xFFB3A6FF)
val BackgroundLight = Color(0xFFF7F7FC)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextPrimaryDark = Color(0xFF20213A)
val TextSecondaryGray = Color(0xFF8A8AA3)
val SuccessGreen = Color(0xFF1FAE7A)
val DangerRed = Color(0xFFE6577A)
val WarningAmber = Color(0xFFF0A63B)
val BorderGray = Color(0xFFEDEDF7)

// ألوان التدرّج المستخدمة في بطاقات "الوضع المالي" الرئيسية
val GradientStart = Color(0xFF7C5CFC)
val GradientEnd = Color(0xFF4B3FBF)

// ================== إضافات Design System (تصميم فقط — لا تغييرات منطقية) ==================

// ألوان حالات كانت متكررة كقيم hex ثابتة داخل عدة شاشات (StatusExt, Dashboard, Treasury).
// تم تسميتها هنا بنفس القيم تماماً لضمان عدم تغيير أي لون ظاهر، فقط لتوحيد المصدر ومنع التكرار.
val StatusBlue = Color(0xFF2E86DE)   // "وصل" / معلومات
val StatusTeal = Color(0xFF0FA3B1)   // "جاهز للتسليم" / حيادي-إيجابي

// نسخ فاتحة من الأخضر/الأحمر تُستخدم فوق خلفيات داكنة (بطاقات التدرّج) لضمان تباين مريح للعين
// بدل الأبيض/الأحمر الصريح — نفس القيم المستخدمة سابقاً في OrderDetailScreen، فقط موحّدة هنا.
val OnGradientText = Color.White
val OnGradientSuccess = Color(0xFF9FE8B5)
val OnGradientDanger = Color(0xFFF3B6B6)

// خلفية بنفسجية فاتحة جداً تُستخدم لخلفيات الشرائح/الأيقونات الدائرية بدل alpha متفرق في كل شاشة
val SurfaceLavender = Color(0xFFF1EEFF)
