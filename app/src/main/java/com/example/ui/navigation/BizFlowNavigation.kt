package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.BizFlowViewModel
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AiOrderScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BizFlowAccent
import com.example.ui.theme.BizFlowBackground
import com.example.ui.theme.BizFlowBorder
import com.example.ui.theme.BizFlowCharcoal
import com.example.ui.theme.BizFlowGrayDark
import com.example.ui.theme.BizFlowGrayMuted
import com.example.ui.theme.BizFlowSurface
import com.example.ui.theme.BizFlowSurfaceHover
import kotlinx.coroutines.launch

data class NavEntry(
    val screen: AppScreen,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BizFlowAppContent(viewModel: BizFlowViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        viewModel.navigateBack()
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissStatusMessage()
        }
    }

    val mainNavEntries = listOf(
        NavEntry(AppScreen.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
        NavEntry(AppScreen.ORDERS, Icons.Default.ShoppingCart, "Orders"),
        NavEntry(AppScreen.CUSTOMERS, Icons.Default.People, "Customers"),
        NavEntry(AppScreen.PRODUCTS, Icons.Default.Store, "Products"),
        NavEntry(AppScreen.INVENTORY, Icons.Default.Inventory, "Inventory"),
        NavEntry(AppScreen.INVOICES, Icons.Default.Receipt, "Invoices"),
        NavEntry(AppScreen.PAYMENTS, Icons.Default.Payments, "Payments"),
        NavEntry(AppScreen.AI_ORDER, Icons.Default.AutoAwesome, "AI Order"),
        NavEntry(AppScreen.AI_ASSISTANT, Icons.Default.AutoAwesome, "AI Assistant"),
        NavEntry(AppScreen.ANALYTICS, Icons.Default.Analytics, "Analytics")
    )

    val bottomNavEntries = listOf(
        NavEntry(AppScreen.SETTINGS, Icons.Default.Settings, "Settings")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = BizFlowSurface
            ) {
                // Calm, clean typographic branding header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "BIZFLOW",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = BizFlowCharcoal,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Business Operations",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = BizFlowGrayMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = profile.name.ifBlank { "Hardware & Wholesale" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BizFlowCharcoal
                    )
                    Text(
                        text = profile.ownerName.ifBlank { "Proprietor" },
                        fontSize = 11.sp,
                        color = BizFlowGrayMuted
                    )
                }

                Divider(color = BizFlowBorder)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    mainNavEntries.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            label = { Text(item.label, fontSize = 13.sp, fontWeight = if (currentScreen == item.screen) FontWeight.Bold else FontWeight.Normal) },
                            selected = currentScreen == item.screen,
                            onClick = {
                                viewModel.navigateTo(item.screen)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = BizFlowSurfaceHover,
                                selectedTextColor = BizFlowAccent,
                                unselectedTextColor = BizFlowCharcoal
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }

                    Divider(color = BizFlowBorder, modifier = Modifier.padding(vertical = 6.dp))

                    bottomNavEntries.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            label = { Text(item.label, fontSize = 13.sp) },
                            selected = currentScreen == item.screen,
                            onClick = {
                                viewModel.navigateTo(item.screen)
                                scope.launch { drawerState.close() }
                            },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentScreen != AppScreen.ONBOARDING) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentScreen.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BizFlowCharcoal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "•  ${profile.name.ifBlank { "BizFlow" }}",
                                    fontSize = 12.sp,
                                    color = BizFlowGrayMuted
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("app_navigation_drawer_button")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = BizFlowCharcoal)
                            }
                        },
                        actions = {
                            if (currentScreen != AppScreen.AI_ORDER) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = BizFlowAccent,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clickable { viewModel.navigateTo(AppScreen.AI_ORDER) }
                                ) {
                                    Text(
                                        text = "+ New Order",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = BizFlowSurface
                        )
                    )
                }
            },
            bottomBar = {
                if (currentScreen != AppScreen.ONBOARDING) {
                    Surface(
                        border = androidx.compose.foundation.BorderStroke(1.dp, BizFlowBorder)
                    ) {
                        NavigationBar(
                            containerColor = BizFlowSurface,
                            tonalElevation = 0.dp,
                            modifier = Modifier.height(58.dp)
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.DASHBOARD,
                                onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", modifier = Modifier.size(18.dp)) },
                                label = { Text("Dashboard", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BizFlowSurfaceHover
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.AI_ORDER,
                                onClick = { viewModel.navigateTo(AppScreen.AI_ORDER) },
                                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Order", modifier = Modifier.size(18.dp)) },
                                label = { Text("AI Order", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BizFlowSurfaceHover
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.ORDERS,
                                onClick = { viewModel.navigateTo(AppScreen.ORDERS) },
                                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Orders", modifier = Modifier.size(18.dp)) },
                                label = { Text("Orders", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BizFlowSurfaceHover
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.CUSTOMERS,
                                onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                                icon = { Icon(Icons.Default.People, contentDescription = "Customers", modifier = Modifier.size(18.dp)) },
                                label = { Text("Customers", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BizFlowSurfaceHover
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen in listOf(
                                    AppScreen.PRODUCTS, AppScreen.INVENTORY, AppScreen.INVOICES,
                                    AppScreen.PAYMENTS, AppScreen.AI_ASSISTANT, AppScreen.ANALYTICS, AppScreen.SETTINGS
                                ),
                                onClick = { scope.launch { drawerState.open() } },
                                icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "Menu", modifier = Modifier.size(18.dp)) },
                                label = { Text("More", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BizFlowSurfaceHover
                                )
                            )
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                    AppScreen.AI_ORDER -> AiOrderScreen(viewModel = viewModel)
                    AppScreen.ORDERS -> OrdersScreen(viewModel = viewModel)
                    AppScreen.CUSTOMERS -> CustomersScreen(viewModel = viewModel)
                    AppScreen.PRODUCTS -> ProductsScreen(viewModel = viewModel)
                    AppScreen.INVENTORY -> InventoryScreen(viewModel = viewModel)
                    AppScreen.INVOICES -> InvoicesScreen(viewModel = viewModel)
                    AppScreen.PAYMENTS -> PaymentsScreen(viewModel = viewModel)
                    AppScreen.AI_ASSISTANT -> AiAssistantScreen(viewModel = viewModel)
                    AppScreen.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                    AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    AppScreen.ONBOARDING -> OnboardingScreen(viewModel = viewModel)
                }
            }
        }
    }
}
