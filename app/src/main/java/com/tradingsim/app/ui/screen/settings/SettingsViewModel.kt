package com.tradingsim.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingsim.app.data.local.SettingsDataStore
import com.tradingsim.app.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    val startingCapital: StateFlow<Double> = settingsDataStore.startingCapitalFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsDataStore.DEFAULT_STARTING_CAPITAL)

    /** Change le capital de départ ET réinitialise le portefeuille (positions, historique, capital). */
    fun setStartingCapital(value: Double) {
        viewModelScope.launch {
            settingsDataStore.setStartingCapital(value)
            portfolioRepository.setStartingCapitalAndReset(value)
        }
    }

    /** Efface toutes les données (positions, historique) en conservant le capital de départ actuel. */
    fun clearAllData() {
        viewModelScope.launch {
            portfolioRepository.setStartingCapitalAndReset(startingCapital.value)
        }
    }
}
