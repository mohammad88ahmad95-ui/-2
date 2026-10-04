package com.hasabati.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.hasabati.app.ui.theme.DangerRed
import com.hasabati.app.ui.theme.GradientEnd
import com.hasabati.app.ui.theme.GradientStart
import com.hasabati.app.ui.theme.PurpleAccent

/** شاشة قفل بسيطة تظهر عند تفعيل PIN (القسم 33) */
@Composable
fun PinLockScreen(correctPin: String, onUnlocked: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(
            androidx.compose.ui.graphics.Brush.linearGradient(colors = listOf(GradientStart, GradientEnd))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("حساباتي", style = MaterialTheme.typography.headlineMedium, color = androidx.compose.ui.graphics.Color.White)
            Spacer(Modifier.height(8.dp))
            Text("أدخلي رمز PIN للمتابعة", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = input,
                onValueChange = { if (it.length <= 6) { input = it; error = false } },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                isError = error,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedTextColor = androidx.compose.ui.graphics.Color.White
                )
            )
            if (error) {
                Spacer(Modifier.height(6.dp))
                Text("رمز غير صحيح", color = DangerRed)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { if (input == correctPin) onUnlocked() else error = true },
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
            ) { Text("دخول") }
        }
    }
}
