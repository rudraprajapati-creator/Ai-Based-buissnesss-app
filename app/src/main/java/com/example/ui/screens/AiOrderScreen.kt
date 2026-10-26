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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.ui.AppScreen
import com.example.ui.BizFlowViewModel
import com.example.ui.components.Formatters
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
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusDangerBorder
import com.example.ui.theme.StatusDangerText
import com.example.ui.theme.StatusSuccessText
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.StatusWarningBorder
import com.example.ui.theme.StatusWarningText

@Composable
fun AiOrderScreen(
    viewModel: BizFlowViewModel,
    modifier: Modifier = Modifier
) {
    val inputPrompt by viewModel.aiOrderInput.collectAsState()
    val draft by viewModel.parsedDraftOrder.collectAsState()
    val isParsing by viewModel.isAiParsing.collectAsState()

    val sampleInputs = listOf(
        "Rajesh needs 20 10mm bolts and 5 12mm nuts",
        "Amit needs 10 bolts", // Ambiguity demonstration!
        "Give Karan 4 cement bags and 2 screwdrivers",
        "Mahesh ordered 15 PVC Pipes at 240 each"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BizFlowBackground)
            .testTag("ai_order_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp)
    ) {
        item {
            Text(
                text = "Create Order with Natural Language",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BizFlowCharcoal
            )
            Text(
                text = "Type customer order details as spoken. The system verifies catalog, prices, and stock.",
                fontSize = 12.sp,
                color = BizFlowGrayMuted
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Input Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = BizFlowSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { viewModel.updateAiOrderInput(it) },
                        placeholder = {
                            Text(
                                "e.g. Rajesh needs 20 10mm bolts and 5 12mm nuts.",
                                color = BizFlowGrayMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("ai_order_text_field"),
                        shape = RoundedCornerShape(4.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Examples (tap to populate):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = BizFlowGrayMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(sampleInputs) { sample ->
                            Box(
                                modifier = Modifier
                                    .border(1.dp, BizFlowBorder, RoundedCornerShape(4.dp))
                                    .background(BizFlowSurfaceHover, RoundedCornerShape(4.dp))
                                    .clickable {
                                        viewModel.updateAiOrderInput(sample)
                                        viewModel.parseAiOrder(sample)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = sample,
                                    fontSize = 11.sp,
                                    color = BizFlowCharcoal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.parseAiOrder() },
                        enabled = inputPrompt.isNotBlank() && !isParsing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("create_order_ai_button"),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BizFlowAccent)
                    ) {
                        if (isParsing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Parsing Request...", fontSize = 13.sp)
                        } else {
                            Text("Create Draft Order", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ==========================================
        // DRAFT ORDER REVIEW SCREEN (Section 12)
        // ==========================================
        draft?.let { orderDraft ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DRAFT ORDER",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BizFlowCharcoal,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Discard",
                        fontSize = 12.sp,
                        color = BizFlowGrayMuted,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { viewModel.clearDraftOrder() }
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Customer verification row
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = BizFlowSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Customer",
                                    fontSize = 11.sp,
                                    color = BizFlowGrayMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = orderDraft.customerName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BizFlowCharcoal
                                )
                                if (orderDraft.matchedCustomer != null) {
                                    Text(
                                        text = "Phone: ${orderDraft.matchedCustomer.phone} • Outstanding: ${Formatters.formatCurrency(orderDraft.matchedCustomer.outstandingAmount)}",
                                        fontSize = 11.sp,
                                        color = BizFlowGrayMuted
                                    )
                                }
                            }

                            if (orderDraft.isCustomerMissing) {
                                Button(
                                    onClick = {
                                        viewModel.createCustomer(
                                            Customer(name = orderDraft.customerName, notes = "Added via AI Order")
                                        )
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BizFlowAccent),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("+ Create Customer", fontSize = 11.sp)
                                }
                            } else {
                                StatusBadge(text = "VERIFIED", statusType = StatusType.SUCCESS)
                            }
                        }

                        if (orderDraft.isCustomerMissing) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Customer not found. Will be registered upon order confirmation.",
                                fontSize = 11.sp,
                                color = StatusWarningText
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Items breakdown
            itemsIndexed(orderDraft.items) { index, item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = BizFlowSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.hasStockWarning) StatusDangerBorder else BizFlowBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Product Name & Disambiguation Handling
                        if (item.ambiguousMatches.size > 1) {
                            Text(
                                text = "Which product did you mean?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BizFlowCharcoal
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            item.ambiguousMatches.forEach { candidate ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable { viewModel.resolveAmbiguousItem(index, candidate) },
                                    shape = RoundedCornerShape(4.dp),
                                    color = BizFlowSurfaceHover,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(candidate.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${Formatters.formatCurrency(candidate.sellingPrice)} (Stock: ${candidate.currentStock})", fontSize = 11.sp, color = BizFlowGrayMuted)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.rawName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BizFlowCharcoal
                                    )
                                    Text(
                                        text = "${item.quantity} × ${Formatters.formatCurrency(item.unitPrice)}",
                                        fontSize = 12.sp,
                                        color = BizFlowGrayMuted
                                    )
                                }

                                // Stepper
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.updateDraftItemQuantity(index, item.quantity - 1) },
                                        enabled = item.quantity > 1,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                                    }
                                    Text(
                                        text = "${item.quantity}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateDraftItemQuantity(index, item.quantity + 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeDraftItem(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = BizFlowGrayMuted, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }

                        // Stock Availability Section
                        if (item.matchedProduct != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BizFlowSurfaceHover, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Available: ${item.availableStock}  |  Required: ${item.quantity}",
                                    fontSize = 11.sp,
                                    color = if (item.hasStockWarning) StatusDangerText else BizFlowGrayDark,
                                    fontWeight = if (item.hasStockWarning) FontWeight.Bold else FontWeight.Normal
                                )
                                StatusBadge(
                                    text = if (item.hasStockWarning) "INSUFFICIENT" else "IN STOCK",
                                    statusType = if (item.hasStockWarning) StatusType.DANGER else StatusType.SUCCESS
                                )
                            }
                        }

                        if (item.stockWarningMessage != null && item.ambiguousMatches.size <= 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.stockWarningMessage,
                                fontSize = 11.sp,
                                color = StatusDangerText
                            )
                        }

                        Divider(color = BizFlowBorder, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item Total (incl. GST):", fontSize = 11.sp, color = BizFlowGrayMuted)
                            Text(Formatters.formatCurrency(item.total), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BizFlowCharcoal)
                        }
                    }
                }
            }

            // Calculation Breakdown Table
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = BizFlowSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal:", fontSize = 12.sp, color = BizFlowGrayDark)
                            Text(Formatters.formatCurrency(orderDraft.subtotal), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("GST:", fontSize = 12.sp, color = BizFlowGrayDark)
                            Text(Formatters.formatCurrency(orderDraft.gstTotal), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Divider(color = BizFlowBorder, modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BizFlowCharcoal)
                            Text(Formatters.formatCurrency(orderDraft.grandTotal), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BizFlowCharcoal)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Buttons: Edit, Discard, Confirm Order
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.clearDraftOrder() },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Discard", fontSize = 12.sp, color = BizFlowCharcoal)
                    }

                    Button(
                        onClick = {
                            viewModel.confirmAiDraftOrder {
                                viewModel.navigateTo(AppScreen.ORDERS)
                            }
                        },
                        enabled = !orderDraft.hasAmbiguity,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BizFlowAccent),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("confirm_order_button")
                    ) {
                        Text("Confirm Order", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Requires owner confirmation before reducing inventory and issuing invoice.",
                    fontSize = 11.sp,
                    color = BizFlowGrayMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
