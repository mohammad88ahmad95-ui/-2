package com.hasabati.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * الطلب. التكاليف والمبيعات الإجمالية تُخزَّن كحقول مُحسوبة (cache) من مجموع
 * عناصر الطلب [OrderItem] حفاظاً على الأداء، ويتم تحديثها عند كل تعديل على العناصر.
 */
@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("customerId"), Index("orderNumber", unique = true)]
)
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val customerId: Long,
    val status: OrderStatus = OrderStatus.CONFIRMED,
    val createdAt: Long = System.currentTimeMillis(),
    val receivedAt: Long? = null,      // تاريخ وصول الطلب (تأكيد التكلفة الفعلية)
    val deliveredAt: Long? = null,     // تاريخ التسليم
    val cancelledAt: Long? = null,
    val expectedCostUsd: Double = 0.0, // مجموع تكلفة الشراء المتوقعة لكل المنتجات
    val actualCostUsd: Double? = null, // مجموع التكلفة الفعلية — null حتى يصل الطلب
    val saleTotalUsd: Double = 0.0,    // مجموع سعر البيع لكل المنتجات
    val depositRefunded: Boolean? = null, // عند الإلغاء: هل تم رد العربون؟ null = لا يوجد عربون أصلاً
    val notes: String = ""
)
