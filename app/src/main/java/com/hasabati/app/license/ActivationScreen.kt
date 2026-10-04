package com.hasabati.app.license

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hasabati.app.R
import com.hasabati.app.ui.common.PrimaryButton
import com.hasabati.app.ui.common.SecondaryButton
import com.hasabati.app.ui.theme.BackgroundLight
import com.hasabati.app.ui.theme.DangerRed
import com.hasabati.app.ui.theme.GradientEnd
import com.hasabati.app.ui.theme.GradientStart
import com.hasabati.app.ui.theme.SurfaceLavender
import com.hasabati.app.ui.theme.SurfaceWhite
import com.hasabati.app.ui.theme.TextSecondaryGray

/** شاشة تظهر قبل استخدام التطبيق: دفع مرة واحدة عبر شام كاش ثم إدخال كود التفعيل. */
@Composable
fun ActivationScreen(onActivated: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val deviceId = remember { LicenseManager.displayDeviceId(context) }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    fun copyText(text: String) {
        clipboard.setText(AnnotatedString(text))
        Toast.makeText(context, "تم النسخ", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        // الترويسة
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.height(36.dp))
                Spacer(Modifier.height(8.dp))
                Text("تفعيل تطبيق حساباتي", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "للبدء باستخدام التطبيق: دفعة واحدة بقيمة ${LicenseConfig.PRICE_TEXT} عبر شام كاش",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // الخطوة 1: الدفع
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1) الدفع عبر شام كاش", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "امسح الرمز التالي من تطبيق شام كاش، أو انسخ رمز المحفظة وأرسل المبلغ إليها.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Image(
                        painter = painterResource(id = R.drawable.sham_cash_qr),
                        contentDescription = "رمز شام كاش",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .width(240.dp)
                            .aspectRatio(720f / 956f)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(LicenseConfig.WALLET_NAME, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            LicenseConfig.WALLET_CODE,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryGray
                        )
                        IconButton(onClick = { copyText(LicenseConfig.WALLET_CODE) }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ رمز المحفظة")
                        }
                    }
                    Text(
                        "المبلغ المطلوب: ${LicenseConfig.PRICE_TEXT}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GradientEnd
                    )
                }
            }

            // الخطوة 2: إرسال المعرّف
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2) إرسال الإيصال ومعرّف الجهاز", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "بعد الدفع، أرسل صورة إيصال الدفع مع معرّف جهازك التالي، وسيصلك كود التفعيل.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryGray
                    )
                    if (LicenseConfig.CONTACT_INFO.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(LicenseConfig.CONTACT_INFO, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceLavender)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(deviceId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { copyText(deviceId) }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ معرّف الجهاز")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    SecondaryButton(
                        text = "مشاركة طلب التفعيل",
                        leadingIcon = Icons.Filled.Share,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "طلب تفعيل تطبيق حساباتي\nمعرّف الجهاز: $deviceId\n(مرفق صورة إيصال الدفع)"
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "إرسال طلب التفعيل"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // الخطوة 3: إدخال الكود
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3) إدخال كود التفعيل", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it; error = false },
                        label = { Text("كود التفعيل") },
                        isError = error,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                val pasted = clipboard.getText()?.text ?: ""
                                code = pasted
                                error = false
                            }) {
                                Icon(Icons.Filled.ContentPaste, contentDescription = "لصق")
                            }
                        }
                    )
                    if (error) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "الكود غير صحيح أو غير مخصص لهذا الجهاز. تأكد من نسخه كاملاً.",
                            color = DangerRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = "تفعيل التطبيق",
                        enabled = code.isNotBlank(),
                        onClick = {
                            if (LicenseManager.activate(context, code)) {
                                onActivated()
                            } else {
                                error = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
