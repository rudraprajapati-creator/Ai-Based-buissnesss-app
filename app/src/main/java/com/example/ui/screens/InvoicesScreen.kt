package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Invoice
import com.example.ui.BizFlowViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusType
import com.example.ui.theme.BizFlowBlue
import com.example.ui.theme.BizFlowGreen
import com.example.ui.theme.BizFlowNavy
import com.example.ui.theme.BizFlowRose

@Composable
fun InvoicesScreen(
    viewModel: BizFlowViewModel,
    modifier: Modifier = Modifier
) {
    val invoices by viewModel.invoices.collectAsState()
    val profile by viewModel.profile.collectAsState()
    var selectedInvoice by remember { mutableStateOf<Invoice?>(null) }
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("invoices_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        item {
            Text(
                text = "Tax Invoices",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "GST compliant billing for wholesale & retail clients",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (invoices.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Receipt,
                    title = "No Invoices Yet",
                    message = "Invoices are automatically generated whenever you confirm an order."
                )
            }
        } else {
            items(invoices) { invoice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable { selectedInvoice = invoice },
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
                                    text = invoice.invoiceNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = BizFlowBlue
                                )
                                Text(
                                    text = "To: ${invoice.customerName} • ${Formatters.formatDate(invoice.issueDate)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val statusType = when (invoice.status) {
                                "PAID" -> StatusType.SUCCESS
                                "PARTIALLY_PAID" -> StatusType.WARNING
                                "CANCELLED" -> StatusType.ERROR
                                else -> StatusType.WARNING
                            }
                            StatusBadge(text = invoice.status, statusType = statusType)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grand Total: ${Formatters.formatCurrency(invoice.grandTotal)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = BizFlowGreen
                            )

                            if (invoice.balanceDue > 0) {
                                Text(
                                    text = "Balance Due: ${Formatters.formatCurrency(invoice.balanceDue)}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = BizFlowRose
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Professional Indian GST Invoice Preview Modal
    selectedInvoice?.let { inv ->
        AlertDialog(
            onDismissRequest = { selectedInvoice = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tax Invoice Preview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    StatusBadge(
                        text = inv.status,
                        statusType = if (inv.status == "PAID") StatusType.SUCCESS else StatusType.WARNING
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    // Header
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = profile.name.ifBlank { "Shree Ganesh Hardware" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BizFlowNavy
                            )
                            Text(
                                text = profile.address,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Phone: ${profile.phone} • Email: ${profile.email}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (profile.gstNumber.isNotBlank()) {
                                Text(
                                    text = "GSTIN: ${profile.gstNumber}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = BizFlowBlue
                                )
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Metadata
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Billed To:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(inv.customerName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                if (inv.customerPhone.isNotBlank()) Text(inv.customerPhone, fontSize = 11.sp)
                                if (inv.customerAddress.isNotBlank()) Text(inv.customerAddress, fontSize = 11.sp)
                                if (inv.customerGst.isNotBlank()) Text("GSTIN: ${inv.customerGst}", fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Invoice #: ${inv.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Date: ${Formatters.formatDate(inv.issueDate)}", fontSize = 11.sp)
                                Text("Due Date: ${Formatters.formatDate(inv.dueDate)}", fontSize = 11.sp)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Summary breakdown
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Taxable Amount (Subtotal):", fontSize = 12.sp)
                                Text(Formatters.formatCurrency(inv.subtotal), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CGST (Central Tax 9%):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Formatters.formatCurrency(inv.taxTotal / 2), fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SGST (State Tax 9%):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Formatters.formatCurrency(inv.taxTotal / 2), fontSize = 11.sp)
                            }
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Invoice Total:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(Formatters.formatCurrency(inv.grandTotal), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BizFlowGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount Paid:", fontSize = 12.sp)
                                Text(Formatters.formatCurrency(inv.amountPaid), fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Balance Due:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BizFlowRose)
                                Text(Formatters.formatCurrency(inv.balanceDue), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BizFlowRose)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Tax Invoice ${inv.invoiceNumber} from ${profile.name}\n" +
                                    "Customer: ${inv.customerName}\n" +
                                    "Total: ${Formatters.formatCurrency(inv.grandTotal)}\n" +
                                    "Balance Due: ${Formatters.formatCurrency(inv.balanceDue)}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Invoice via"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BizFlowBlue)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedInvoice = null }) {
                    Text("Close")
                }
            }
        )
    }
}
