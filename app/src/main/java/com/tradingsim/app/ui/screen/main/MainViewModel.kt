package com.tradingsim.app.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingsim.app.data.model.Asset
import com.tradingsim.app.data.model.Candle
import com.tradingsim.app.data.model.OpenPosition
import com.tradingsim.app.data.model.PositionSide
import com.tradingsim.app.data.model.Timeframe
import com.tradingsim.app.data.repository.MarketRepository
import com.tradingsim.app.data.repository.MarketState
import com.tradingsim.app.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainScreenState(
    val asset: Asset = Asset("BTCUSDT", "BTC / USDT"),
    val timeframe: Timeframe = Timeframe.M15,
    val candles: List<Candle> = emptyList(),
    val currentPrice: Double = 0.0,
    val changePercent: Double = 0.0,
    val isOnline: Boolean = false,
    val openPosition: OpenPosition? = null,
    val capital: Double = 0.0,
    val startingCapital: Double = 0.0,
    val tradeQuantityUsd: Double = 100.0
)

class MainViewModel(
    private val marketRepository: MarketRepository,
    private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val asset = MutableStateFlow(Asset("BTCUSDT", "BTC / USDT"))
    private val timeframe = MutableStateFlow(Timeframe.M15)

    private val assetTimeframeFlow = combine(asset, timeframe) { a, tf -> a to tf }

    private val marketFlow = assetTimeframeFlow
        .flatMapLatest { (a, tf) -> marketRepository.observeMarket(a.symbol, tf) }

    val state: StateFlow<MainScreenState> = combine(
        assetTimeframeFlow,
        marketFlow,
        portfolioRepository.observeOpenPositions(),
        portfolioRepository.observeCapital(),
        portfolioRepository.observeStats()
    ) { (a, tf), market, positions, capital, stats ->
        MainScreenState(
            asset = a,
            timeframe = tf,
            candles = market.candles,
            currentPrice = market.currentPrice,
            changePercent = market.changePercent24h,
            isOnline = market.isOnline,
            openPosition = positions.firstOrNull { it.symbol == a.symbol },
            capital = capital,
            startingCapital = stats.startingCapital
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenState())

    fun selectTimeframe(tf: Timeframe) {
        timeframe.value = tf
    }

    fun buy() = openPosition(PositionSide.LONG)
    fun sell() = openPosition(PositionSide.SHORT)

    private fun openPosition(side: PositionSide) {
        val current = state.value
        if (current.currentPrice <= 0.0 || current.openPosition != null) return
        val quantity = current.tradeQuantityUsd / current.currentPrice
        viewModelScope.launch {
            portfolioRepository.openPosition(
                symbol = current.asset.symbol,
                side = side,
                price = current.currentPrice,
                quantity = quantity,
                leverage = 1,
                currentCapital = current.capital,
                startingCapital = current.startingCapital
            )
        }
    }

    fun closePosition(chartSnapshotPath: String? = null) {
        val current = state.value
        val position = current.openPosition ?: return
        viewModelScope.launch {
            portfolioRepository.closePosition(
                position = position,
                exitPrice = current.currentPrice,
                currentCapital = current.capital,
                startingCapital = current.startingCapital,
                chartSnapshotPath = chartSnapshotPath
            )
        }
    }
}
