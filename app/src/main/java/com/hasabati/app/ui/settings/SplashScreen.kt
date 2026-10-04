package com.hasabati.app.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hasabati.app.R
import com.hasabati.app.ui.common.PrimaryButton
import com.hasabati.app.ui.theme.GradientEnd
import com.hasabati.app.ui.theme.GradientStart

/** شاشة ترحيب تظهر عند فتح التطبيق (تصميم فقط). */
@Composable
fun SplashScreen(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(160.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("حساباتي", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("كل حساب .. بمكان واحد", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(40.dp))
            PrimaryButton(
                text = "ابدأ الآن",
                onClick = onStart,
                containerColor = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
