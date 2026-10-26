package com.example.data.repository

import com.example.data.dao.AiChatDao
import com.example.data.dao.BusinessProfileDao
import com.example.data.dao.CustomerDao
import com.example.data.dao.InventoryMovementDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.OrderDao
import com.example.data.dao.OrderItemDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.ProductDao
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
import java.util.Calendar

class BizFlowRepository(
    private val profileDao: BusinessProfileDao,
    private val customerDao: CustomerDao,
    private val productDao: ProductDao,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val invoiceDao: InvoiceDao,
    private val paymentDao: PaymentDao,
    private val movementDao: InventoryMovementDao,
    private val aiChatDao: AiChatDao
) {
    // Profile
    val profileFlow: Flow<BusinessProfile?> = profileDao.getProfileFlow()
    suspend fun getProfile(): BusinessProfile = profileDao.getProfile() ?: BusinessProfile()
    suspend fun saveProfile(profile: BusinessProfile) = profileDao.saveProfile(profile)

    // Customers
    val customersFlow: Flow<List<Customer>> = customerDao.getAllCustomersFlow()
    suspend fun getAllCustomers(): List<Customer> = customerDao.getAllCustomers()
    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)
    suspend fun findCustomerByName(name: String): Customer? = customerDao.findCustomerByName(name)
    suspend fun saveCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    // Products
    val productsFlow: Flow<List<Product>> = productDao.getAllProductsFlow()
    val lowStockProductsFlow: Flow<List<Product>> = productDao.getLowStockProductsFlow()
    suspend fun getAllProducts(): List<Product> = productDao.getAllProducts()
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    suspend fun findProductByName(name: String): Product? = productDao.findProductByName(name)
    suspend fun saveProduct(product: Product): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    // Orders
    val ordersFlow: Flow<List<Order>> = orderDao.getAllOrdersFlow()
    val pendingOrdersFlow: Flow<List<Order>> = orderDao.getPendingOrdersFlow()
    suspend fun getAllOrders(): List<Order> = orderDao.getAllOrders()
    suspend fun getOrderById(id: Long): Order? = orderDao.getOrderById(id)
    fun getOrderItemsFlow(orderId: Long): Flow<List<OrderItem>> = orderItemDao.getItemsForOrderFlow(orderId)
    suspend fun getOrderItems(orderId: Long): List<OrderItem> = orderItemDao.getItemsForOrder(orderId)

    // Invoices & Payments
    val invoicesFlow: Flow<List<Invoice>> = invoiceDao.getAllInvoicesFlow()
    suspend fun getAllInvoices(): List<Invoice> = invoiceDao.getAllInvoices()
    val paymentsFlow: Flow<List<Payment>> = paymentDao.getAllPaymentsFlow()

    // Inventory Movements
    val inventoryMovementsFlow: Flow<List<InventoryMovement>> = movementDao.getAllMovementsFlow()

    // AI Messages
    val aiMessagesFlow: Flow<List<AiChatMessage>> = aiChatDao.getAllMessagesFlow()
    suspend fun sendUserMessage(msg: String) = aiChatDao.insertMessage(AiChatMessage(role = "user", content = msg))
    suspend fun saveAiReply(msg: String, action: String? = null) = aiChatDao.insertMessage(AiChatMessage(role = "assistant", content = msg, actionType = action))
    suspend fun clearAiHistory() = aiChatDao.clearHistory()

    // Adjust Stock manually
    suspend fun adjustProductStock(productId: Long, delta: Int, reason: String): Result<Product> {
        val product = productDao.getProductById(productId) ?: return Result.failure(Exception("Product not found"))
        val newStock = product.currentStock + delta
        if (newStock < 0) {
            return Result.failure(Exception("Stock cannot be negative. Current available: ${product.currentStock}"))
        }
        val updated = product.copy(currentStock = newStock)
        productDao.updateProduct(updated)
        movementDao.insertMovement(
            InventoryMovement(
                productId = productId,
                productName = product.name,
                type = if (delta >= 0) "IN" else "OUT",
                quantityChanged = delta,
                stockAfter = newStock,
                reason = reason
            )
        )
        return Result.success(updated)
    }

    // Confirm Order Business Logic
    suspend fun confirmOrderWithStockDeduction(
        order: Order,
        items: List<OrderItem>,
        createInvoice: Boolean = true
    ): Result<Long> {
        if (items.isEmpty()) {
            return Result.failure(Exception("Please add at least one product."))
        }

        // 1. Validate Stock for all items
        for (item in items) {
            val product = productDao.getProductById(item.productId)
            if (product == null) {
                return Result.failure(Exception("Product '${item.productName}' not found in database."))
            }
            if (product.currentStock < item.quantity) {
                return Result.failure(Exception("Insufficient stock for '${product.name}'. Available: ${product.currentStock}, Requested: ${item.quantity}."))
            }
        }

        // 2. Insert Order
        val orderId = orderDao.insertOrder(order)
        val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
        orderItemDao.insertOrderItems(itemsWithOrderId)

        // 3. Deduct stock and log movements
        for (item in items) {
            val product = productDao.getProductById(item.productId)!!
            val newStock = product.currentStock - item.quantity
            productDao.updateProduct(product.copy(currentStock = newStock))
            movementDao.insertMovement(
                InventoryMovement(
                    productId = item.productId,
                    productName = item.productName,
                    type = "ORDER_FULFILLMENT",
                    quantityChanged = -item.quantity,
                    stockAfter = newStock,
                    reason = "Order #${order.orderNumber}"
                )
            )
        }

        // 4. Update Customer purchase statistics
        val unpaidPortion = order.grandTotal - order.amountPaid
        customerDao.recordOrderPurchase(order.customerId, order.grandTotal)
        if (order.amountPaid > 0) {
            customerDao.deductOutstandingBalance(order.customerId, order.amountPaid)
        }

        // 5. Create Invoice if requested
        if (createInvoice) {
            val profile = getProfile()
            val invNumber = "${profile.invoicePrefix}${System.currentTimeMillis() % 100000}"
            val customer = customerDao.getCustomerById(order.customerId)
            val invoice = Invoice(
                invoiceNumber = invNumber,
                orderId = orderId,
                customerId = order.customerId,
                customerName = order.customerName,
                customerGst = customer?.gstNumber ?: "",
                customerPhone = order.customerPhone,
                customerAddress = customer?.address ?: "",
                subtotal = order.subtotal,
                taxTotal = order.gstTotal,
                grandTotal = order.grandTotal,
                amountPaid = order.amountPaid,
                balanceDue = unpaidPortion,
                status = if (unpaidPortion <= 0.05) "PAID" else if (order.amountPaid > 0) "PARTIALLY_PAID" else "UNPAID"
            )
            invoiceDao.insertInvoice(invoice)
        }

        return Result.success(orderId)
    }

    // Cancel Order: Restores inventory and preserves history
    suspend fun cancelOrder(orderId: Long): Result<Unit> {
        val order = orderDao.getOrderById(orderId) ?: return Result.failure(Exception("Order not found"))
        if (order.orderStatus == "CANCELLED") {
            return Result.failure(Exception("Order is already cancelled."))
        }

        val items = orderItemDao.getItemsForOrder(orderId)
        // 1. Restore Inventory
        for (item in items) {
            val product = productDao.getProductById(item.productId)
            if (product != null) {
                val newStock = product.currentStock + item.quantity
                productDao.updateProduct(product.copy(currentStock = newStock))
                movementDao.insertMovement(
                    InventoryMovement(
                        productId = item.productId,
                        productName = item.productName,
                        type = "ORDER_CANCELLATION",
                        quantityChanged = item.quantity,
                        stockAfter = newStock,
                        reason = "Cancelled Order #${order.orderNumber}"
                    )
                )
            }
        }

        // 2. Mark order as CANCELLED
        orderDao.updateOrder(order.copy(orderStatus = "CANCELLED"))

        // 3. Mark invoice as CANCELLED
        val invoice = invoiceDao.getInvoiceByOrderId(orderId)
        if (invoice != null) {
            invoiceDao.updateInvoice(invoice.copy(status = "CANCELLED"))
        }

        // 4. Adjust customer outstanding balance if unpaid
        val unpaid = order.grandTotal - order.amountPaid
        if (unpaid > 0) {
            customerDao.deductOutstandingBalance(order.customerId, unpaid)
        }

        return Result.success(Unit)
    }

    // Record Payment
    suspend fun recordPayment(
        customerId: Long,
        customerName: String,
        amount: Double,
        method: String,
        orderId: Long? = null,
        notes: String = "",
        refId: String = ""
    ): Result<Long> {
        if (amount <= 0) {
            return Result.failure(Exception("Payment amount must be greater than zero."))
        }

        val payment = Payment(
            customerId = customerId,
            customerName = customerName,
            orderId = orderId,
            amount = amount,
            paymentMethod = method,
            referenceId = refId,
            notes = notes
        )
        val pId = paymentDao.insertPayment(payment)

        // Deduct customer outstanding balance
        customerDao.deductOutstandingBalance(customerId, amount)

        // If order specified, update order & invoice
        if (orderId != null) {
            val order = orderDao.getOrderById(orderId)
            if (order != null) {
                val newPaid = order.amountPaid + amount
                val newStatus = if (newPaid >= order.grandTotal - 0.05) "PAID" else "PARTIALLY_PAID"
                orderDao.updateOrder(order.copy(amountPaid = newPaid, paymentStatus = newStatus))

                val invoice = invoiceDao.getInvoiceByOrderId(orderId)
                if (invoice != null) {
                    invoiceDao.recordInvoicePayment(invoice.id, amount)
                }
            }
        }

        return Result.success(pId)
    }
}
