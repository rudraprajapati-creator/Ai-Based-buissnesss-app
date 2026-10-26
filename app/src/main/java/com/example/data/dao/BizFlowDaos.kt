package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AiChatMessage
import com.example.data.model.BusinessProfile
import com.example.data.model.Customer
import com.example.data.model.InventoryMovement
import com.example.data.model.Invoice
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Payment
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<BusinessProfile?>

    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): BusinessProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: BusinessProfile)
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomers(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :name || '%' LIMIT 1")
    suspend fun findCustomerByName(name: String): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET totalOrders = totalOrders + 1, totalPurchased = totalPurchased + :amount, outstandingAmount = outstandingAmount + :amount WHERE id = :customerId")
    suspend fun recordOrderPurchase(customerId: Long, amount: Double)

    @Query("UPDATE customers SET outstandingAmount = MAX(0.0, outstandingAmount - :amount) WHERE id = :customerId")
    suspend fun deductOutstandingBalance(customerId: Long, amount: Double)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :name || '%' LIMIT 1")
    suspend fun findProductByName(name: String): Product?

    @Query("SELECT * FROM products WHERE currentStock <= minimumStock ORDER BY currentStock ASC")
    fun getLowStockProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE currentStock <= minimumStock ORDER BY currentStock ASC")
    suspend fun getLowStockProducts(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET currentStock = currentStock + :delta WHERE id = :productId")
    suspend fun adjustStock(productId: Long, delta: Int)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersFlow(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    suspend fun getAllOrders(): List<Order>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): Order?

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersByCustomerFlow(customerId: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE orderStatus != 'DELIVERED' AND orderStatus != 'CANCELLED' ORDER BY createdAt DESC")
    fun getPendingOrdersFlow(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)
}

@Dao
interface OrderItemDao {
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getItemsForOrderFlow(orderId: Long): Flow<List<OrderItem>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsForOrder(orderId: Long): List<OrderItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteItemsForOrder(orderId: Long)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY issueDate DESC")
    fun getAllInvoicesFlow(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices ORDER BY issueDate DESC")
    suspend fun getAllInvoices(): List<Invoice>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): Invoice?

    @Query("SELECT * FROM invoices WHERE orderId = :orderId LIMIT 1")
    suspend fun getInvoiceByOrderId(orderId: Long): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Query("UPDATE invoices SET amountPaid = amountPaid + :amount, balanceDue = MAX(0.0, balanceDue - :amount), status = CASE WHEN balanceDue - :amount <= 0.05 THEN 'PAID' ELSE 'PARTIALLY_PAID' END WHERE id = :invoiceId")
    suspend fun recordInvoicePayment(invoiceId: Long, amount: Double)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPaymentsFlow(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getPaymentsByCustomerFlow(customerId: Long): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long
}

@Dao
interface InventoryMovementDao {
    @Query("SELECT * FROM inventory_movements ORDER BY timestamp DESC")
    fun getAllMovementsFlow(): Flow<List<InventoryMovement>>

    @Query("SELECT * FROM inventory_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProductFlow(productId: Long): Flow<List<InventoryMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: InventoryMovement): Long
}

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<AiChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiChatMessage): Long

    @Query("DELETE FROM ai_chat_messages")
    suspend fun clearHistory()
}
