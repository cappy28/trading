package com.tradingsim.app.data.repository

import com.tradingsim.app.data.model.Asset
import com.tradingsim.app.data.model.Candle
import com.tradingsim.app.data.model.MarketType
import com.tradingsim.app.data.model.Timeframe
import com.tradingsim.app.data.remote.BinanceApi
import com.tradingsim.app.data.remote.BinanceWebSocketClient
import com.tradingsim.app.data.remote.YahooFinanceClient
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
 * Point d'entrée unique pour les données de marché, quel que soit le type d'actif.
 * - CRYPTO (Binance)      : historique REST + flux temps réel WebSocket.
 * - STOCK (Yahoo Finance) : historique + rafraîchissement périodique (pas de WebSocket
 *   public gratuit pour les actions, donc "temps réel" ≈ polling toutes les ~5 secondes).
 * - FICTIONAL             : aucune donnée réelle n'existe (société non cotée) -> toujours
 *   simulé, même quand l'appareil est en ligne.
 * Hors ligne, ou en cas d'échec réseau, bascule automatique vers le moteur de simulation.
 */
class MarketRepository(
    private val binanceApi: BinanceApi,
    private val binanceWsClient: BinanceWebSocketClient,
    private val yahooClient: YahooFinanceClient,
    private val connectivityObserver: NetworkConnectivityObserver
) {
    private val maxCandlesKept = 300

    fun observeMarket(asset: Asset, timeframe: Timeframe): Flow<MarketState> = channelFlow {
        if (asset.market == MarketType.FICTIONAL) {
            streamSimulatedMarket(timeframe)
            return@channelFlow
        }

        val isOnline = try {
            connectivityObserver.observe().first()
        } catch (_: Exception) {
            false
        }

        if (isOnline) {
            try {
                when (asset.market) {
                    MarketType.CRYPTO -> streamRealCryptoMarket(asset.symbol, timeframe)
                    MarketType.STOCK -> streamRealStockMarket(asset.symbol, timeframe)
                    MarketType.FICTIONAL -> Unit // déjà géré plus haut
                }
            } catch (_: Exception) {
                // Échec réseau en cours de route -> on bascule sur la simulation
                streamSimulatedMarket(timeframe)
            }
        } else {
            streamSimulatedMarket(timeframe)
        }
    }

    private suspend fun ProducerScope<MarketState>.streamRealCryptoMarket(symbol: String, timeframe: Timeframe) {
        val history = fetchBinanceHistory(symbol, timeframe).toMutableList()
        val referencePrice = history.firstOrNull()?.close
        emitState(history, true, referencePrice)

        binanceWsClient.streamKlines(symbol, timeframe.binanceInterval).collect { liveCandle ->
            upsertCandle(history, liveCandle)
            emitState(history, true, referencePrice)
        }
    }

    /** Pas de WebSocket public gratuit pour les actions : on interroge Yahoo Finance en boucle. */
    private suspend fun ProducerScope<MarketState>.streamRealStockMarket(symbol: String, timeframe: Timeframe) {
        var history = yahooClient.fetchCandles(symbol, timeframe).toMutableList()
        val referencePrice = history.firstOrNull()?.close
        emitState(history, true, referencePrice)

        while (true) {
            delay(5000L)
            val fresh = yahooClient.fetchCandles(symbol, timeframe)
            if (fresh.isNotEmpty()) {
                history = fresh.toMutableList()
                if (history.size > maxCandlesKept) {
                    history = history.takeLast(maxCandlesKept).toMutableList()
                }
                emitState(history, true, referencePrice)
            }
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

    private suspend fun fetchBinanceHistory(symbol: String, timeframe: Timeframe): List<Candle> {
        val raw = binanceApi.getKlines(symbol, timeframe.binanceInterval, 200)
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
