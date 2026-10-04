package com.hasabati.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hasabati.app.ui.theme.AppShapes
import com.hasabati.app.ui.theme.AppSpacing
import com.hasabati.app.ui.theme.BorderGray
import com.hasabati.app.ui.theme.PurpleAccent
import com.hasabati.app.ui.theme.SurfaceWhite
import com.hasabati.app.ui.theme.TextPrimaryDark
import com.hasabati.app.ui.theme.TextSecondaryGray

// ============================================================================================
// المكوّنات الأساسية (كما كانت — لم يتغيّر أي توقيع/باراميتر مطلوب هنا لضمان عدم كسر أي شاشة)
// ============================================================================================

@Composable
fun StatCard(
    title: String,
    value: String,
    emoji: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = androidx.compose.ui.unit.TextUnit.Unspecified)
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryGray)
            }
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = accentColor)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryGray)
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun StatusChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * حالة فارغة — تم توسيعها باراميترات اختيارية فقط (icon / actionLabel / onAction)
 * كلها افتراضية null، لذلك أي استدعاء قديم مثل EmptyState("لا توجد حركات بعد")
 * يستمر بالعمل دون أي تعديل.
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(TextSecondaryGray.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TextSecondaryGray.copy(alpha = 0.6f))
            }
            Spacer(Modifier.height(12.dp))
        }
        Text(
            message,
            color = TextSecondaryGray,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            PrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

/**
 * بطاقة تدرّج لونية أنيقة (بنفسجي → نيلي) تُستخدم لأهم رقم بكل شاشة (الوضع المالي،
 * رصيد العميلة، حساب الوكيلة...) بدل الصندوق الداكن الثقيل السابق.
 */
@Composable
fun GradientHeroCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(com.hasabati.app.ui.theme.GradientStart, com.hasabati.app.ui.theme.GradientEnd)
                    )
                )
                .padding(20.dp),
            content = content
        )
    }
}

// ============================================================================================
// مكوّنات جديدة — Design System موحّد (تصميم فقط، لا منطق) — القسم 30 من متطلبات إعادة التصميم
// ============================================================================================

/**
 * زر أساسي موحّد لكل التطبيق (إجراء رئيسي: حفظ، تأكيد، تحصيل دفعة...).
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = PurpleAccent
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.small,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * زر ثانوي موحّد (إجراء بديل أو إلغاء: "المزيد"، "إلغاء الطلب"...).
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    contentColor: Color = PurpleAccent
) {
    OutlinedButton(
        onClick = onClick,
        shape = AppShapes.small,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * نص مبلغ مالي موحّد الشكل (وزن الخط والحجم) في كل شاشة تعرض أرقاماً مالية،
 * بدل تكرار fontWeight = Bold يدوياً في كل مكان.
 */
@Composable
fun MoneyText(
    amount: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimaryDark,
    style: TextStyle = MaterialTheme.typography.headlineMedium
) {
    Text(amount, color = color, fontWeight = FontWeight.Bold, style = style, modifier = modifier)
}

/**
 * شريط بحث موحّد (يُستخدم في الطلبات، العملاء، وأي قائمة قابلة للبحث لاحقاً).
 */
@Composable
fun SearchBarField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextSecondaryGray) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondaryGray) },
        singleLine = true,
        shape = AppShapes.pill,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = BorderGray,
            focusedBorderColor = PurpleAccent
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * إجراء سريع موحّد (نفس شكل الأزرار الأربعة في الرئيسية) — أصبح مكوّناً مشتركاً بدل خاص
 * بشاشة الرئيسية فقط، حتى يمكن إعادة استخدامه في شاشة "المزيد" وغيرها.
 */
@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderGray),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                color = TextPrimaryDark,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * صف حركة مالية عام — يوحّد شكل "آخر الحركات" في الرئيسية، وسجل حركات الخزينة،
 * وسجل عمليات الطلب، بدل ثلاث نسخ شبه متطابقة من نفس الـ Card.
 */
@Composable
fun TransactionRow(
    title: String,
    subtitle: String,
    amountText: String,
    amountColor: Color,
    modifier: Modifier = Modifier,
    note: String? = null
) {
    Card(
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderGray),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryGray)
                if (!note.isNullOrBlank()) {
                    Text(note, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryGray)
                }
            }
            MoneyText(amountText, color = amountColor, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * سطر "يحتاج متابعة" موحّد: نقطة ملوّنة + نص، بدل نص عادي بدون تمييز بصري للأولوية.
 * الألوان هنا تصميمية بحتة ولا علاقة لها بأي حساب أو منطق.
 */
@Composable
fun FollowUpRow(text: String, dotColor: Color, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clip(AppShapes.small) else Modifier)
            .then(if (onClick != null) Modifier.background(dotColor.copy(alpha = 0.06f)) else Modifier)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 10.dp, horizontal = if (onClick != null) 10.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(dotColor)
        )
        Spacer(Modifier.width(AppSpacing.sm))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextPrimaryDark)
    }
}

// ============================================================================================
// مكوّنات إضافية جديدة (هذه الرسالة) — لدعم تصميم العملاء/الوكيلة/المصاريف/الإعدادات
// الجديد المطابق للصورة المرجعية: أفاتار دائري، أيقونة فئة ملونة، وشريط تقدّم بسيط.
// ============================================================================================

/**
 * أفاتار دائري بحرف أول من الاسم — بديل بصري بسيط لصورة العميلة (لا نملك صور حقيقية).
 */
@Composable
fun InitialAvatar(name: String, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 44.dp, color: Color = PurpleAccent) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.trim().firstOrNull()?.uppercase() ?: "?",
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * أيقونة دائرية ملوّنة لفئة (مصروف، إجراء...) — بديل بصري موحّد بدل إيموجي حر.
 */
@Composable
fun CategoryIcon(icon: ImageVector, color: Color, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 40.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color)
    }
}

/**
 * صف عنوان + شريط تقدّم بسيط + قيمة — يُستخدم في تفصيل "المدفوع حسب العملة" بحساب الوكيلة.
 */
@Composable
fun ProgressStatRow(
    label: String,
    valueText: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryGray)
            Text(valueText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            color = color,
            trackColor = color.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
        )
    }
}

/**
 * عنصر قائمة بسيط للإعدادات: أيقونة + عنوان + عنوان فرعي اختياري + سهم.
 */
@Composable
fun SettingsListItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconColor: Color = PurpleAccent,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CategoryIcon(icon, iconColor)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryGray)
                }
            }
            Icon(androidx.compose.material.icons.Icons.Filled.ChevronLeft, contentDescription = null, tint = TextSecondaryGray)
        }
    }
}
