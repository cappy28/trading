package com.tradingsim.app.data.repository

import com.tradingsim.app.data.local.dao.TradingDao
import com.tradingsim.app.data.local.entity.ClosedTradeEntity
import com.tradingsim.app.data.local.entity.OpenPositionEntity
import com.tradingsim.app.data.local.entity.PortfolioStateEntity
import com.tradingsim.app.data.model.ClosedTrade
import com.tradingsim.app.data.model.OpenPosition
import com.tradingsim.app.data.model.PortfolioStats
import com.tradingsim.app.data.model.PositionSide
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PortfolioRepository(private val dao: TradingDao) {

    fun observeOpenPositions(): Flow<List<OpenPosition>> =
        dao.observeOpenPositions().map { list -> list.map { it.toModel() } }

    fun observeClosedTrades(): Flow<List<ClosedTrade>> =
        dao.observeClosedTrades().map { list -> list.map { it.toModel() } }

    fun observeCapital(): Flow<Double> =
        dao.observePortfolioState().map { it?.currentCapital ?: 0.0 }

    fun observeStats(): Flow<PortfolioStats> =
        combine(dao.observeClosedTrades(), dao.observePortfolioState()) { trades, state ->
            val closed = trades.map { it.toModel() }
            val wins = closed.filter { it.pnl > 0 }
            val losses = closed.filter { it.pnl < 0 }
            PortfolioStats(
                currentCapital = state?.currentCapital ?: 0.0,
                startingCapital = state?.startingCapital ?: 0.0,
                totalPnl = closed.sumOf { it.pnl },
                tradeCount = closed.size,
                winRate = if (closed.isNotEmpty()) wins.size.toDouble() / closed.size * 100.0 else 0.0,
                totalProfit = wins.sumOf { it.pnl },
                totalLoss = losses.sumOf { it.pnl },
                bestTrade = closed.maxByOrNull { it.pnl },
                worstTrade = closed.minByOrNull { it.pnl }
            )
        }

    suspend fun setStartingCapitalAndReset(startingCapital: Double) {
        dao.clearOpenPositions()
        dao.clearHistory()
        dao.upsertPortfolioState(
            PortfolioStateEntity(currentCapital = startingCapital, startingCapital = startingCapital)
        )
    }

    /** Ouvre une position (achat = LONG, vente à découvert = SHORT). */
    suspend fun openPosition(
        symbol: String,
        side: PositionSide,
        price: Double,
        quantity: Double,
        leverage: Int,
        currentCapital: Double,
        startingCapital: Double
    ) {
        dao.insertOpenPosition(
            OpenPositionEntity(
                symbol = symbol,
                side = side.name,
                entryPrice = price,
                quantity = quantity,
                leverage = leverage,
                openedAtMs = System.currentTimeMillis()
            )
        )
        val cost = price * quantity
        dao.upsertPortfolioState(
            PortfolioStateEntity(
                currentCapital = currentCapital - cost,
                startingCapital = startingCapital
            )
        )
    }

    /** Ferme une position ouverte et calcule le PNL réalisé. */
    suspend fun closePosition(
        position: OpenPosition,
        exitPrice: Double,
        currentCapital: Double,
        startingCapital: Double,
        chartSnapshotPath: String?
    ) {
        val pnl = position.unrealizedPnl(exitPrice)
        val proceeds = position.entryPrice * position.quantity + pnl

        dao.deleteOpenPosition(
            OpenPositionEntity(
                id = position.id,
                symbol = position.symbol,
                side = position.side.name,
                entryPrice = position.entryPrice,
                quantity = position.quantity,
                leverage = position.leverage,
                openedAtMs = position.openedAtMs
            )
        )
        dao.insertClosedTrade(
            ClosedTradeEntity(
                symbol = position.symbol,
                side = position.side.name,
                entryPrice = position.entryPrice,
                exitPrice = exitPrice,
                quantity = position.quantity,
                leverage = position.leverage,
                openedAtMs = position.openedAtMs,
                closedAtMs = System.currentTimeMillis(),
                pnl = pnl,
                chartSnapshotPath = chartSnapshotPath
            )
        )
        dao.upsertPortfolioState(
            PortfolioStateEntity(
                currentCapital = currentCapital + proceeds,
                startingCapital = startingCapital
            )
        )
    }
}

private fun OpenPositionEntity.toModel() = OpenPosition(
    id = id,
    symbol = symbol,
    side = PositionSide.valueOf(side),
    entryPrice = entryPrice,
    quantity = quantity,
    openedAtMs = openedAtMs,
    leverage = leverage
)

private fun ClosedTradeEntity.toModel() = ClosedTrade(
    id = id,
    symbol = symbol,
    side = PositionSide.valueOf(side),
    entryPrice = entryPrice,
    exitPrice = exitPrice,
    quantity = quantity,
    leverage = leverage,
    openedAtMs = openedAtMs,
    closedAtMs = closedAtMs,
    pnl = pnl,
    chartSnapshot = chartSnapshotPath
)
