package com.hasabati.app.data.db.dao

import androidx.room.*
import com.hasabati.app.data.db.entities.Transaction
import com.hasabati.app.data.db.entities.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY createdAt ASC")
    fun observeForCustomer(customerId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY createdAt ASC")
    suspend fun getForCustomerOnce(customerId: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE orderId = :orderId ORDER BY createdAt ASC")
    fun observeForOrder(orderId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY createdAt DESC")
    fun observeByType(type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE createdAt BETWEEN :from AND :to ORDER BY createdAt DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Query("SELECT * FROM transactions")
    suspend fun getAllOnce(): List<Transaction>
}
