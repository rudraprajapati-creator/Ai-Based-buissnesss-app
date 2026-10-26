package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiBusinessAdvisor
import com.example.ai.AiOrderParser
import com.example.ai.ParsedDraftOrder
import com.example.ai.ParsedItemDraft
import com.example.data.database.AppDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.Customer
import com.example.data.model.InventoryMovement
import com.example.data.model.Invoice
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Payment
import com.example.data.model.Product
import com.example.data.repository.BizFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    AI_ORDER("AI Quick Order"),
    ORDERS("Orders"),
    CUSTOMERS("Customers"),
    PRODUCTS("Products"),
    INVENTORY("Inventory"),
    INVOICES("Invoices"),
    PAYMENTS("Payments"),
    AI_ASSISTANT("AI Business Advisor"),
    ANALYTICS("Analytics"),
    SETTINGS("Settings"),
    ONBOARDING("Setup Business")
}

data class DashboardMetrics(
    val todaySales: Double = 0.0,
    val pendingOrdersCount: Int = 0,
    val pendingPaymentsAmount: Double = 0.0,
    val totalCustomers: Int = 0
)

class BizFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = BizFlowRepository(
        profileDao = db.businessProfileDao(),
        customerDao = db.customerDao(),
        productDao = db.productDao(),
        orderDao = db.orderDao(),
        orderItemDao = db.orderItemDao(),
        invoiceDao = db.invoiceDao(),
        paymentDao = db.paymentDao(),
        movementDao = db.inventoryMovementDao(),
        aiChatDao = db.aiChatDao()
    )

    // Current screen navigation
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Screen navigation stack for back handler
    private val navStack = mutableListOf(AppScreen.DASHBOARD)

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            navStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.size - 1)
            _currentScreen.value = navStack.last()
            return true
        }
        return false
    }

    // Business Profile
    val profile: StateFlow<BusinessProfile> = repository.profileFlow
        .map { it ?: BusinessProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessProfile())

    // Customers & Products
    val customers: StateFlow<List<Customer>> = repository.customersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repository.productsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProductsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Orders & Invoices & Payments
    val orders: StateFlow<List<Order>> = repository.ordersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.invoicesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.paymentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryMovements: StateFlow<List<InventoryMovement>> = repository.inventoryMovementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiMessages = repository.aiMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Combined Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        orders,
        customers
    ) { orderList, customerList ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startOfToday = cal.timeInMillis

        val todaySales = orderList.filter {
            it.createdAt >= startOfToday && it.orderStatus != "CANCELLED"
        }.sumOf { it.grandTotal }

        val pendingOrders = orderList.count {
            it.orderStatus != "DELIVERED" && it.orderStatus != "CANCELLED"
        }

        val pendingPayments = customerList.sumOf { it.outstandingAmount }

        DashboardMetrics(
            todaySales = todaySales,
            pendingOrdersCount = pendingOrders,
            pendingPaymentsAmount = pendingPayments,
            totalCustomers = customerList.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // AI Order Creation State
    private val _aiOrderInput = MutableStateFlow("")
    val aiOrderInput: StateFlow<String> = _aiOrderInput.asStateFlow()

    private val _parsedDraftOrder = MutableStateFlow<ParsedDraftOrder?>(null)
    val parsedDraftOrder: StateFlow<ParsedDraftOrder?> = _parsedDraftOrder.asStateFlow()

    private val _isAiParsing = MutableStateFlow(false)
    val isAiParsing: StateFlow<Boolean> = _isAiParsing.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun updateAiOrderInput(text: String) {
        _aiOrderInput.value = text
    }

    fun parseAiOrder(inputPrompt: String = _aiOrderInput.value) {
        if (inputPrompt.isBlank()) {
            _statusMessage.value = "Please enter an order description"
            return
        }
        _isAiParsing.value = true
        viewModelScope.launch {
            val allCust = repository.getAllCustomers()
            val allProd = repository.getAllProducts()
            val result = AiOrderParser.parseOrderPrompt(inputPrompt, allCust, allProd)
            _parsedDraftOrder.value = result
            _isAiParsing.value = false
        }
    }

    fun updateDraftCustomer(customerName: String) {
        val current = _parsedDraftOrder.value ?: return
        val matched = customers.value.firstOrNull { it.name.equals(customerName, true) }
        _parsedDraftOrder.value = current.copy(
            customerName = customerName,
            matchedCustomer = matched,
            isCustomerMissing = (matched == null)
        )
    }

    fun resolveAmbiguousItem(index: Int, chosenProduct: Product) {
        val current = _parsedDraftOrder.value ?: return
        if (index !in current.items.indices) return
        val existingItem = current.items[index]
        val updatedItems = current.items.toMutableList()

        val subtotal = existingItem.quantity * chosenProduct.sellingPrice
        val gst = subtotal * (chosenProduct.gstPercentage / 100.0)
        val hasStockIssue = chosenProduct.currentStock < existingItem.quantity
        val stockWarning = if (hasStockIssue) {
            "Only ${chosenProduct.currentStock} units are available. You requested ${existingItem.quantity}."
        } else null

        updatedItems[index] = existingItem.copy(
            rawName = chosenProduct.name,
            matchedProduct = chosenProduct,
            ambiguousMatches = emptyList(),
            unitPrice = chosenProduct.sellingPrice,
            gstPercentage = chosenProduct.gstPercentage,
            availableStock = chosenProduct.currentStock,
            hasStockWarning = hasStockIssue,
            stockWarningMessage = stockWarning,
            isProductMissing = false,
            total = subtotal + gst
        )

        val newSubtotal = updatedItems.sumOf { it.quantity * it.unitPrice }
        val newGst = updatedItems.sumOf { (it.quantity * it.unitPrice) * (it.gstPercentage / 100.0) }
        val stillAmbiguous = updatedItems.any { it.ambiguousMatches.size > 1 }
        val stillStockIssues = updatedItems.any { it.hasStockWarning }

        _parsedDraftOrder.value = current.copy(
            items = updatedItems,
            subtotal = newSubtotal,
            gstTotal = newGst,
            grandTotal = newSubtotal + newGst,
            hasAmbiguity = stillAmbiguous,
            hasStockIssues = stillStockIssues
        )
    }

    fun updateDraftItemQuantity(index: Int, newQuantity: Int) {
        val current = _parsedDraftOrder.value ?: return
        if (index !in current.items.indices || newQuantity <= 0) return
        val item = current.items[index]
        val updatedItems = current.items.toMutableList()

        val product = item.matchedProduct
        val available = product?.currentStock ?: 0
        val hasStockIssue = product != null && available < newQuantity
        val stockWarning = if (hasStockIssue) {
            "Only $available units are available. You requested $newQuantity."
        } else if (product == null) {
            item.stockWarningMessage
        } else null

        val subtotal = newQuantity * item.unitPrice
        val gst = subtotal * (item.gstPercentage / 100.0)
        updatedItems[index] = item.copy(
            quantity = newQuantity,
            total = subtotal + gst,
            availableStock = available,
            hasStockWarning = hasStockIssue,
            stockWarningMessage = stockWarning
        )

        val newSubtotal = updatedItems.sumOf { it.quantity * it.unitPrice }
        val newGst = updatedItems.sumOf { (it.quantity * it.unitPrice) * (it.gstPercentage / 100.0) }

        _parsedDraftOrder.value = current.copy(
            items = updatedItems,
            subtotal = newSubtotal,
            gstTotal = newGst,
            grandTotal = newSubtotal + newGst,
            hasStockIssues = updatedItems.any { it.hasStockWarning }
        )
    }

    fun removeDraftItem(index: Int) {
        val current = _parsedDraftOrder.value ?: return
        if (index !in current.items.indices) return
        val updatedItems = current.items.toMutableList().also { it.removeAt(index) }
        val newSubtotal = updatedItems.sumOf { it.quantity * it.unitPrice }
        val newGst = updatedItems.sumOf { (it.quantity * it.unitPrice) * (it.gstPercentage / 100.0) }
        _parsedDraftOrder.value = current.copy(
            items = updatedItems,
            subtotal = newSubtotal,
            gstTotal = newGst,
            grandTotal = newSubtotal + newGst
        )
    }

    fun clearDraftOrder() {
        _parsedDraftOrder.value = null
        _aiOrderInput.value = ""
    }

    fun confirmAiDraftOrder(onSuccess: () -> Unit) {
        val draft = _parsedDraftOrder.value ?: return
        viewModelScope.launch {
            if (draft.items.isEmpty()) {
                _statusMessage.value = "Please add at least one product."
                return@launch
            }

            // Check if customer exists, or auto-create customer
            var customer = draft.matchedCustomer
            if (customer == null) {
                val newId = repository.saveCustomer(
                    Customer(
                        name = draft.customerName,
                        notes = "Created via AI Order"
                    )
                )
                customer = repository.getCustomerById(newId)
            }

            val orderNumber = "ORD-${System.currentTimeMillis() % 10000}"
            val order = Order(
                orderNumber = orderNumber,
                customerId = customer?.id ?: 1L,
                customerName = customer?.name ?: draft.customerName,
                customerPhone = customer?.phone ?: "",
                subtotal = draft.subtotal,
                discount = draft.discount,
                gstTotal = draft.gstTotal,
                grandTotal = draft.grandTotal,
                orderStatus = "CONFIRMED",
                paymentStatus = "UNPAID",
                amountPaid = 0.0,
                notes = draft.notes
            )

            val orderItems = draft.items.mapNotNull { item ->
                val prod = item.matchedProduct ?: return@mapNotNull null
                val itemSub = item.quantity * item.unitPrice
                val itemGst = itemSub * (item.gstPercentage / 100.0)
                OrderItem(
                    orderId = 0L,
                    productId = prod.id,
                    productName = prod.name,
                    unit = prod.unit,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    gstPercentage = item.gstPercentage,
                    gstAmount = itemGst,
                    total = itemSub + itemGst
                )
            }

            val result = repository.confirmOrderWithStockDeduction(order, orderItems, createInvoice = true)
            result.onSuccess {
                _statusMessage.value = "Order #$orderNumber confirmed successfully!"
                clearDraftOrder()
                onSuccess()
            }.onFailure { err ->
                _statusMessage.value = err.message ?: "Failed to confirm order"
            }
        }
    }

    // Customer Actions
    fun createCustomer(customer: Customer, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            _statusMessage.value = "Customer '${customer.name}' added."
            onDone()
        }
    }

    fun updateCustomer(customer: Customer, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            _statusMessage.value = "Customer updated."
            onDone()
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _statusMessage.value = "Customer deleted."
        }
    }

    // Product Actions
    fun createProduct(product: Product, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveProduct(product)
            _statusMessage.value = "Product '${product.name}' added."
            onDone()
        }
    }

    fun updateProduct(product: Product, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateProduct(product)
            _statusMessage.value = "Product updated."
            onDone()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _statusMessage.value = "Product deleted."
        }
    }

    fun adjustStock(productId: Long, delta: Int, reason: String) {
        viewModelScope.launch {
            val result = repository.adjustProductStock(productId, delta, reason)
            result.onSuccess {
                _statusMessage.value = "Stock updated successfully."
            }.onFailure { err ->
                _statusMessage.value = err.message ?: "Stock adjustment failed."
            }
        }
    }

    // Order Actions
    fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            val result = repository.cancelOrder(orderId)
            result.onSuccess {
                _statusMessage.value = "Order cancelled and inventory restored."
            }.onFailure { err ->
                _statusMessage.value = err.message ?: "Failed to cancel order."
            }
        }
    }

    // Payment Actions
    fun recordPayment(
        customerId: Long,
        customerName: String,
        amount: Double,
        method: String,
        orderId: Long? = null,
        notes: String = "",
        refId: String = "",
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.recordPayment(customerId, customerName, amount, method, orderId, notes, refId)
            result.onSuccess {
                _statusMessage.value = "Payment of ₹$amount recorded."
                onDone()
            }.onFailure { err ->
                _statusMessage.value = err.message ?: "Failed to record payment."
            }
        }
    }

    // AI Assistant Chat
    fun askAiAssistant(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            repository.sendUserMessage(question)
            val allCust = repository.getAllCustomers()
            val allProd = repository.getAllProducts()
            val allOrd = repository.getAllOrders()
            // Fetch order items across orders for analytics
            val allItems = mutableListOf<OrderItem>()
            for (order in allOrd.take(30)) {
                allItems.addAll(repository.getOrderItems(order.id))
            }
            val reply = AiBusinessAdvisor.generateAnswer(question, allCust, allProd, allOrd, allItems)
            repository.saveAiReply(reply)
        }
    }

    fun clearAiChat() {
        viewModelScope.launch {
            repository.clearAiHistory()
        }
    }

    // Settings Profile update
    fun updateBusinessProfile(updated: BusinessProfile) {
        viewModelScope.launch {
            repository.saveProfile(updated)
            _statusMessage.value = "Business settings saved."
        }
    }

    fun dismissStatusMessage() {
        _statusMessage.value = null
    }

    // Active Selection states for dialogs & sheets
    private val _selectedCustomerForDetail = MutableStateFlow<Customer?>(null)
    val selectedCustomerForDetail = _selectedCustomerForDetail.asStateFlow()
    fun selectCustomerForDetail(c: Customer?) { _selectedCustomerForDetail.value = c }

    private val _selectedInvoiceForPreview = MutableStateFlow<Invoice?>(null)
    val selectedInvoiceForPreview = _selectedInvoiceForPreview.asStateFlow()
    fun selectInvoiceForPreview(inv: Invoice?) { _selectedInvoiceForPreview.value = inv }
}
