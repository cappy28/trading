package com.tradingsim.app.data.model

/**
 * Représente une bougie OHLCV (Open, High, Low, Close, Volume).
 * timestampMs : instant de clôture/ouverture de la bougie en millisecondes epoch.
 */
data class Candle(
    val timestampMs: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
) {
    val isBullish: Boolean get() = close >= open
    val bodyMax: Double get() = maxOf(open, close)
    val bodyMin: Double get() = minOf(open, close)
}

enum class Timeframe(val label: String, val millis: Long) {
    M1("1m", 60_000L),
    M5("5m", 5 * 60_000L),
    M15("15m", 15 * 60_000L),
    H1("1H", 60 * 60_000L),
    H4("4H", 4 * 60 * 60_000L),
    D1("1D", 24 * 60 * 60_000L);

    /** Format d'intervalle attendu par l'API Binance (toujours en minuscules, ex: "1h", "1d"). */
    val binanceInterval: String get() = label.lowercase()
}

data class Asset(
    val symbol: String,      // ex: BTCUSDT
    val displayName: String  // ex: BTC / USDT
)
