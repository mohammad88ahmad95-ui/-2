package com.hasabati.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hasabati.app.data.db.dao.CustomerDao
import com.hasabati.app.data.db.dao.OrderDao
import com.hasabati.app.data.db.dao.TransactionDao
import com.hasabati.app.data.db.entities.Customer
import com.hasabati.app.data.db.entities.Order
import com.hasabati.app.data.db.entities.OrderItem
import com.hasabati.app.data.db.entities.Transaction

@Database(
    entities = [Customer::class, Order::class, OrderItem::class, Transaction::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hasabati.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
