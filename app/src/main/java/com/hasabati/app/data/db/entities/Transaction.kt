package com.hasabati.app.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * سجل العمليات المالية (Transaction Ledger) — القاعدة 2 و23:
 * كل حركة مالية في التطبيق، مهما كان نوعها، تُسجَّل هنا كعملية مستقلة.
 * لا شيء يُحذف نهائياً؛ التصحيح يتم بعملية عكسية جديدة (انظر Repository.reverseTransaction).
 *
 * [amount] هو المبلغ بعملته الأصلية [currency] كما تم دفعه فعلياً (القاعدة 8: لا نفقد المبلغ الأصلي).
 * [usdEquivalent] هو القيمة المحاسبية بالدولار المستخدمة لتخفيض الديون وحساب الأرصدة.
 * [signedUsdForTreasury] (محسوب في Repository وليس مخزَّناً) يحدد أثر العملية على الخزينة.
 */
@Entity(
    tableName = "transactions",
    indices = [Index("customerId"), Index("orderId"), Index("type"), Index("createdAt")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Double,               // بعملته الأصلية كما دُفع فعلياً
    val currency: Currency,
    val usdEquivalent: Double,        // القيمة المحاسبية بالدولار
    val exchangeRate: Double? = null, // مطلوب فقط عندما currency = SYP
    val method: PaymentMethod,
    val customerId: Long? = null,
    val orderId: Long? = null,
    val expenseCategory: String? = null,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reversedByTransactionId: Long? = null, // إن كانت هذه العملية أُلغيت بعملية عكسية
    val isReversal: Boolean = false            // هل هذه العملية نفسها عملية عكسية/تصحيحية
)
