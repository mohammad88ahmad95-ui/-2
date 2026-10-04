package com.hasabati.app.ui.common

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val usdFormat = NumberFormat.getNumberInstance(Locale("en", "US")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    private val sypFormat = NumberFormat.getNumberInstance(Locale("en", "US")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }
    private val sarFormat = NumberFormat.getNumberInstance(Locale("en", "US")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("ar"))
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale("ar"))

    fun usd(amount: Double): String = "${usdFormat.format(amount)}$"
    fun syp(amount: Double): String = "${sypFormat.format(amount)} ل.س"
    fun sar(amount: Double): String = "${sarFormat.format(amount)} ر.س"
    fun amount(amount: Double, currency: com.hasabati.app.data.db.entities.Currency): String = when (currency) {
        com.hasabati.app.data.db.entities.Currency.USD -> usd(amount)
        com.hasabati.app.data.db.entities.Currency.SYP -> syp(amount)
        com.hasabati.app.data.db.entities.Currency.SAR -> sar(amount)
    }
    fun date(millis: Long): String = dateFormat.format(Date(millis))
    fun dateTime(millis: Long): String = dateTimeFormat.format(Date(millis))
}
