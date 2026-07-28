package com.tradingsim.app.ui.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingsim.app.data.model.PortfolioStats
import com.tradingsim.app.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PortfolioViewModel(portfolioRepository: PortfolioRepository) : ViewModel() {

    val stats: StateFlow<PortfolioStats> = portfolioRepository.observeStats()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PortfolioStats(0.0, 0.0, 0.0, 0, 0.0, 0.0, 0.0, null, null)
        )
}
