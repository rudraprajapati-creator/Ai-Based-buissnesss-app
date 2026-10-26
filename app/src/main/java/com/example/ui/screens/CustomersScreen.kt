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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import com.example.data.model.Customer
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
fun CustomersScreen(
    viewModel: BizFlowViewModel,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var selectedCustomerDetail by remember { mutableStateOf<Customer?>(null) }

    val filtered = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery) ||
        it.address.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BizFlowBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("customers_screen"),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
        ) {
            item {
                Text(
                    text = "Customer Directory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${customers.size} registered clients • Total Dues: ${Formatters.formatCurrency(customers.sumOf { it.outstandingAmount })}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, phone or address...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Person,
                        title = "No Customers Found",
                        message = if (searchQuery.isNotBlank()) "No customer matched \"$searchQuery\"." else "Tap '+' to add your first customer."
                    )
                }
            } else {
                items(filtered) { customer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { selectedCustomerDetail = customer },
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = BizFlowBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = customer.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        if (customer.phone.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Call,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = customer.phone,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                if (customer.outstandingAmount > 0) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = Formatters.formatCurrency(customer.outstandingAmount),
                                            fontWeight = FontWeight.Bold,
                                            color = BizFlowRose,
                                            fontSize = 14.sp
                                        )
                                        StatusBadge(text = "DUE", statusType = StatusType.WARNING)
                                    }
                                } else {
                                    StatusBadge(text = "CLEAR", statusType = StatusType.SUCCESS)
                                }
                            }

                            if (customer.address.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = customer.address,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${customer.totalOrders} Orders • ${Formatters.formatCurrency(customer.totalPurchased)} Total",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row {
                                    IconButton(
                                        onClick = { customerToEdit = customer },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteCustomer(customer) },
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
    }

    // Customer Detail Dialog
    selectedCustomerDetail?.let { cust ->
        AlertDialog(
            onDismissRequest = { selectedCustomerDetail = null },
            title = {
                Text(cust.name, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Phone: ${cust.phone.ifBlank { "N/A" }}")
                    Text("Email: ${cust.email.ifBlank { "N/A" }}")
                    Text("Address: ${cust.address.ifBlank { "N/A" }}")
                    if (cust.gstNumber.isNotBlank()) Text("GSTIN: ${cust.gstNumber}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Orders: ${cust.totalOrders}")
                    Text("Total Purchases: ${Formatters.formatCurrency(cust.totalPurchased)}")
                    Text("Outstanding Balance: ${Formatters.formatCurrency(cust.outstandingAmount)}", color = if (cust.outstandingAmount > 0) BizFlowRose else BizFlowGreen, fontWeight = FontWeight.Bold)
                    if (cust.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Notes: ${cust.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAiOrderInput("Order for ${cust.name}: ")
                        selectedCustomerDetail = null
                        viewModel.navigateTo(AppScreen.AI_ORDER)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BizFlowBlue)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Order")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedCustomerDetail = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Add or Edit Customer Dialog
    if (showAddDialog || customerToEdit != null) {
        val editing = customerToEdit
        var name by remember(editing) { mutableStateOf(editing?.name ?: "") }
        var phone by remember(editing) { mutableStateOf(editing?.phone ?: "") }
        var email by remember(editing) { mutableStateOf(editing?.email ?: "") }
        var address by remember(editing) { mutableStateOf(editing?.address ?: "") }
        var gst by remember(editing) { mutableStateOf(editing?.gstNumber ?: "") }
        var notes by remember(editing) { mutableStateOf(editing?.notes ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                customerToEdit = null
            },
            title = {
                Text(if (editing != null) "Edit Customer" else "Add New Customer", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Customer Name *") },
                            modifier = Modifier.fillMaxWidth().testTag("cust_input_name")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth().testTag("cust_input_phone")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Billing / Delivery Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = gst,
                            onValueChange = { gst = it },
                            label = { Text("GSTIN (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (e.g. credit terms)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            if (editing != null) {
                                viewModel.updateCustomer(
                                    editing.copy(name = name, phone = phone, email = email, address = address, gstNumber = gst, notes = notes)
                                )
                            } else {
                                viewModel.createCustomer(
                                    Customer(name = name, phone = phone, email = email, address = address, gstNumber = gst, notes = notes)
                                )
                            }
                            showAddDialog = false
                            customerToEdit = null
                        }
                    },
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BizFlowBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showAddDialog = false
                    customerToEdit = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
