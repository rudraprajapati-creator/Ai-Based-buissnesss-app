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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.AppScreen
import com.example.ui.BizFlowViewModel
import com.example.ui.components.Formatters
import com.example.ui.theme.BizFlowAccent
import com.example.ui.theme.BizFlowBackground
import com.example.ui.theme.BizFlowBorder
import com.example.ui.theme.BizFlowCharcoal
import com.example.ui.theme.BizFlowGrayDark
import com.example.ui.theme.BizFlowGrayMuted
import com.example.ui.theme.BizFlowSurface
import com.example.ui.theme.BizFlowSurfaceHover

@Composable
fun AiAssistantScreen(
    viewModel: BizFlowViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.aiMessages.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val suggestedCommands = listOf(
        "Who owes me money?",
        "What did I sell this month?",
        "Which products are low in stock?",
        "Who is my biggest customer?",
        "What were my sales this week?",
        "Show me Rajesh's orders"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BizFlowBackground)
            .testTag("ai_assistant_screen")
    ) {
        // Business Command Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BizFlowSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Business Assistant",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BizFlowCharcoal
                    )
                    Text(
                        text = "Real-time answers strictly derived from database records",
                        fontSize = 11.sp,
                        color = BizFlowGrayMuted
                    )
                }

                IconButton(
                    onClick = { viewModel.clearAiChat() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = BizFlowGrayMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Output History
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isUser) BizFlowSurfaceHover else BizFlowSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isUser) "QUERY" else "DATABASE RESULT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BizFlowGrayMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = Formatters.formatDateTime(msg.timestamp),
                                fontSize = 10.sp,
                                color = BizFlowGrayMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.content,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = BizFlowCharcoal,
                            fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
                        )

                        // If response mentions debtors or stock, offer inline actions
                        if (!isUser && msg.content.contains("outstanding", ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("View Customers", fontSize = 11.sp, color = BizFlowCharcoal)
                                }
                                Button(
                                    onClick = { viewModel.navigateTo(AppScreen.PAYMENTS) },
                                    shape = RoundedCornerShape(4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BizFlowAccent),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Record Payment", fontSize = 11.sp)
                                }
                            }
                        } else if (!isUser && msg.content.contains("low in stock", ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.INVENTORY) },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BizFlowAccent),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Open Inventory", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Command Pills & Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BizFlowSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .padding(bottom = 60.dp)
            ) {
                Text(
                    text = "Common business queries:",
                    fontSize = 11.sp,
                    color = BizFlowGrayMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestedCommands) { cmd ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, BizFlowBorder, RoundedCornerShape(4.dp))
                                .background(BizFlowSurfaceHover, RoundedCornerShape(4.dp))
                                .clickable { viewModel.askAiAssistant(cmd) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(cmd, fontSize = 11.sp, color = BizFlowCharcoal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = { Text("Ask a business question (e.g. Who owes me money?)", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        shape = RoundedCornerShape(4.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (inputQuery.isNotBlank()) {
                                viewModel.askAiAssistant(inputQuery)
                                inputQuery = ""
                            }
                        },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BizFlowAccent),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        enabled = inputQuery.isNotBlank(),
                        modifier = Modifier.testTag("send_ai_query_button")
                    ) {
                        Text("Search", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
