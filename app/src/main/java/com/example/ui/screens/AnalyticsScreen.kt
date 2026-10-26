package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BizFlowViewModel
import com.example.ui.components.Formatters
import com.example.ui.components.MetricCard
import com.example.ui.theme.BizFlowBlue
import com.example.ui.theme.BizFlowBlueContainer
import com.example.ui.theme.BizFlowGreen
import com.example.ui.theme.BizFlowGreenLight
import com.example.ui.theme.BizFlowRose
import com.example.ui.theme.BizFlowRoseLight
import java.util.Calendar

@Composable
fun AnalyticsScreen(
    viewModel: BizFlowViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()

    val validOrders = orders.filter { it.orderStatus != "CANCELLED" }
    val totalRevenue = validOrders.sumOf { it.grandTotal }
    val avgOrderValue = if (validOrders.isNotEmpty()) totalRevenue / validOrders.size else 0.0

    // Compute sales in 7 day intervals
    val now = System.currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L

    val salesPerDay = (0..6).map { dayOffset ->
        val dayStart = now - ((6 - dayOffset + 1) * oneDayMs)
        val dayEnd = now - ((6 - dayOffset) * oneDayMs)
        val dayTotal = validOrders.filter { it.createdAt in dayStart..dayEnd }.sumOf { it.grandTotal }
        dayTotal
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        item {
            Text(
                text = "Business Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Key performance indicators and sales trends",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Gross Revenue",
                    value = Formatters.formatCurrency(totalRevenue),
                    subtitle = "${validOrders.size} orders finalized",
                    icon = Icons.Default.TrendingUp,
                    iconTint = BizFlowGreen,
                    iconBg = BizFlowGreenLight,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Avg Order Value",
                    value = Formatters.formatCurrency(avgOrderValue),
                    subtitle = "Per sales order",
                    icon = Icons.Default.ShoppingCart,
                    iconTint = BizFlowBlue,
                    iconBg = BizFlowBlueContainer,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Weekly Sales Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Last 7 Days Sales Trend",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Daily order volume overview",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val maxSale = (salesPerDay.maxOrNull() ?: 1.0).coerceAtLeast(1000.0)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val barWidth = size.width / (salesPerDay.size * 2f)
                        val step = size.width / salesPerDay.size

                        salesPerDay.forEachIndexed { i, amount ->
                            val heightRatio = (amount / maxSale).toFloat().coerceIn(0.08f, 1.0f)
                            val barHeight = size.height * heightRatio
                            val left = i * step + (step - barWidth) / 2f
                            val top = size.height - barHeight

                            drawRoundRect(
                                color = Color(0xFF2563EB),
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("6d ago", "5d ago", "4d ago", "3d ago", "2d ago", "Yday", "Today").forEach {
                            Text(it, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Top Customers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Top Wholesale Customers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val topClients = customers.sortedByDescending { it.totalPurchased }.take(4)
                    topClients.forEachIndexed { index, c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. ${c.name} (${c.totalOrders} orders)",
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            Text(
                                text = Formatters.formatCurrency(c.totalPurchased),
                                fontWeight = FontWeight.Bold,
                                color = BizFlowGreen,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Top Catalog Products
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Catalog Inventory Value",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val totalStockValue = products.sumOf { it.currentStock * it.sellingPrice }
                    Text(
                        text = "Total Selling Valuation: ${Formatters.formatCurrency(totalStockValue)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BizFlowBlue
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    products.take(4).forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(p.name, fontSize = 13.sp)
                            Text("${p.currentStock} ${p.unit} in stock", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
