package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.BizFlowViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusType
import com.example.ui.theme.BizFlowBlue
import com.example.ui.theme.BizFlowGreen
import com.example.ui.theme.BizFlowRose

@Composable
fun ProductsScreen(
    viewModel: BizFlowViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    val categories = listOf("All", "Fasteners", "Tools", "Electrical", "Plumbing", "Building Materials")

    val filtered = products.filter {
        (selectedCategory == "All" || it.category.equals(selectedCategory, true)) &&
        (it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BizFlowBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("products_screen"),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
        ) {
            item {
                Text(
                    text = "Product Catalog",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${products.size} items in catalog • ${products.count { it.currentStock <= it.minimumStock }} low in stock",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by product name or SKU...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Inventory,
                        title = "No Products Found",
                        message = if (searchQuery.isNotBlank()) "No product matched \"$searchQuery\"." else "Tap '+' to add your first hardware item."
                    )
                }
            } else {
                items(filtered) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "SKU: ${product.sku.ifBlank { "N/A" }} • ${product.category}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val status = when {
                                    product.currentStock == 0 -> Pair("OUT OF STOCK", StatusType.ERROR)
                                    product.currentStock <= product.minimumStock -> Pair("LOW STOCK", StatusType.WARNING)
                                    else -> Pair("IN STOCK", StatusType.SUCCESS)
                                }
                                StatusBadge(text = status.first, statusType = status.second)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Selling Price: ${Formatters.formatCurrency(product.sellingPrice)} / ${product.unit}",
                                        fontWeight = FontWeight.SemiBold,
                                        color = BizFlowGreen,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "GST: ${product.gstPercentage.toInt()}% • Supplier: ${product.supplier}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Stock: ${product.currentStock} ${product.unit}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Min: ${product.minimumStock}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = { productToEdit = product },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.deleteProduct(product) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BizFlowRose, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add or Edit Product Dialog
    if (showAddDialog || productToEdit != null) {
        val editing = productToEdit
        var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
        var sku by remember(editing) { mutableStateOf(editing?.sku ?: "") }
        var category by remember(editing) { mutableStateOf(editing?.category ?: "Fasteners") }
        var unit by remember(editing) { mutableStateOf(editing?.unit ?: "pcs") }
        var sellingPrice by remember(editing) { mutableStateOf(editing?.sellingPrice?.toString() ?: "") }
        var purchasePrice by remember(editing) { mutableStateOf(editing?.purchasePrice?.toString() ?: "") }
        var gstPercentage by remember(editing) { mutableStateOf(editing?.gstPercentage?.toInt()?.toString() ?: "18") }
        var stock by remember(editing) { mutableStateOf(editing?.currentStock?.toString() ?: "0") }
        var minStock by remember(editing) { mutableStateOf(editing?.minimumStock?.toString() ?: "10") }
        var supplier by remember(editing) { mutableStateOf(editing?.supplier ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                productToEdit = null
            },
            title = {
                Text(if (editing != null) "Edit Product" else "Add New Product", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Product Name * (e.g. 10mm Bolt)") },
                            modifier = Modifier.fillMaxWidth().testTag("prod_input_name")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                label = { Text("SKU / Code") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sellingPrice,
                                onValueChange = { sellingPrice = it },
                                label = { Text("Selling Price (₹) *") },
                                modifier = Modifier.weight(1f).testTag("prod_input_price")
                            )
                            OutlinedTextField(
                                value = purchasePrice,
                                onValueChange = { purchasePrice = it },
                                label = { Text("Purchase Price (₹)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = stock,
                                onValueChange = { stock = it },
                                label = { Text("Initial Stock") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = minStock,
                                onValueChange = { minStock = it },
                                label = { Text("Min Stock Alert") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Unit (pcs/kg/box)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = gstPercentage,
                                onValueChange = { gstPercentage = it },
                                label = { Text("GST % (18/28)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = supplier,
                            onValueChange = { supplier = it },
                            label = { Text("Supplier Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sPrice = sellingPrice.toDoubleOrNull() ?: 0.0
                        val pPrice = purchasePrice.toDoubleOrNull() ?: 0.0
                        val gst = gstPercentage.toDoubleOrNull() ?: 18.0
                        val curStock = stock.toIntOrNull() ?: 0
                        val mStock = minStock.toIntOrNull() ?: 10

                        if (name.isNotBlank() && sPrice > 0) {
                            if (editing != null) {
                                viewModel.updateProduct(
                                    editing.copy(
                                        name = name,
                                        sku = sku,
                                        category = category,
                                        unit = unit,
                                        sellingPrice = sPrice,
                                        purchasePrice = pPrice,
                                        gstPercentage = gst,
                                        currentStock = curStock,
                                        minimumStock = mStock,
                                        supplier = supplier
                                    )
                                )
                            } else {
                                viewModel.createProduct(
                                    Product(
                                        name = name,
                                        sku = sku,
                                        category = category,
                                        unit = unit,
                                        sellingPrice = sPrice,
                                        purchasePrice = pPrice,
                                        gstPercentage = gst,
                                        currentStock = curStock,
                                        minimumStock = mStock,
                                        supplier = supplier
                                    )
                                )
                            }
                            showAddDialog = false
                            productToEdit = null
                        }
                    },
                    enabled = name.isNotBlank() && (sellingPrice.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = BizFlowBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showAddDialog = false
                    productToEdit = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
