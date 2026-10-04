package com.hasabati.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.PaymentMethod
import com.hasabati.app.ui.common.*
import com.hasabati.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen() {
    val repo = repository()
    val backup = backupManager()
    val vm: SettingsViewModel = viewModel(factory = SimpleViewModelFactory { SettingsViewModel(repo, backup) })
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showAddCapitalDialog by remember { mutableStateOf(false) }
    var showWithdrawalDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    val pinEnabled by PinPrefs.isEnabled(context).collectAsState(initial = false)

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val tempFile = File(context.cacheDir, "import_backup.json")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            vm.importBackup(tempFile) { }
        }
    }

    LaunchedEffect(state.message) {
        // يمكن ربطها بـ Snackbar لاحقاً؛ نتركها كرسالة نصية بسيطة أسفل الشاشة
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("الإعدادات", style = MaterialTheme.typography.headlineMedium) }

        item { SectionTitle("رأس المال") }
        item {
            GradientHeroCard(modifier = Modifier.fillMaxWidth()) {
                CapitalRow("رأس المال الابتدائي", Formatters.usd(state.capital.initial))
                CapitalRow("الإضافات", Formatters.usd(state.capital.additions))
                CapitalRow("السحوبات الشخصية", Formatters.usd(state.capital.withdrawals))
                Divider(Modifier.padding(vertical = 8.dp), color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f))
                CapitalRow("الصافي", Formatters.usd(state.capital.netCapital), bold = true)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { showAddCapitalDialog = true }, modifier = Modifier.weight(1f)) { Text("إضافة رأس مال") }
                OutlinedButton(onClick = { showWithdrawalDialog = true }, modifier = Modifier.weight(1f)) { Text("سحب شخصي") }
            }
        }

        item { SectionTitle("النسخ الاحتياطي") }
        item {
            Card(shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)) {
                Column(Modifier.padding(14.dp)) {
                    Text("احتفظي بنسخة من كل بياناتك، أو استعيديها عند تغيير الهاتف.", color = TextSecondaryGray, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                vm.exportBackup { file ->
                                    if (file != null) {
                                        val uri = FileProvider.getUriForFile(context, "com.hasabati.app.fileprovider", file)
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "مشاركة النسخة الاحتياطية"))
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                            modifier = Modifier.weight(1f)
                        ) { Text("تصدير نسخة") }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f)
                        ) { Text("استعادة نسخة") }
                    }
                    if (state.isWorking) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    if (state.message != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(state.message ?: "", color = SuccessGreen, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item { SectionTitle("الحماية") }
        item {
            Card(shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)) {
                Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("قفل برمز PIN")
                        Text("اختياري — لحماية دخول التطبيق", color = TextSecondaryGray, style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = pinEnabled, onCheckedChange = { checked ->
                        if (checked) showPinDialog = true
                        else scope.launch { PinPrefs.setPin(context, false, "") }
                    })
                }
            }
        }

        item { Spacer(Modifier.height(70.dp)) }
    }

    if (showAddCapitalDialog) {
        CapitalDialog(
            title = "إضافة رأس مال",
            isInitialOption = true,
            onDismiss = { showAddCapitalDialog = false },
            onSave = { isInitial, amount, currency, method, rate ->
                vm.addCapital(isInitial, amount, currency, method, rate, if (isInitial) "رأس مال ابتدائي" else "إضافة رأس مال")
                showAddCapitalDialog = false
            }
        )
    }
    if (showWithdrawalDialog) {
        CapitalDialog(
            title = "سحب شخصي",
            isInitialOption = false,
            onDismiss = { showWithdrawalDialog = false },
            onSave = { _, amount, currency, method, rate ->
                vm.addPersonalWithdrawal(amount, currency, method, rate, "سحب شخصي")
                showWithdrawalDialog = false
            }
        )
    }
    if (showPinDialog) {
        SetPinDialog(
            onDismiss = { showPinDialog = false },
            onConfirm = { code ->
                scope.launch { PinPrefs.setPin(context, true, code) }
                showPinDialog = false
            }
        )
    }
}

@Composable
private fun CapitalRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f))
        Text(
            value, color = androidx.compose.ui.graphics.Color.White,
            fontWeight = if (bold) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun CapitalDialog(
    title: String,
    isInitialOption: Boolean,
    onDismiss: () -> Unit,
    onSave: (Boolean, Double, Currency, PaymentMethod, Double?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currency.USD) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    var rate by remember { mutableStateOf("") }
    var isInitial by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (isInitialOption) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isInitial, onCheckedChange = { isInitial = it })
                        Spacer(Modifier.width(8.dp))
                        Text("اعتبارها رأس مال ابتدائي")
                    }
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") }, modifier = Modifier.fillMaxWidth())
                Row(Modifier.padding(top = 8.dp)) {
                    Currency.values().forEach { c ->
                        FilterChip(selected = currency == c, onClick = { currency = c }, label = { Text(c.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                    }
                }
                Row(Modifier.padding(top = 8.dp)) {
                    PaymentMethod.values().forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m.arabicLabel) }, modifier = Modifier.padding(end = 8.dp))
                    }
                }
                if (currency != Currency.USD) {
                    OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("سعر الصرف (${currency.symbol} لكل 1$)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull() ?: return@TextButton
                onSave(isInitial, amt, currency, method, if (currency != Currency.USD) rate.toDoubleOrNull() else null)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun SetPinDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعيين رمز PIN") },
        text = {
            OutlinedTextField(
                value = code, onValueChange = { if (it.length <= 6) code = it },
                label = { Text("رمز مكوّن من 4 أرقام") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (code.length >= 4) onConfirm(code) }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
