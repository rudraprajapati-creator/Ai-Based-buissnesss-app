package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.data.model.Customer
import com.example.ui.BizFlowViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusType
import com.example.ui.theme.BizFlowBlue
import com.example.ui.theme.BizFlowGreen
import com.example.ui.theme.BizFlowGreenLight
import com.example.ui.theme.BizFlowRose
import com.example.ui.theme.BizFlowRoseLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    viewModel: BizFlowViewModel,
    modifier: Modifier = Modifier
) {
    val payments by viewModel.payments.collectAsState()
    val customers by viewModel.customers.collectAsState()
    var showRecordDialog by remember { mutableStateOf(false) }

    val totalCollected = payments.sumOf { it.amount }
    val totalPending = customers.sumOf { it.outstandingAmount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showRecordDialog = true },
                containerColor = BizFlowGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("record_payment_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Collect Payment", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("payments_screen"),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
        ) {
            item {
                Text(
                    text = "Payments & Collections",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track cash, UPI (PhonePe, GPay, Paytm) and bank transfers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Collected",
                        value = Formatters.formatCurrency(totalCollected),
                        subtitle = "${payments.size} transactions",
                        icon = Icons.Default.Payments,
                        iconTint = BizFlowGreen,
                        iconBg = BizFlowGreenLight,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Pending Market Dues",
                        value = Formatters.formatCurrency(totalPending),
                        subtitle = "From ${customers.count { it.outstandingAmount > 0 }} debtors",
                        icon = Icons.Default.Payments,
                        iconTint = BizFlowRose,
                        iconBg = BizFlowRoseLight,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (payments.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Payments,
                        title = "No Payments Logged",
                        message = "Tap 'Collect Payment' to record your first cash or UPI receipt."
                    )
                }
            } else {
                items(payments) { payment ->
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
                            Column {
                                Text(
                                    text = payment.customerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${payment.paymentMethod} • ${Formatters.formatDateTime(payment.paymentDate)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (payment.referenceId.isNotBlank()) {
                                    Text(
                                        text = "Ref: ${payment.referenceId}",
                                        fontSize = 11.sp,
                                        color = BizFlowBlue
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+ " + Formatters.formatCurrency(payment.amount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = BizFlowGreen
                                )
                                StatusBadge(text = "RECEIVED", statusType = StatusType.SUCCESS)
                            }
                        }
                    }
                }
            }
        }
    }

    // Record Payment Dialog
    if (showRecordDialog) {
        var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }
        var isDropdownExpanded by remember { mutableStateOf(false) }
        var amountText by remember { mutableStateOf("") }
        var paymentMethod by remember { mutableStateOf("UPI") }
        var referenceId by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        val paymentMethods = listOf("UPI", "Cash", "Bank Transfer", "Card", "Cheque")

        AlertDialog(
            onDismissRequest = { showRecordDialog = false },
            title = {
                Text("Record Customer Payment", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn {
                    item {
                        Text("Select Customer *", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))

                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCustomer?.let { "${it.name} (Due: ${Formatters.formatCurrency(it.outstandingAmount)})" } ?: "Select Customer",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false }
                            ) {
                                customers.forEach { customer ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(customer.name, fontWeight = FontWeight.SemiBold)
                                                Text("Due: ${Formatters.formatCurrency(customer.outstandingAmount)}", fontSize = 11.sp, color = BizFlowRose)
                                            }
                                        },
                                        onClick = {
                                            selectedCustomer = customer
                                            if (customer.outstandingAmount > 0) {
                                                amountText = customer.outstandingAmount.toInt().toString()
                                            }
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Payment Amount (₹) *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            paymentMethods.take(3).forEach { method ->
                                Button(
                                    onClick = { paymentMethod = method },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (paymentMethod == method) BizFlowGreen else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = method,
                                        fontSize = 11.sp,
                                        color = if (paymentMethod == method) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = referenceId,
                            onValueChange = { referenceId = it },
                            label = { Text("UPI Reference / UTR / Cheque #") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        val customer = selectedCustomer
                        if (customer != null && amount > 0) {
                            viewModel.recordPayment(
                                customerId = customer.id,
                                customerName = customer.name,
                                amount = amount,
                                method = paymentMethod,
                                refId = referenceId,
                                notes = notes,
                                onDone = { showRecordDialog = false }
                            )
                        }
                    },
                    enabled = selectedCustomer != null && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = BizFlowGreen)
                ) {
                    Text("Save Receipt")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRecordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
