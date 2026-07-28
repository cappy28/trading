package com.tradingsim.app.data.model

enum class PositionSide { LONG, SHORT }

/**
 * Position ouverte (en cours) sur le portefeuille virtuel.
 */
data class OpenPosition(
    val id: Long = 0L,
    val symbol: String,
    val side: PositionSide,
    val entryPrice: Double,
    val quantity: Double,
    val openedAtMs: Long,
    val leverage: Int = 1
) {
    fun unrealizedPnl(currentPrice: Double): Double {
        val diff = currentPrice - entryPrice
        val signed = if (side == PositionSide.LONG) diff else -diff
        return signed * quantity * leverage
    }
}

/**
 * Trade clôturé, conservé dans l'historique.
 */
data class ClosedTrade(
    val id: Long = 0L,
    val symbol: String,
    val side: PositionSide,
    val entryPrice: Double,
    val exitPrice: Double,
    val quantity: Double,
    val leverage: Int,
    val openedAtMs: Long,
    val closedAtMs: Long,
    val pnl: Double,
    val chartSnapshot: String? = null // chemin du PNG capturé au moment de la clôture
)

data class PortfolioStats(
    val currentCapital: Double,
    val startingCapital: Double,
    val totalPnl: Double,
    val tradeCount: Int,
    val winRate: Double,
    val totalProfit: Double,
    val totalLoss: Double,
    val bestTrade: ClosedTrade?,
    val worstTrade: ClosedTrade?
)
