package com.tradingsim.app.data.repository

import com.tradingsim.app.data.model.Candle
import com.tradingsim.app.data.model.Timeframe
import com.tradingsim.app.data.remote.BinanceApi
import com.tradingsim.app.data.remote.BinanceWebSocketClient
import com.tradingsim.app.data.simulation.MarketSimulationEngine
import com.tradingsim.app.util.NetworkConnectivityObserver
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first

data class MarketState(
    val candles: List<Candle>,
    val isOnline: Boolean,
    val currentPrice: Double,
    val changePercent24h: Double
)

/**
 * Point d'entrée unique pour les données de marché.
 * - En ligne  : historique REST Binance + flux temps réel WebSocket.
 * - Hors ligne : bascule automatique vers le moteur de simulation (MarketSimulationEngine),
 *   qui génère un marché fictif réaliste et continu.
 */
class MarketRepository(
    private val api: BinanceApi,
    private val wsClient: BinanceWebSocketClient,
    private val connectivityObserver: NetworkConnectivityObserver
) {
    private val maxCandlesKept = 300

    fun observeMarket(symbol: String, timeframe: Timeframe): Flow<MarketState> = channelFlow {
        val isOnline = try {
            connectivityObserver.observe().first()
        } catch (_: Exception) {
            false
        }

        if (isOnline) {
            try {
                streamRealMarket(symbol, timeframe)
            } catch (_: Exception) {
                // Échec réseau en cours de route -> on bascule sur la simulation
                streamSimulatedMarket(timeframe)
            }
        } else {
            streamSimulatedMarket(timeframe)
        }
    }

    private suspend fun ProducerScope<MarketState>.streamRealMarket(symbol: String, timeframe: Timeframe) {
        val history = fetchHistory(symbol, timeframe).toMutableList()
        val referencePrice = history.firstOrNull()?.close
        emitState(history, true, referencePrice)

        wsClient.streamKlines(symbol, timeframe.binanceInterval).collect { liveCandle ->
            upsertCandle(history, liveCandle)
            emitState(history, true, referencePrice)
        }
    }

    private suspend fun ProducerScope<MarketState>.streamSimulatedMarket(timeframe: Timeframe) {
        val engine = MarketSimulationEngine()
        val history = mutableListOf<Candle>()
        val now = System.currentTimeMillis()

        // Backfill : générer un historique de départ pour ne pas partir d'un graphique vide
        val backfillCount = 150
        var ts = now - backfillCount * timeframe.millis
        repeat(backfillCount) {
            history.add(engine.nextCandle(ts, timeframe.millis))
            ts += timeframe.millis
        }
        val referencePrice = history.firstOrNull()?.close
        emitState(history, false, referencePrice)

        // Génère une nouvelle bougie à un rythme "vivant", en continu
        var lastTs = history.last().timestampMs
        while (true) {
            delay(2000L)
            lastTs += timeframe.millis
            val candle = engine.nextCandle(lastTs, timeframe.millis)
            history.add(candle)
            if (history.size > maxCandlesKept) history.removeAt(0)
            emitState(history, false, referencePrice)
        }
    }

    private fun upsertCandle(history: MutableList<Candle>, incoming: Candle) {
        val lastIndex = history.lastIndex
        if (lastIndex >= 0 && history[lastIndex].timestampMs == incoming.timestampMs) {
            history[lastIndex] = incoming
        } else {
            history.add(incoming)
            if (history.size > maxCandlesKept) history.removeAt(0)
        }
    }

    private suspend fun ProducerScope<MarketState>.emitState(
        history: List<Candle>,
        online: Boolean,
        referencePrice: Double?
    ) {
        val last = history.lastOrNull() ?: return
        val change = if (referencePrice != null && referencePrice != 0.0) {
            (last.close - referencePrice) / referencePrice * 100.0
        } else 0.0
        send(MarketState(history.toList(), online, last.close, change))
    }

    private suspend fun fetchHistory(symbol: String, timeframe: Timeframe): List<Candle> {
        val raw = api.getKlines(symbol, timeframe.binanceInterval, 200)
        return raw.map { row ->
            Candle(
                timestampMs = (row[0] as Double).toLong(),
                open = (row[1] as String).toDouble(),
                high = (row[2] as String).toDouble(),
                low = (row[3] as String).toDouble(),
                close = (row[4] as String).toDouble(),
                volume = (row[5] as String).toDouble()
            )
        }
    }
}
