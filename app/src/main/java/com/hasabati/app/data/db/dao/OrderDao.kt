package com.hasabati.app.data.db.dao

import androidx.room.*
import com.hasabati.app.data.db.entities.Order
import com.hasabati.app.data.db.entities.OrderItem
import com.hasabati.app.data.db.entities.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    suspend fun getAllOnce(): List<Order>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun observeForCustomer(customerId: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt ASC")
    suspend fun getForCustomerOnce(customerId: Long): List<Order>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeById(id: Long): Flow<Order?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getById(id: Long): Order?

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    fun observeByStatus(status: OrderStatus): Flow<List<Order>>

    @Query("SELECT COUNT(*) FROM orders WHERE orderNumber = :orderNumber")
    suspend fun countByOrderNumber(orderNumber: String): Int

    @Query("SELECT MAX(CAST(SUBSTR(orderNumber, 5) AS INTEGER)) FROM orders WHERE orderNumber LIKE 'ORD-%'")
    suspend fun maxOrderSequence(): Int?

    @Insert
    suspend fun insert(order: Order): Long

    @Update
    suspend fun update(order: Order)

    // -- عناصر الطلب --
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun observeItems(orderId: Long): Flow<List<OrderItem>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItems(orderId: Long): List<OrderItem>

    @Insert
    suspend fun insertItems(items: List<OrderItem>): List<Long>

    @Update
    suspend fun updateItem(item: OrderItem)

    @Update
    suspend fun updateItems(items: List<OrderItem>)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteItemsForOrder(orderId: Long)

    // -- تقارير / لوحة التحكم --
    @Query("SELECT * FROM orders WHERE status IN ('ARRIVED','READY_FOR_DELIVERY') ORDER BY createdAt DESC")
    fun observeArrivedNotDelivered(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE status = 'SHIPPING' ORDER BY createdAt DESC")
    fun observeShipping(): Flow<List<Order>>
}
