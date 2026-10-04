package com.hasabati.app.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "hasabati_settings")

/** إعدادات بسيطة: تفعيل قفل PIN ورمزه (القسم 33) — تُخزَّن محلياً على الجهاز فقط. */
object PinPrefs {
    private val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
    private val PIN_CODE = stringPreferencesKey("pin_code")

    fun isEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[PIN_ENABLED] ?: false }

    fun pinCode(context: Context): Flow<String> =
        context.dataStore.data.map { it[PIN_CODE] ?: "" }

    suspend fun setPin(context: Context, enabled: Boolean, code: String) {
        context.dataStore.edit {
            it[PIN_ENABLED] = enabled
            it[PIN_CODE] = code
        }
    }
}
