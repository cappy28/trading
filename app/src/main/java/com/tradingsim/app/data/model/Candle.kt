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

enum class MarketType { CRYPTO, STOCK, FICTIONAL }

data class Asset(
    val symbol: String,      // ex: BTCUSDT (crypto) ou AMZN (action)
    val displayName: String, // ex: BTC / USDT
    val market: MarketType
)

/**
 * Catalogue des actifs proposés dans l'app.
 * - CRYPTO  : données réelles Binance.
 * - STOCK   : données réelles Yahoo Finance (actions cotées en bourse).
 * - FICTIONAL : aucune donnée réelle n'existe (société non cotée) -> toujours simulé,
 *   jamais présenté comme une vraie cotation.
 */
object AssetCatalog {
    val all = listOf(
        Asset("BTCUSDT", "Bitcoin (BTC/USDT)", MarketType.CRYPTO),
        Asset("ETHUSDT", "Ethereum (ETH/USDT)", MarketType.CRYPTO),
        Asset("SOLUSDT", "Solana (SOL/USDT)", MarketType.CRYPTO),
        Asset("AMZN", "Amazon", MarketType.STOCK),
        Asset("GOOGL", "Alphabet (Google)", MarketType.STOCK),
        Asset("AAPL", "Apple", MarketType.STOCK),
        Asset("MSFT", "Microsoft", MarketType.STOCK),
        Asset("TSLA", "Tesla", MarketType.STOCK),
        Asset("NVDA", "Nvidia", MarketType.STOCK),
        // Anthropic n'est pas cotée en bourse : aucune donnée réelle n'existe pour cette
        // entrée, elle est simulée en permanence et n'est jamais présentée comme réelle.
        Asset("ANTHROPIC", "Anthropic (fictif — non cotée)", MarketType.FICTIONAL)
    )

    val default = all.first()
}
