package com.hasabati.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.hasabati.app.ui.agent.AgentScreen
import com.hasabati.app.ui.customers.CustomerDetailScreen
import com.hasabati.app.ui.customers.CustomersListScreen
import com.hasabati.app.ui.dashboard.DashboardScreen
import com.hasabati.app.ui.expenses.ExpensesScreen
import com.hasabati.app.ui.orders.NewOrderScreen
import com.hasabati.app.ui.orders.OrderDetailScreen
import com.hasabati.app.ui.orders.OrdersListScreen
import com.hasabati.app.ui.reports.ReportsScreen
import com.hasabati.app.ui.settings.SettingsScreen
import com.hasabati.app.ui.theme.PurpleAccent
import com.hasabati.app.ui.theme.TextSecondaryGray

private sealed class TopLevelDest(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Dashboard : TopLevelDest("dashboard", "الرئيسية", Icons.Outlined.Home, Icons.Filled.Home)
    object Orders : TopLevelDest("orders", "الطلبات", Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart)
    object Customers : TopLevelDest("customers", "العملاء", Icons.Outlined.People, Icons.Filled.People)
    object Treasury : TopLevelDest("treasury", "الخزينة", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet)
    object More : TopLevelDest("more", "المزيد", Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz)
}

private val bottomItems = listOf(TopLevelDest.Dashboard, TopLevelDest.Orders, TopLevelDest.Customers, TopLevelDest.Treasury, TopLevelDest.More)

@Composable
fun HasabatiNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                bottomItems.forEach { dest ->
                    val selected = currentRoute?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            // ننتقل مباشرة ونمسح أي شاشات وسيطة (تفاصيل طلب/عميلة...) حتى يعمل
                            // زر "الرئيسية" وباقي التبويبات فوراً من أي مكان بالتطبيق بدون أي تعقيد.
                            navController.navigate(dest.route) {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(if (selected) dest.selectedIcon else dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PurpleAccent,
                            selectedTextColor = PurpleAccent,
                            unselectedIconColor = TextSecondaryGray,
                            unselectedTextColor = TextSecondaryGray,
                            indicatorColor = PurpleAccent.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDest.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(TopLevelDest.Dashboard.route) {
                DashboardScreen(
                    onNewOrder = { navController.navigate("new_order") },
                    onCollectPayment = { navController.navigate(TopLevelDest.Customers.route) },
                    onAddExpense = { navController.navigate("expenses") },
                    onPayAgent = { navController.navigate("agent") },
                    onOpenTreasury = { navController.navigate(TopLevelDest.Treasury.route) },
                    onOpenAgent = { navController.navigate("agent") },
                    onOpenCustomersWithBalance = { navController.navigate(TopLevelDest.Customers.route) },
                    onOpenArrivedOrders = { navController.navigate(TopLevelDest.Orders.route) }
                )
            }
            composable(TopLevelDest.Orders.route) {
                OrdersListScreen(
                    onOpenOrder = { id -> navController.navigate("order_detail/$id") },
                    onNewOrder = { navController.navigate("new_order") }
                )
            }
            composable("new_order") {
                NewOrderScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.popBackStack()
                        navController.navigate("order_detail/$id")
                    }
                )
            }
            composable(
                "order_detail/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
                OrderDetailScreen(orderId = orderId, onBack = { navController.popBackStack() })
            }
            composable(TopLevelDest.Customers.route) {
                CustomersListScreen(onOpenCustomer = { id -> navController.navigate("customer_detail/$id") })
            }
            composable(
                "customer_detail/{customerId}",
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                CustomerDetailScreen(
                    customerId = customerId,
                    onBack = { navController.popBackStack() },
                    onOpenOrder = { id -> navController.navigate("order_detail/$id") }
                )
            }
            composable(TopLevelDest.Treasury.route) {
                com.hasabati.app.ui.treasury.TreasuryScreen()
            }
            composable(TopLevelDest.More.route) {
                MoreScreen(
                    onOpenAgent = { navController.navigate("agent") },
                    onOpenExpenses = { navController.navigate("expenses") },
                    onOpenReports = { navController.navigate("reports") },
                    onOpenSettings = { navController.navigate("settings") }
                )
            }
            composable("agent") { AgentScreen() }
            composable("expenses") { ExpensesScreen() }
            composable("reports") { ReportsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
