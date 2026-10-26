package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.BizFlowViewModel
import com.example.ui.components.Formatters
import com.example.ui.components.MetricStat
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusType
import com.example.ui.theme.BizFlowAccent
import com.example.ui.theme.BizFlowBackground
import com.example.ui.theme.BizFlowBorder
import com.example.ui.theme.BizFlowCharcoal
import com.example.ui.theme.BizFlowGrayDark
import com.example.ui.theme.BizFlowGrayMuted
import com.example.ui.theme.BizFlowSurface
import com.example.ui.theme.BizFlowSurfaceHover

@Composable
fun DashboardScreen(
    viewModel: BizFlowViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BizFlowBackground)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp)
    ) {
        // Top Header with greeting & primary actions
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Good morning, ${profile.ownerName.ifBlank { "Owner" }}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BizFlowCharcoal
                        )
                        Text(
                            text = profile.name.ifBlank { "BizFlow Hardware & Tools" },
                            fontSize = 13.sp,
                            color = BizFlowGrayMuted
                        )
                    }

                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.AI_ORDER) },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BizFlowAccent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("dashboard_new_order_btn")
                        ) {
                            Text("+ New Order", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text("+ Customer", fontSize = 12.sp, color = BizFlowCharcoal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Restrained Compact Business Summary (3 clean stats)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricStat(
                        label = "Today's sales",
                        value = Formatters.formatCurrency(metrics.todaySales),
                        subtext = "Confirmed orders",
                        modifier = Modifier.weight(1f)
                    )
                    MetricStat(
                        label = "Pending orders",
                        value = "${metrics.pendingOrdersCount}",
                        subtext = "To be delivered",
                        modifier = Modifier.weight(1f)
                    )
                    MetricStat(
                        label = "Outstanding payments",
                        value = Formatters.formatCurrency(metrics.pendingPaymentsAmount),
                        subtext = "Market receivables",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Compact Natural Language Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.AI_ORDER) },
                    shape = RoundedCornerShape(6.dp),
                    color = BizFlowSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Quick Order (Natural Language)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BizFlowCharcoal
                            )
                            Text(
                                text = "Type \"Rajesh needs 20 10mm bolts and 5 nuts\"",
                                fontSize = 12.sp,
                                color = BizFlowGrayMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = BizFlowGrayDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Section: RECENT ORDERS (Clean tabular list)
        item {
            SectionHeader(
                title = "RECENT ORDERS",
                actionText = "View all",
                onActionClick = { viewModel.navigateTo(AppScreen.ORDERS) }
            )
        }

        if (orders.isEmpty()) {
            item {
                Text(
                    text = "No orders created yet.",
                    fontSize = 12.sp,
                    color = BizFlowGrayMuted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = BizFlowSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
                ) {
                    Column {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BizFlowSurfaceHover)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ORDER / CUSTOMER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BizFlowGrayMuted, modifier = Modifier.weight(2f))
                            Text("AMOUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BizFlowGrayMuted, modifier = Modifier.weight(1.2f))
                            Text("STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BizFlowGrayMuted, modifier = Modifier.weight(1f))
                        }
                        Divider(color = BizFlowBorder)

                        orders.take(5).forEachIndexed { idx, order ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.navigateTo(AppScreen.ORDERS) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(2f)) {
                                    Text(text = order.customerName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BizFlowCharcoal)
                                    Text(text = "#${order.orderNumber} • ${Formatters.formatDate(order.createdAt)}", fontSize = 11.sp, color = BizFlowGrayMuted)
                                }
                                Text(
                                    text = Formatters.formatCurrency(order.grandTotal),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BizFlowCharcoal,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    val badgeType = when (order.orderStatus) {
                                        "CONFIRMED" -> StatusType.SUCCESS
                                        "DELIVERED" -> StatusType.INFO
                                        "CANCELLED" -> StatusType.DANGER
                                        else -> StatusType.WARNING
                                    }
                                    StatusBadge(text = order.orderStatus, statusType = badgeType)
                                }
                            }
                            if (idx < orders.take(5).size - 1) {
                                Divider(color = BizFlowBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        // Section: PAYMENTS TO FOLLOW UP
        val debtors = customers.filter { it.outstandingAmount > 0 }.sortedByDescending { it.outstandingAmount }
        if (debtors.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(
                    title = "PAYMENTS TO FOLLOW UP",
                    actionText = "Manage",
                    onActionClick = { viewModel.navigateTo(AppScreen.PAYMENTS) }
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = BizFlowSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
                ) {
                    Column {
                        debtors.take(3).forEachIndexed { idx, debtor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(debtor.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BizFlowCharcoal)
                                    Text(debtor.phone.ifBlank { "No phone recorded" }, fontSize = 11.sp, color = BizFlowGrayMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.formatCurrency(debtor.outstandingAmount),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BizFlowCharcoal
                                    )
                                    StatusBadge(text = "UNPAID", statusType = StatusType.WARNING)
                                }
                            }
                            if (idx < debtors.take(3).size - 1) {
                                Divider(color = BizFlowBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        // Section: LOW STOCK
        if (lowStock.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeader(
                    title = "LOW STOCK",
                    actionText = "Inventory",
                    onActionClick = { viewModel.navigateTo(AppScreen.INVENTORY) }
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = BizFlowSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
                ) {
                    Column {
                        lowStock.take(3).forEachIndexed { idx, product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BizFlowCharcoal)
                                    Text("Min required: ${product.minimumStock} ${product.unit}", fontSize = 11.sp, color = BizFlowGrayMuted)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${product.currentStock} ${product.unit}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BizFlowCharcoal,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    StatusBadge(
                                        text = if (product.currentStock == 0) "OUT OF STOCK" else "LOW",
                                        statusType = if (product.currentStock == 0) StatusType.DANGER else StatusType.WARNING
                                    )
                                }
                            }
                            if (idx < lowStock.take(3).size - 1) {
                                Divider(color = BizFlowBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}
