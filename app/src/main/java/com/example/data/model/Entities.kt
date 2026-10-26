package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Long = 1L,
    val name: String = "Shree Ganesh Hardware & Tools",
    val ownerName: String = "Rajesh Sharma",
    val businessType: String = "Hardware & Wholesale",
    val phone: String = "+91 98234 56789",
    val email: String = "sales@shreeganeshhardware.in",
    val address: String = "Plot 42, Market Yard, Pune, Maharashtra 411037",
    val gstNumber: String = "27AAPFU0939F1ZV",
    val currency: String = "₹",
    val invoicePrefix: String = "INV-",
    val defaultGstPercent: Double = 18.0,
    val isOnboardingCompleted: Boolean = true
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val gstNumber: String = "",
    val totalOrders: Int = 0,
    val totalPurchased: Double = 0.0,
    val outstandingAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val sku: String = "",
    val category: String = "Hardware",
    val unit: String = "pcs",
    val sellingPrice: Double,
    val purchasePrice: Double = 0.0,
    val gstPercentage: Double = 18.0,
    val currentStock: Int = 0,
    val minimumStock: Int = 10,
    val supplier: String = "Local Wholesaler",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val orderNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String = "",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val gstTotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    val orderStatus: String = "CONFIRMED", // DRAFT, CONFIRMED, PROCESSING, READY, DELIVERED, CANCELLED
    val paymentStatus: String = "UNPAID", // UNPAID, PARTIALLY_PAID, PAID
    val amountPaid: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_items",
    indices = [Index(value = ["orderId"]), Index(value = ["productId"])]
)
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val unit: String = "pcs",
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val gstPercentage: Double = 18.0,
    val gstAmount: Double = 0.0,
    val total: Double = 0.0
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceNumber: String,
    val orderId: Long,
    val customerId: Long,
    val customerName: String,
    val customerGst: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000),
    val subtotal: Double,
    val taxTotal: Double,
    val grandTotal: Double,
    val amountPaid: Double = 0.0,
    val balanceDue: Double = 0.0,
    val status: String = "UNPAID", // UNPAID, PARTIALLY_PAID, PAID, CANCELLED
    val notes: String = ""
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val customerId: Long,
    val customerName: String,
    val orderId: Long? = null,
    val invoiceId: Long? = null,
    val amount: Double,
    val paymentMethod: String = "UPI", // Cash, UPI, Bank Transfer, Card, Cheque
    val paymentDate: Long = System.currentTimeMillis(),
    val referenceId: String = "",
    val notes: String = ""
)

@Entity(tableName = "inventory_movements")
data class InventoryMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val productName: String,
    val type: String, // IN, OUT, ADJUSTMENT, ORDER_FULFILLMENT, ORDER_CANCELLATION
    val quantityChanged: Int,
    val stockAfter: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_chat_messages")
data class AiChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val role: String, // user, assistant
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null
)
