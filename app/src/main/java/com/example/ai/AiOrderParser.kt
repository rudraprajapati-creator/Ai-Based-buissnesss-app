package com.example.ai

import com.example.data.model.Customer
import com.example.data.model.Product
import java.util.regex.Pattern

data class ParsedItemDraft(
    val rawName: String,
    val matchedProduct: Product?,
    val ambiguousMatches: List<Product> = emptyList(),
    val quantity: Int,
    val unitPrice: Double,
    val gstPercentage: Double,
    val total: Double,
    val availableStock: Int = 0,
    val hasStockWarning: Boolean = false,
    val stockWarningMessage: String? = null,
    val isProductMissing: Boolean = false
)

data class ParsedDraftOrder(
    val rawInput: String,
    val customerName: String,
    val matchedCustomer: Customer?,
    val isCustomerMissing: Boolean,
    val items: List<ParsedItemDraft>,
    val subtotal: Double,
    val discount: Double,
    val gstTotal: Double,
    val grandTotal: Double,
    val hasAmbiguity: Boolean = false,
    val hasStockIssues: Boolean = false,
    val notes: String = ""
)

object AiOrderParser {

    fun parseOrderPrompt(
        prompt: String,
        allCustomers: List<Customer>,
        allProducts: List<Product>
    ): ParsedDraftOrder {
        val cleanPrompt = prompt.trim()

        // 1. Extract Customer name
        val detectedCustomerName = extractCustomerName(cleanPrompt, allCustomers)
        val matchedCustomer = allCustomers.firstOrNull {
            it.name.equals(detectedCustomerName, ignoreCase = true) ||
            cleanPrompt.contains(it.name, ignoreCase = true)
        }

        val finalCustomerName = matchedCustomer?.name
            ?: detectedCustomerName
            ?: "Walk-in Customer"

        val isCustomerMissing = (matchedCustomer == null && detectedCustomerName != null)

        // 2. Extract Items and Quantities
        val extractedItems = extractItems(cleanPrompt, allProducts)

        // 3. Compute Totals & Check Stock / Ambiguities
        var subtotal = 0.0
        var gstTotal = 0.0
        var hasStockIssues = false
        var hasAmbiguity = false

        val processedItems = extractedItems.map { item ->
            val product = item.matchedProduct
            val available = product?.currentStock ?: 0

            val isMissing = (product == null && item.ambiguousMatches.isEmpty())
            val isAmbiguous = item.ambiguousMatches.size > 1

            if (isAmbiguous) {
                hasAmbiguity = true
            }

            val warning = when {
                isMissing -> "Product '${item.rawName}' not found in database."
                isAmbiguous -> "Which product did you mean?"
                product != null && product.currentStock == 0 -> {
                    hasStockIssues = true
                    "Out of stock. 0 available (requested ${item.quantity})."
                }
                product != null && product.currentStock < item.quantity -> {
                    hasStockIssues = true
                    "Only ${product.currentStock} units are available. You requested ${item.quantity}."
                }
                else -> null
            }

            val itemSubtotal = item.quantity * item.unitPrice
            val itemGst = itemSubtotal * (item.gstPercentage / 100.0)
            subtotal += itemSubtotal
            gstTotal += itemGst

            item.copy(
                availableStock = available,
                hasStockWarning = (product != null && product.currentStock < item.quantity),
                stockWarningMessage = warning,
                isProductMissing = isMissing,
                total = itemSubtotal + itemGst
            )
        }

        val grandTotal = subtotal + gstTotal

        return ParsedDraftOrder(
            rawInput = cleanPrompt,
            customerName = finalCustomerName,
            matchedCustomer = matchedCustomer,
            isCustomerMissing = isCustomerMissing,
            items = processedItems,
            subtotal = subtotal,
            discount = 0.0,
            gstTotal = gstTotal,
            grandTotal = grandTotal,
            hasAmbiguity = hasAmbiguity,
            hasStockIssues = hasStockIssues,
            notes = "AI order parsed from text: \"$cleanPrompt\""
        )
    }

    private fun extractCustomerName(prompt: String, customers: List<Customer>): String? {
        for (customer in customers) {
            if (prompt.contains(customer.name, ignoreCase = true)) {
                return customer.name
            }
        }

        val patterns = listOf(
            Pattern.compile("^([A-Za-z]+)\\s+(?:needs|wants|ordered|took|asked for|requires)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:give|order for|customer|client|send to)\\s+([A-Za-z]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^([A-Za-z]+):", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(prompt)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (!candidate.isNullOrBlank() && !candidate.equals("please", true) && !candidate.equals("give", true)) {
                    return candidate.capitalizeFirstLetter()
                }
            }
        }

        val words = prompt.split(" ", ",")
        if (words.isNotEmpty() && words[0].matches(Regex("^[A-Za-z]{3,}$"))) {
            val first = words[0].capitalizeFirstLetter()
            val commonWords = listOf("please", "create", "order", "add", "send", "give", "urgent", "i", "we")
            if (first.lowercase() !in commonWords) {
                return first
            }
        }

        return null
    }

    private fun extractItems(prompt: String, catalog: List<Product>): List<ParsedItemDraft> {
        val items = mutableListOf<ParsedItemDraft>()
        val segments = prompt.split(Regex("(?i)\\band\\b|,|\\+|\\&|\\n|;"))

        for (segment in segments) {
            val seg = segment.trim()
            if (seg.isBlank()) continue

            // Regex: match quantity and item description
            val qtyPattern = Pattern.compile("(\\d+)\\s*(?:pcs|pieces|nos|bags|rolls|box|boxes)?\\s+([A-Za-z0-9\\s\\-\\.\"]+)", Pattern.CASE_INSENSITIVE)
            val matcher = qtyPattern.matcher(seg)

            if (matcher.find()) {
                val qty = matcher.group(1)?.toIntOrNull() ?: 1
                var rawProduct = matcher.group(2)?.trim() ?: ""
                rawProduct = rawProduct.replace(Regex("(?i)\\s+(at|for|each|price).*"), "").trim()

                if (rawProduct.isNotBlank()) {
                    val candidateMatches = findCandidateProducts(rawProduct, catalog)

                    val matchedProduct = if (candidateMatches.size == 1) candidateMatches[0] else null
                    val price = matchedProduct?.sellingPrice ?: 0.0
                    val gst = matchedProduct?.gstPercentage ?: 18.0

                    items.add(
                        ParsedItemDraft(
                            rawName = matchedProduct?.name ?: rawProduct.capitalizeFirstLetter(),
                            matchedProduct = matchedProduct,
                            ambiguousMatches = if (candidateMatches.size > 1) candidateMatches else emptyList(),
                            quantity = qty,
                            unitPrice = price,
                            gstPercentage = gst,
                            total = (qty * price) * (1 + gst / 100.0)
                        )
                    )
                }
            } else {
                for (product in catalog) {
                    if (seg.contains(product.name, ignoreCase = true)) {
                        val numMatcher = Pattern.compile("(\\d+)").matcher(seg)
                        val qty = if (numMatcher.find()) numMatcher.group(1)?.toIntOrNull() ?: 1 else 1

                        if (items.none { it.matchedProduct?.id == product.id }) {
                            items.add(
                                ParsedItemDraft(
                                    rawName = product.name,
                                    matchedProduct = product,
                                    quantity = qty,
                                    unitPrice = product.sellingPrice,
                                    gstPercentage = product.gstPercentage,
                                    total = (qty * product.sellingPrice) * (1 + product.gstPercentage / 100.0)
                                )
                            )
                        }
                    }
                }
            }
        }

        return items
    }

    private fun findCandidateProducts(rawName: String, catalog: List<Product>): List<Product> {
        val clean = rawName.lowercase().trim()
            .replace("bolts", "bolt")
            .replace("nuts", "nut")
            .replace("pipes", "pipe")
            .replace("tapes", "tape")
            .replace("screwdrivers", "screwdriver")
            .replace("drill bits", "drill bit")
            .replace("bags", "bag")
            .replace("taps", "tap")

        // Exact match first
        val exact = catalog.filter { it.name.equals(clean, ignoreCase = true) }
        if (exact.isNotEmpty()) return exact

        // Specific multi-word match (e.g. "10mm bolt")
        val tokens = clean.split(" ").filter { it.length >= 2 }
        val tokenMatches = catalog.filter { product ->
            tokens.all { token -> product.name.contains(token, ignoreCase = true) }
        }
        if (tokenMatches.isNotEmpty()) return tokenMatches

        // Partial contains match (e.g. "bolt" matches both "10mm Bolt" and "12mm Bolt" -> AMBIGUOUS!)
        val partialMatches = catalog.filter { product ->
            product.name.contains(clean, ignoreCase = true) || clean.contains(product.name, ignoreCase = true)
        }
        return partialMatches
    }

    private fun String.capitalizeFirstLetter(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
