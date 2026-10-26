package com.example.ai

import com.example.data.model.Customer
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AiBusinessAdvisor {

    fun generateAnswer(
        question: String,
        customers: List<Customer>,
        products: List<Product>,
        orders: List<Order>,
        orderItems: List<OrderItem>
    ): String {
        val q = question.lowercase().trim()
        val inr = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        // 1. "Who owes me money?" / Pending payments
        if (q.contains("owe") || q.contains("pending payment") || q.contains("outstanding") || q.contains("dues")) {
            val debtors = customers.filter { it.outstandingAmount > 0 }.sortedByDescending { it.outstandingAmount }
            if (debtors.isEmpty()) {
                return "Good news! No customers currently have pending dues. All payments are clear."
            }
            val totalDue = debtors.sumOf { it.outstandingAmount }
            val builder = StringBuilder()
            builder.append("You have ${debtors.size} customers with outstanding balance totaling ${inr.format(totalDue)}:\n\n")
            debtors.forEachIndexed { index, c ->
                builder.append("${index + 1}. **${c.name}**: ${inr.format(c.outstandingAmount)}")
                if (c.phone.isNotBlank()) builder.append(" (📞 ${c.phone})")
                builder.append("\n")
            }
            builder.append("\n💡 *Tip: Tap on Customers or Payments to record UPI or cash collections.*")
            return builder.toString()
        }

        // 2. "Which products are low in stock?"
        if (q.contains("low stock") || q.contains("out of stock") || q.contains("stock low") || q.contains("reorder")) {
            val lowStock = products.filter { it.currentStock <= it.minimumStock }.sortedBy { it.currentStock }
            if (lowStock.isEmpty()) {
                return "All your products are currently well-stocked above their minimum threshold level!"
            }
            val builder = StringBuilder()
            builder.append("⚠️ **${lowStock.size} products need restocking:**\n\n")
            lowStock.forEachIndexed { index, p ->
                val status = if (p.currentStock == 0) "OUT OF STOCK" else "LOW STOCK"
                builder.append("${index + 1}. **${p.name}** [${p.category}]\n")
                builder.append("   • Available: **${p.currentStock} ${p.unit}** (Min threshold: ${p.minimumStock})\n")
                builder.append("   • Status: $status | Supplier: ${p.supplier}\n\n")
            }
            builder.append("💡 *Tip: Go to Inventory to adjust stock or create purchase orders.*")
            return builder.toString()
        }

        // 3. "What did I sell today?" / "this week" / "this month"
        if (q.contains("sell") || q.contains("sales") || q.contains("revenue") || q.contains("sold")) {
            val now = Calendar.getInstance()

            val isToday = q.contains("today")
            val isWeek = q.contains("week")
            val isMonth = q.contains("month")

            val startTime = when {
                isToday -> {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.timeInMillis
                }
                isWeek -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -7)
                    cal.timeInMillis
                }
                isMonth -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -30)
                    cal.timeInMillis
                }
                else -> {
                    // Default to today and this month summary
                    0L
                }
            }

            val validOrders = orders.filter { it.orderStatus != "CANCELLED" && (startTime == 0L || it.createdAt >= startTime) }
            val totalRevenue = validOrders.sumOf { it.grandTotal }
            val periodLabel = when {
                isToday -> "Today"
                isWeek -> "In the last 7 days"
                isMonth -> "In the last 30 days"
                else -> "All-time"
            }

            return "📊 **Sales Summary ($periodLabel):**\n\n" +
                    "• **Total Revenue:** ${inr.format(totalRevenue)}\n" +
                    "• **Total Orders:** ${validOrders.size}\n" +
                    "• **Average Order Value:** ${if (validOrders.isNotEmpty()) inr.format(totalRevenue / validOrders.size) else inr.format(0)}\n\n" +
                    "Keep driving sales with our Quick Order feature!"
        }

        // 4. "Who is my biggest customer?"
        if (q.contains("biggest customer") || q.contains("top customer") || q.contains("best customer")) {
            val topCustomer = customers.maxByOrNull { it.totalPurchased }
            return if (topCustomer != null && topCustomer.totalPurchased > 0) {
                "🏆 Your biggest customer is **${topCustomer.name}**!\n\n" +
                        "• **Total Purchased:** ${inr.format(topCustomer.totalPurchased)}\n" +
                        "• **Total Orders:** ${topCustomer.totalOrders}\n" +
                        "• **Current Outstanding:** ${inr.format(topCustomer.outstandingAmount)}\n" +
                        "• **Contact:** ${topCustomer.phone}"
            } else {
                "No purchase history recorded yet to determine your top customer."
            }
        }

        // 5. "Which products sold the most?" / Top selling products
        if (q.contains("top product") || q.contains("sold the most") || q.contains("popular product") || q.contains("best selling")) {
            val itemQuantities = orderItems.groupBy { it.productName }
                .mapValues { entry -> entry.value.sumOf { it.quantity } }
                .toList()
                .sortedByDescending { it.second }

            if (itemQuantities.isEmpty()) {
                return "No completed order items recorded yet to determine top selling products."
            }

            val builder = StringBuilder()
            builder.append("🔥 **Top Selling Products by Volume:**\n\n")
            itemQuantities.take(5).forEachIndexed { index, pair ->
                builder.append("${index + 1}. **${pair.first}**: ${pair.second} units sold\n")
            }
            return builder.toString()
        }

        // 6. Specific customer query, e.g. "Show me Rajesh's orders"
        for (customer in customers) {
            if (q.contains(customer.name.lowercase())) {
                val custOrders = orders.filter { it.customerId == customer.id || it.customerName.equals(customer.name, true) }
                val builder = StringBuilder()
                builder.append("📋 **Customer Profile: ${customer.name}**\n\n")
                builder.append("• **Phone:** ${customer.phone.ifBlank { "Not specified" }}\n")
                builder.append("• **Outstanding Due:** ${inr.format(customer.outstandingAmount)}\n")
                builder.append("• **Total Orders:** ${customer.totalOrders} (${inr.format(customer.totalPurchased)})\n\n")

                if (custOrders.isNotEmpty()) {
                    builder.append("**Recent Orders:**\n")
                    val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    custOrders.take(3).forEach { o ->
                        builder.append("• Order #${o.orderNumber}: ${inr.format(o.grandTotal)} [${o.orderStatus}] on ${df.format(Date(o.createdAt))}\n")
                    }
                } else {
                    builder.append("No active orders found for ${customer.name}.")
                }
                return builder.toString()
            }
        }

        // 7. General hardware business advisor fallback
        return "I analyzed your store database:\n\n" +
                "• **Total Catalog Products:** ${products.size}\n" +
                "• **Registered Customers:** ${customers.size}\n" +
                "• **Total Active Orders:** ${orders.count { it.orderStatus != "CANCELLED" }}\n" +
                "• **Total Outstanding Market Dues:** ${inr.format(customers.sumOf { it.outstandingAmount })}\n\n" +
                "Try asking me:\n" +
                "👉 \"Who owes me money?\"\n" +
                "👉 \"Which products are low in stock?\"\n" +
                "👉 \"How much did I sell this week?\"\n" +
                "👉 \"Who is my biggest customer?\""
    }
}
