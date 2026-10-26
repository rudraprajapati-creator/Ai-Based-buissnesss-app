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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun InventoryScreen(
    viewModel: BizFlowViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val movements by viewModel.inventoryMovements.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var productForAdjustment by remember { mutableStateOf<Product?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("inventory_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        item {
            Text(
                text = "Inventory Control",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Real-time stock tracking with auto-deduction on order confirmation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            TabRow(
                selectedTabIndex = selectedTab
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Stock Levels (${products.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Movement Log (${movements.size})") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedTab == 0) {
            // Stock Levels Tab
            val lowStockCount = products.count { it.currentStock <= it.minimumStock }
            if (lowStockCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BizFlowRose.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = BizFlowRose)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "$lowStockCount products have dropped to or below minimum threshold!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BizFlowRose
                            )
                        }
                    }
                }
            }

            items(products) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Category: ${product.category} • Min Alert: ${product.minimumStock} ${product.unit}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val (statusText, statusType) = when {
                                product.currentStock == 0 -> Pair("OUT OF STOCK", StatusType.ERROR)
                                product.currentStock <= product.minimumStock -> Pair("LOW STOCK", StatusType.WARNING)
                                else -> Pair("HEALTHY", StatusType.SUCCESS)
                            }
                            StatusBadge(text = statusText, statusType = statusType)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${product.currentStock} ${product.unit}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (product.currentStock <= product.minimumStock) BizFlowRose else BizFlowGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { productForAdjustment = product },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BizFlowBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Adjust", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Movement History Log Tab
            if (movements.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.History,
                        title = "No Inventory Movements",
                        message = "Confirm orders or adjust stock to see real-time inventory history."
                    )
                }
            } else {
                items(movements) { mov ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mov.productName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${mov.reason} • ${Formatters.formatDateTime(mov.timestamp)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val isPositive = mov.quantityChanged >= 0
                                Text(
                                    text = (if (isPositive) "+" else "") + "${mov.quantityChanged}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isPositive) BizFlowGreen else BizFlowRose
                                )
                                Text(
                                    text = "Balance: ${mov.stockAfter}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick Stock Adjustment Dialog
    productForAdjustment?.let { product ->
        var qtyString by remember { mutableStateOf("") }
        var isAddition by remember { mutableStateOf(true) }
        var reason by remember { mutableStateOf("Supplier delivery") }

        AlertDialog(
            onDismissRequest = { productForAdjustment = null },
            title = {
                Text("Adjust Stock: ${product.name}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Current on-hand stock: ${product.currentStock} ${product.unit}")
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isAddition = true; reason = "Supplier purchase delivery" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAddition) BizFlowGreen else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Stock (+)")
                        }
                        Button(
                            onClick = { isAddition = false; reason = "Damaged / Waste / Manual write-off" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isAddition) BizFlowRose else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove (-)")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = qtyString,
                        onValueChange = { qtyString = it },
                        label = { Text("Quantity (${product.unit})") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Adjustment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = qtyString.toIntOrNull() ?: 0
                        if (qty > 0) {
                            val delta = if (isAddition) qty else -qty
                            viewModel.adjustStock(product.id, delta, reason)
                            productForAdjustment = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BizFlowBlue)
                ) {
                    Text("Apply Adjustment")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productForAdjustment = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
