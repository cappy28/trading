package com.tradingsim.app.data.remote

import com.tradingsim.app.data.model.Candle
import com.tradingsim.app.data.model.Timeframe

/**
 * Convertit un Timeframe de l'app vers le couple (interval, range) attendu par Yahoo Finance.
 * Yahoo ne propose pas nativement de "4h" : on utilise "60m" en élargissant la plage
 * (les bougies affichées en 4H seront donc en réalité des bougies 1H sur cette source -
 *  limitation connue, propre aux actions, absente en crypto/Binance).
 */
private fun Timeframe.toYahooParams(): Pair<String, String> = when (this) {
    Timeframe.M1 -> "1m" to "1d"
    Timeframe.M5 -> "5m" to "5d"
    Timeframe.M15 -> "15m" to "1mo"
    Timeframe.H1 -> "60m" to "3mo"
    Timeframe.H4 -> "60m" to "6mo"
    Timeframe.D1 -> "1d" to "1y"
}

class YahooFinanceClient(private val api: YahooFinanceApi) {

    suspend fun fetchCandles(symbol: String, timeframe: Timeframe): List<Candle> {
        val (interval, range) = timeframe.toYahooParams()
        val response = api.getChart(symbol, interval, range)
        val result = response.chart.result?.firstOrNull()
            ?: throw IllegalStateException(response.chart.error?.description ?: "Symbole introuvable")

        val timestamps = result.timestamp.orEmpty()
        val quote = result.indicators.quote.firstOrNull()

        val candles = mutableListOf<Candle>()
        for (i in timestamps.indices) {
            val open = quote?.open?.getOrNull(i)
            val high = quote?.high?.getOrNull(i)
            val low = quote?.low?.getOrNull(i)
            val close = quote?.close?.getOrNull(i)
            // Yahoo renvoie des null pour les minutes sans transaction (marché fermé, illiquidité) -> on ignore
            if (open == null || high == null || low == null || close == null) continue
            candles.add(
                Candle(
                    timestampMs = timestamps[i] * 1000L,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = (quote.volume?.getOrNull(i) ?: 0L).toDouble()
                )
            )
        }
        return candles
    }
}
