package com.tradingsim.app.ui.navigation

sealed class Screen(val route: String, val label: String) {
    data object Main : Screen("main", "Marché")
    data object Portfolio : Screen("portfolio", "Portefeuille")
    data object History : Screen("history", "Historique")
    data object Settings : Screen("settings", "Paramètres")

    companion object {
        val bottomBarScreens = listOf(Main, Portfolio, History, Settings)
    }
}
