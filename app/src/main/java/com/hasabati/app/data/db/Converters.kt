package com.hasabati.app.data.db

import androidx.room.TypeConverter
import com.hasabati.app.data.db.entities.Currency
import com.hasabati.app.data.db.entities.OrderStatus
import com.hasabati.app.data.db.entities.PaymentMethod
import com.hasabati.app.data.db.entities.TransactionType

class Converters {
    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name
    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)

    @TypeConverter
    fun fromCurrency(value: Currency): String = value.name
    @TypeConverter
    fun toCurrency(value: String): Currency = Currency.valueOf(value)

    @TypeConverter
    fun fromMethod(value: PaymentMethod): String = value.name
    @TypeConverter
    fun toMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)

    @TypeConverter
    fun fromTxType(value: TransactionType): String = value.name
    @TypeConverter
    fun toTxType(value: String): TransactionType = TransactionType.valueOf(value)
}
