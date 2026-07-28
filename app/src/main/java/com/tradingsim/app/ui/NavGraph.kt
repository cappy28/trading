package com.tradingsim.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tradingsim.app.ui.ViewModelFactory
import com.tradingsim.app.ui.screens.history.HistoryScreen
import com.tradingsim.app.ui.screens.history.HistoryViewModel
import com.tradingsim.app.ui.screens.main.MainScreen
import com.tradingsim.app.ui.screens.main.MainViewModel
import com.tradingsim.app.ui.screens.portfolio.PortfolioScreen
import com.tradingsim.app.ui.screens.portfolio.PortfolioViewModel
import com.tradingsim.app.ui.screens.settings.SettingsScreen
import com.tradingsim.app.ui.screens.settings.SettingsViewModel
import com.tradingsim.app.ui.theme.AccentBlue
import com.tradingsim.app.ui.theme.CardDark
import com.tradingsim.app.ui.theme.TextMuted

@Composable
fun AppNavGraph(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = com.tradingsim.app.ui.theme.BackgroundDark,
        bottomBar = {
            NavigationBar(containerColor = CardDark) {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                Screen.bottomBarScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(screen), contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CardDark
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Main.route) {
                val vm: MainViewModel = viewModel(factory = viewModelFactory)
                MainScreen(vm)
            }
            composable(Screen.Portfolio.route) {
                val vm: PortfolioViewModel = viewModel(factory = viewModelFactory)
                PortfolioScreen(vm)
            }
            composable(Screen.History.route) {
                val vm: HistoryViewModel = viewModel(factory = viewModelFactory)
                HistoryScreen(vm)
            }
            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel(factory = viewModelFactory)
                SettingsScreen(vm)
            }
        }
    }
}

private fun iconFor(screen: Screen) = when (screen) {
    Screen.Main -> Icons.Filled.BarChart
    Screen.Portfolio -> Icons.Filled.Wallet
    Screen.History -> Icons.Filled.History
    Screen.Settings -> Icons.Filled.Settings
}
