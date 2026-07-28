package com.tradingsim.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingsim.app.data.model.ClosedTrade
import com.tradingsim.app.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(portfolioRepository: PortfolioRepository) : ViewModel() {

    val trades: StateFlow<List<ClosedTrade>> = portfolioRepository.observeClosedTrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
