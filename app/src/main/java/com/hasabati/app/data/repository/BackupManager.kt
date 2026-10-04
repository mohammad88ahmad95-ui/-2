package com.hasabati.app.data.repository

import android.content.Context
import com.hasabati.app.data.db.AppDatabase
import com.hasabati.app.data.db.entities.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * نسخ احتياطي واستعادة كاملة لكل بيانات التطبيق كملف JSON واحد (القسم 32).
 * لا نستخدم مكتبات خارجية إضافية — org.json مدمجة في نظام أندرويد.
 */
class BackupManager(private val context: Context, private val db: AppDatabase) {

    suspend fun exportToFile(): File {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val customerList = db.customerDao().getAllOnce()
        val customers = JSONArray()
        customerList.forEach { c ->
            customers.put(JSONObject().apply {
                put("id", c.id); put("name", c.name); put("phone", c.phone)
                put("notes", c.notes); put("createdAt", c.createdAt)
            })
        }
        root.put("customers", customers)

        val orderList = db.orderDao().getAllOnce()
        val orders = JSONArray()
        orderList.forEach { o ->
            orders.put(JSONObject().apply {
                put("id", o.id); put("orderNumber", o.orderNumber); put("customerId", o.customerId)
                put("status", o.status.name); put("createdAt", o.createdAt)
                put("receivedAt", o.receivedAt ?: JSONObject.NULL)
                put("deliveredAt", o.deliveredAt ?: JSONObject.NULL)
                put("cancelledAt", o.cancelledAt ?: JSONObject.NULL)
                put("expectedCostUsd", o.expectedCostUsd); put("actualCostUsd", o.actualCostUsd ?: JSONObject.NULL)
                put("saleTotalUsd", o.saleTotalUsd)
                put("depositRefunded", o.depositRefunded ?: JSONObject.NULL)
                put("notes", o.notes)
            })
        }
        root.put("orders", orders)

        val items = JSONArray()
        orderList.forEach { o ->
            db.orderDao().getItems(o.id).forEach { oi ->
                items.put(JSONObject().apply {
                    put("id", oi.id); put("orderId", oi.orderId); put("productName", oi.productName)
                    put("quantity", oi.quantity); put("expectedUnitCostUsd", oi.expectedUnitCostUsd)
                    put("actualUnitCostUsd", oi.actualUnitCostUsd ?: JSONObject.NULL)
                    put("unitSalePriceUsd", oi.unitSalePriceUsd)
                })
            }
        }
        root.put("orderItems", items)

        val txs = JSONArray()
        db.transactionDao().getAllOnce().forEach { t ->
            txs.put(JSONObject().apply {
                put("id", t.id); put("type", t.type.name); put("amount", t.amount)
                put("currency", t.currency.name); put("usdEquivalent", t.usdEquivalent)
                put("exchangeRate", t.exchangeRate ?: JSONObject.NULL); put("method", t.method.name)
                put("customerId", t.customerId ?: JSONObject.NULL); put("orderId", t.orderId ?: JSONObject.NULL)
                put("expenseCategory", t.expenseCategory ?: JSONObject.NULL); put("note", t.note)
                put("createdAt", t.createdAt)
                put("reversedByTransactionId", t.reversedByTransactionId ?: JSONObject.NULL)
                put("isReversal", t.isReversal)
            })
        }
        root.put("transactions", txs)

        val dir = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
        val file = File(dir, "hasabati_backup_${System.currentTimeMillis()}.json")
        file.writeText(root.toString(2))
        return file
    }

    /** استعادة كاملة — تستبدل كل البيانات الحالية بمحتوى ملف النسخة الاحتياطية */
    suspend fun importFromFile(file: File) {
        val root = JSONObject(file.readText())

        db.clearAllTables()

        val customers = root.getJSONArray("customers")
        for (i in 0 until customers.length()) {
            val c = customers.getJSONObject(i)
            db.customerDao().insert(
                Customer(
                    id = c.getLong("id"), name = c.getString("name"), phone = c.optString("phone", ""),
                    notes = c.optString("notes", ""), createdAt = c.getLong("createdAt")
                )
            )
        }

        val orders = root.getJSONArray("orders")
        for (i in 0 until orders.length()) {
            val o = orders.getJSONObject(i)
            db.orderDao().insert(
                Order(
                    id = o.getLong("id"), orderNumber = o.getString("orderNumber"), customerId = o.getLong("customerId"),
                    status = OrderStatus.valueOf(o.getString("status")), createdAt = o.getLong("createdAt"),
                    receivedAt = o.optLongNullable("receivedAt"), deliveredAt = o.optLongNullable("deliveredAt"),
                    cancelledAt = o.optLongNullable("cancelledAt"), expectedCostUsd = o.getDouble("expectedCostUsd"),
                    actualCostUsd = o.optDoubleNullable("actualCostUsd"), saleTotalUsd = o.getDouble("saleTotalUsd"),
                    depositRefunded = o.optBooleanNullable("depositRefunded"), notes = o.optString("notes", "")
                )
            )
        }

        val items = root.getJSONArray("orderItems")
        for (i in 0 until items.length()) {
            val oi = items.getJSONObject(i)
            db.orderDao().insertItems(listOf(
                OrderItem(
                    id = oi.getLong("id"), orderId = oi.getLong("orderId"), productName = oi.getString("productName"),
                    quantity = oi.getInt("quantity"), expectedUnitCostUsd = oi.getDouble("expectedUnitCostUsd"),
                    actualUnitCostUsd = oi.optDoubleNullable("actualUnitCostUsd"), unitSalePriceUsd = oi.getDouble("unitSalePriceUsd")
                )
            ))
        }

        val txs = root.getJSONArray("transactions")
        for (i in 0 until txs.length()) {
            val t = txs.getJSONObject(i)
            db.transactionDao().insert(
                Transaction(
                    id = t.getLong("id"), type = TransactionType.valueOf(t.getString("type")), amount = t.getDouble("amount"),
                    currency = Currency.valueOf(t.getString("currency")), usdEquivalent = t.getDouble("usdEquivalent"),
                    exchangeRate = t.optDoubleNullable("exchangeRate"), method = PaymentMethod.valueOf(t.getString("method")),
                    customerId = t.optLongNullable("customerId"), orderId = t.optLongNullable("orderId"),
                    expenseCategory = if (t.isNull("expenseCategory")) null else t.getString("expenseCategory"),
                    note = t.optString("note", ""), createdAt = t.getLong("createdAt"),
                    reversedByTransactionId = t.optLongNullable("reversedByTransactionId"),
                    isReversal = t.optBoolean("isReversal", false)
                )
            )
        }
    }
}

private fun JSONObject.optLongNullable(key: String): Long? = if (isNull(key)) null else getLong(key)
private fun JSONObject.optDoubleNullable(key: String): Double? = if (isNull(key)) null else getDouble(key)
private fun JSONObject.optBooleanNullable(key: String): Boolean? = if (isNull(key)) null else getBoolean(key)
