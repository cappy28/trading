package com.tradingsim.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tradingsim.app.TradingSimApplication
import com.tradingsim.app.ui.screens.history.HistoryViewModel
import com.tradingsim.app.ui.screens.main.MainViewModel
import com.tradingsim.app.ui.screens.portfolio.PortfolioViewModel
import com.tradingsim.app.ui.screens.settings.SettingsViewModel

/**
 * Fabrique manuelle de ViewModels : évite d'introduire Hilt/Dagger pour un projet personnel,
 * tout en gardant une architecture MVVM propre et testable.
 */
class ViewModelFactory(private val app: TradingSimApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            MainViewModel::class.java ->
                MainViewModel(app.marketRepository, app.portfolioRepository) as T
            PortfolioViewModel::class.java ->
                PortfolioViewModel(app.portfolioRepository) as T
            HistoryViewModel::class.java ->
                HistoryViewModel(app.portfolioRepository) as T
            SettingsViewModel::class.java ->
                SettingsViewModel(app.settingsDataStore, app.portfolioRepository) as T
            else -> throw IllegalArgumentException("ViewModel inconnu: ${modelClass.name}")
        }
    }
}
