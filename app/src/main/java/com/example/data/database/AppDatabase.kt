package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BusinessProfile::class,
        Customer::class,
        Product::class,
        Order::class,
        OrderItem::class,
        Invoice::class,
        Payment::class,
        InventoryMovement::class,
        AiChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun paymentDao(): PaymentDao
    abstract fun inventoryMovementDao(): InventoryMovementDao
    abstract fun aiChatDao(): AiChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bizflow_database.db"
                )
                .addCallback(SeedDataCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedDataCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedInitialHardwareData(database)
                }
            }
        }
    }
}

suspend fun seedInitialHardwareData(db: AppDatabase) {
    // 1. Business Profile
    val profile = BusinessProfile(
        id = 1L,
        name = "Shree Ganesh Hardware & Wholesalers",
        ownerName = "Rajesh Sharma",
        businessType = "Hardware & Wholesale",
        phone = "+91 98234 56789",
        email = "sales@shreeganeshhardware.in",
        address = "Plot 42, Market Yard, Pune, Maharashtra 411037",
        gstNumber = "27AAPFU0939F1ZV",
        currency = "₹",
        invoicePrefix = "INV-2026-",
        defaultGstPercent = 18.0,
        isOnboardingCompleted = true
    )
    db.businessProfileDao().saveProfile(profile)

    // 2. Customers
    val rajeshId = db.customerDao().insertCustomer(
        Customer(
            name = "Rajesh",
            phone = "+91 98220 11223",
            email = "rajesh.civil@gmail.com",
            address = "Kothrud, Pune",
            gstNumber = "27AABCR1234F1Z8",
            totalOrders = 3,
            totalPurchased = 18450.0,
            outstandingAmount = 4250.0,
            notes = "Regular building contractor, 15 days credit"
        )
    )

    val amitId = db.customerDao().insertCustomer(
        Customer(
            name = "Amit",
            phone = "+91 98220 44556",
            email = "amit.patel@rediffmail.com",
            address = "Hadapsar Industrial Estate, Pune",
            gstNumber = "27BCDP8821K1Z2",
            totalOrders = 4,
            totalPurchased = 34500.0,
            outstandingAmount = 12800.0,
            notes = "Bulk buyer for residential sites"
        )
    )

    val karanId = db.customerDao().insertCustomer(
        Customer(
            name = "Karan",
            phone = "+91 98220 77889",
            email = "karan.electricals@gmail.com",
            address = "Sinhagad Road, Pune",
            gstNumber = "",
            totalOrders = 2,
            totalPurchased = 5400.0,
            outstandingAmount = 0.0,
            notes = "Electrician, always pays UPI instantly"
        )
    )

    val maheshId = db.customerDao().insertCustomer(
        Customer(
            name = "Mahesh",
            phone = "+91 98220 99001",
            email = "mahesh.plumbing@yahoo.com",
            address = "Wakad, Pune",
            gstNumber = "27CCUP5542M1Z0",
            totalOrders = 3,
            totalPurchased = 14200.0,
            outstandingAmount = 3150.0,
            notes = "Plumbing master contractor"
        )
    )

    // 3. Products
    val p10mmBolt = db.productDao().insertProduct(
        Product(
            name = "10mm Bolt",
            sku = "BLT-10MM",
            category = "Fasteners",
            unit = "pcs",
            sellingPrice = 15.0,
            purchasePrice = 9.5,
            gstPercentage = 18.0,
            currentStock = 180,
            minimumStock = 40,
            supplier = "Apex Fasteners Ludhiana"
        )
    )

    val p12mmBolt = db.productDao().insertProduct(
        Product(
            name = "12mm Bolt",
            sku = "BLT-12MM",
            category = "Fasteners",
            unit = "pcs",
            sellingPrice = 22.0,
            purchasePrice = 14.0,
            gstPercentage = 18.0,
            currentStock = 140,
            minimumStock = 30,
            supplier = "Apex Fasteners Ludhiana"
        )
    )

    val p10mmNut = db.productDao().insertProduct(
        Product(
            name = "10mm Nut",
            sku = "NUT-10MM",
            category = "Fasteners",
            unit = "pcs",
            sellingPrice = 6.0,
            purchasePrice = 3.5,
            gstPercentage = 18.0,
            currentStock = 320,
            minimumStock = 60,
            supplier = "Apex Fasteners Ludhiana"
        )
    )

    val p12mmNut = db.productDao().insertProduct(
        Product(
            name = "12mm Nut",
            sku = "NUT-12MM",
            category = "Fasteners",
            unit = "pcs",
            sellingPrice = 8.0,
            purchasePrice = 4.8,
            gstPercentage = 18.0,
            currentStock = 240,
            minimumStock = 50,
            supplier = "Apex Fasteners Ludhiana"
        )
    )

    val pPvcPipe = db.productDao().insertProduct(
        Product(
            name = "PVC Pipe",
            sku = "PVC-4IN-10FT",
            category = "Plumbing",
            unit = "pcs",
            sellingPrice = 240.0,
            purchasePrice = 180.0,
            gstPercentage = 18.0,
            currentStock = 6,
            minimumStock = 15, // Low stock!
            supplier = "Finolex Pipes Distributor"
        )
    )

    val pElectricalTape = db.productDao().insertProduct(
        Product(
            name = "Electrical Tape",
            sku = "EL-TAPE-BLK",
            category = "Electrical",
            unit = "roll",
            sellingPrice = 25.0,
            purchasePrice = 16.0,
            gstPercentage = 18.0,
            currentStock = 4,
            minimumStock = 12, // Low stock!
            supplier = "Anchor Electricals"
        )
    )

    val pScrewdriver = db.productDao().insertProduct(
        Product(
            name = "Screwdriver",
            sku = "TOOL-SCREW-SET",
            category = "Tools",
            unit = "set",
            sellingPrice = 320.0,
            purchasePrice = 220.0,
            gstPercentage = 18.0,
            currentStock = 18,
            minimumStock = 5,
            supplier = "Taparia Tools"
        )
    )

    val pDrillBit = db.productDao().insertProduct(
        Product(
            name = "Drill Bit",
            sku = "TOOL-DRILL-8MM",
            category = "Tools",
            unit = "pcs",
            sellingPrice = 120.0,
            purchasePrice = 80.0,
            gstPercentage = 18.0,
            currentStock = 22,
            minimumStock = 8,
            supplier = "Bosch India"
        )
    )

    val pCementBag = db.productDao().insertProduct(
        Product(
            name = "Cement Bag",
            sku = "BLD-CMT-50KG",
            category = "Building Materials",
            unit = "bag",
            sellingPrice = 390.0,
            purchasePrice = 330.0,
            gstPercentage = 28.0,
            currentStock = 45,
            minimumStock = 20,
            supplier = "UltraTech Cement Depot"
        )
    )

    // 4. Sample Recent Orders & Invoices
    val order1Id = db.orderDao().insertOrder(
        Order(
            orderNumber = "ORD-101",
            customerId = rajeshId,
            customerName = "Rajesh",
            customerPhone = "+91 98220 11223",
            subtotal = 3600.0,
            discount = 100.0,
            gstTotal = 630.0,
            grandTotal = 4130.0,
            orderStatus = "CONFIRMED",
            paymentStatus = "PARTIALLY_PAID",
            amountPaid = 2000.0,
            notes = "Site delivery at Kothrud Phase 2",
            createdAt = System.currentTimeMillis() - (4 * 60 * 60 * 1000)
        )
    )
    db.orderItemDao().insertOrderItems(
        listOf(
            OrderItem(orderId = order1Id, productId = p10mmBolt, productName = "10mm Bolt", quantity = 100, unitPrice = 15.0, gstPercentage = 18.0, gstAmount = 270.0, total = 1770.0),
            OrderItem(orderId = order1Id, productId = p10mmNut, productName = "10mm Nut", quantity = 100, unitPrice = 6.0, gstPercentage = 18.0, gstAmount = 108.0, total = 708.0),
            OrderItem(orderId = order1Id, productId = pCementBag, productName = "Cement Bag", quantity = 4, unitPrice = 390.0, gstPercentage = 28.0, gstAmount = 436.8, total = 1996.8)
        )
    )

    db.invoiceDao().insertInvoice(
        Invoice(
            invoiceNumber = "INV-2026-001",
            orderId = order1Id,
            customerId = rajeshId,
            customerName = "Rajesh",
            customerGst = "27AABCR1234F1Z8",
            customerPhone = "+91 98220 11223",
            customerAddress = "Kothrud, Pune",
            subtotal = 3500.0,
            taxTotal = 630.0,
            grandTotal = 4130.0,
            amountPaid = 2000.0,
            balanceDue = 2130.0,
            status = "PARTIALLY_PAID"
        )
    )

    // 5. Sample Payment
    db.paymentDao().insertPayment(
        Payment(
            customerId = rajeshId,
            customerName = "Rajesh",
            orderId = order1Id,
            amount = 2000.0,
            paymentMethod = "UPI",
            referenceId = "UPI/629910481239",
            notes = "Google Pay advance payment"
        )
    )

    // 6. Welcome AI message
    db.aiChatDao().insertMessage(
        AiChatMessage(
            role = "assistant",
            content = "Namaste Rajesh ji! Welcome to BizFlow AI. I can help analyze your business and create orders from natural language.\n\nTry asking:\n• 'Who owes me money?'\n• 'Which products are low in stock?'\n• 'What did I sell today?'"
        )
    )
}
