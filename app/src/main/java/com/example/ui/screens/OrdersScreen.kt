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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.Order
import com.example.ui.AppScreen
import com.example.ui.BizFlowViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusType
import com.example.ui.theme.BizFlowBlue
import com.example.ui.theme.BizFlowGreen
import com.example.ui.theme.BizFlowRose

@Composable
fun OrdersScreen(
    viewModel: BizFlowViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedOrderDetail by remember { mutableStateOf<Order?>(null) }
    var showCancelConfirm by remember { mutableStateOf<Order?>(null) }

    val statusFilters = listOf("All", "CONFIRMED", "PROCESSING", "READY", "DELIVERED", "CANCELLED")

    val filtered = orders.filter {
        selectedFilter == "All" || it.orderStatus.equals(selectedFilter, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.AI_ORDER) },
                containerColor = BizFlowBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("new_order_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ AI Order", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("orders_screen"),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
        ) {
            item {
                Text(
                    text = "Sales Orders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${orders.size} total orders • ${orders.count { it.orderStatus != "CANCELLED" && it.orderStatus != "DELIVERED" }} active in progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statusFilters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.ShoppingCart,
                        title = "No Orders Found",
                        message = "Tap '+ AI Order' to create an order using natural language."
                    )
                }
            } else {
                items(filtered) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { selectedOrderDetail = order },
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
                                Column {
                                    Text(
                                        text = order.customerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Order #${order.orderNumber} • ${Formatters.formatDateTime(order.createdAt)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val statusType = when (order.orderStatus) {
                                    "CONFIRMED" -> StatusType.SUCCESS
                                    "DELIVERED" -> StatusType.INFO
                                    "CANCELLED" -> StatusType.ERROR
                                    else -> StatusType.WARNING
                                }
                                StatusBadge(text = order.orderStatus, statusType = statusType)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total: ${Formatters.formatCurrency(order.grandTotal)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = BizFlowGreen
                                    )
                                    if (order.amountPaid > 0) {
                                        Text(
                                            text = "Paid: ${Formatters.formatCurrency(order.amountPaid)}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                val payStatusType = when (order.paymentStatus) {
                                    "PAID" -> StatusType.SUCCESS
                                    "PARTIALLY_PAID" -> StatusType.WARNING
                                    else -> StatusType.ERROR
                                }
                                StatusBadge(text = order.paymentStatus, statusType = payStatusType)
                            }

                            if (order.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notes: ${order.notes}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Order Detail Dialog
    selectedOrderDetail?.let { order ->
        AlertDialog(
            onDismissRequest = { selectedOrderDetail = null },
            title = {
                Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Customer: ${order.customerName}", fontWeight = FontWeight.Bold)
                    if (order.customerPhone.isNotBlank()) Text("Phone: ${order.customerPhone}")
                    Text("Date: ${Formatters.formatDateTime(order.createdAt)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status: ${order.orderStatus}")
                    Text("Payment: ${order.paymentStatus} (${Formatters.formatCurrency(order.amountPaid)} paid)")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Subtotal: ${Formatters.formatCurrency(order.subtotal)}")
                    Text("GST: ${Formatters.formatCurrency(order.gstTotal)}")
                    Text("Grand Total: ${Formatters.formatCurrency(order.grandTotal)}", fontWeight = FontWeight.Bold, color = BizFlowGreen)

                    if (order.orderStatus != "CANCELLED") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showCancelConfirm = order
                                    selectedOrderDetail = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = BizFlowRose, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel Order", color = BizFlowRose, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    selectedOrderDetail = null
                                    viewModel.navigateTo(AppScreen.INVOICES)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = BizFlowBlue)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Invoice", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedOrderDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BizFlowBlue)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Cancel Order Confirmation Dialog
    showCancelConfirm?.let { order ->
        AlertDialog(
            onDismissRequest = { showCancelConfirm = null },
            title = {
                Text("Cancel Order #${order.orderNumber}?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Cancelling this order will automatically restore the deducted product quantities back into your inventory and update market balances.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelOrder(order.id)
                        showCancelConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BizFlowRose)
                ) {
                    Text("Yes, Cancel & Restore Stock")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelConfirm = null }) {
                    Text("Keep Order")
                }
            }
        )
    }
}
