package com.hasabati.app.license

import android.content.Context
import android.provider.Settings
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import android.util.Base64

/**
 * بيانات الدفع والتواصل — تظهر في شاشة التفعيل.
 */
object LicenseConfig {
    const val PRICE_TEXT = "10 دولار"
    const val WALLET_NAME = "محمد عبدالحميد احمد"
    const val WALLET_CODE = "68b28b37272e348d615bd38a1570342e"

    // اتركيها فارغة "" إذا لا تريد إظهار وسيلة تواصل. مثال: "واتساب: 09xxxxxxxx"
    const val CONTACT_INFO = ""
}

/**
 * نظام التفعيل (بدون سيرفر):
 *  - كل جهاز له "معرّف جهاز" ثابت.
 *  - صاحب التطبيق يولّد "كود تفعيل" لهذا المعرّف فقط، موقّعاً بمفتاح خاص (ECDSA P-256) لا يوجد داخل التطبيق.
 *  - التطبيق يحتوي المفتاح العام فقط، فيتحقق من صحة الكود ولا يستطيع أحد تزويره.
 */
object LicenseManager {
    private const val PREFS = "hasabati_license"
    private const val KEY_CODE = "activation_code"

    // المفتاح العام (X.509 / Base64) — آمن ليكون داخل التطبيق
    private const val PUBLIC_KEY_B64 =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEs4ehSaovIO9Vp69GIzs6m065RxF7ziCVWEZGIdtej30qgv9lvADGWZGZrbmibIapu1Ww+kSx3LrLu65OUNqkuA=="

    private const val B32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    /** معرّف الجهاز بصيغة 16 حرفاً (أرقام وحروف A-F) بدون فواصل. */
    private fun rawDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(("hasabati-device:" + androidId).toByteArray(Charsets.UTF_8))
        val sb = StringBuilder()
        for (i in 0 until 8) {
            sb.append(String.format("%02X", digest[i].toInt() and 0xFF))
        }
        return sb.toString()
    }

    /** معرّف الجهاز للعرض بصيغة XXXX-XXXX-XXXX-XXXX */
    fun displayDeviceId(context: Context): String =
        rawDeviceId(context).chunked(4).joinToString("-")

    /** هل التطبيق مفعّل على هذا الجهاز؟ (يعيد التحقق من الكود المحفوظ في كل مرة) */
    fun isActivated(context: Context): Boolean {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_CODE, null) ?: return false
        return verify(context, saved)
    }

    /** يحاول تفعيل التطبيق بالكود المُدخل. يعيد true عند النجاح ويحفظ الكود. */
    fun activate(context: Context, code: String): Boolean {
        val clean = normalizeCode(code)
        if (!verify(context, clean)) return false
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_CODE, clean).apply()
        return true
    }

    private fun normalizeCode(code: String): String =
        code.uppercase().filter { B32.indexOf(it) >= 0 }

    private fun verify(context: Context, code: String): Boolean {
        return try {
            val raw = base32Decode(normalizeCode(code)) ?: return false
            if (raw.size < 64) return false
            val message = ("HASABATI:" + rawDeviceId(context)).toByteArray(Charsets.UTF_8)
            val keyBytes = Base64.decode(PUBLIC_KEY_B64, Base64.DEFAULT)
            val publicKey = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(keyBytes))
            val sig = Signature.getInstance("SHA256withECDSA")
            sig.initVerify(publicKey)
            sig.update(message)
            sig.verify(rawToDer(raw.copyOfRange(0, 64)))
        } catch (e: Exception) {
            false
        }
    }

    private fun base32Decode(s: String): ByteArray? {
        val out = java.io.ByteArrayOutputStream()
        var buffer = 0
        var bits = 0
        for (ch in s) {
            val v = B32.indexOf(ch)
            if (v < 0) return null
            buffer = (buffer shl 5) or v
            bits += 5
            if (bits >= 8) {
                out.write((buffer shr (bits - 8)) and 0xFF)
                bits -= 8
                buffer = buffer and ((1 shl bits) - 1)
            }
        }
        return out.toByteArray()
    }

    /** يحوّل توقيع (r||s) بطول 64 بايت إلى صيغة DER التي تفهمها مكتبة جافا. */
    private fun rawToDer(raw: ByteArray): ByteArray {
        val r = derInt(raw.copyOfRange(0, 32))
        val s = derInt(raw.copyOfRange(32, 64))
        val body = r + s
        return byteArrayOf(0x30, body.size.toByte()) + body
    }

    private fun derInt(x: ByteArray): ByteArray {
        var start = 0
        while (start < x.size - 1 && x[start].toInt() == 0) start++
        var v = x.copyOfRange(start, x.size)
        if ((v[0].toInt() and 0x80) != 0) {
            v = byteArrayOf(0) + v
        }
        return byteArrayOf(0x02, v.size.toByte()) + v
    }
}
