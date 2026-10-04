package com.hasabati.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId")]
)
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productName: String,
    val quantity: Int,
    val expectedUnitCostUsd: Double,
    val actualUnitCostUsd: Double? = null,
    val unitSalePriceUsd: Double
) {
    val expectedCostTotal: Double get() = quantity * expectedUnitCostUsd
    val actualCostTotal: Double? get() = actualUnitCostUsd?.let { it * quantity }
    val saleTotal: Double get() = quantity * unitSalePriceUsd
}
