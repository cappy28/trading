package com.tradingsim.app.data.remote

import com.google.gson.JsonParser
import com.tradingsim.app.data.model.Candle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response as OkResponse

/**
 * Se connecte au flux Binance kline (ex: btcusdt@kline_1m) et émet les bougies
 * en temps réel, y compris la bougie en cours de formation ("live candle").
 */
class BinanceWebSocketClient(private val client: OkHttpClient) {

    fun streamKlines(symbol: String, interval: String): Flow<Candle> = callbackFlow {
        val streamName = "${symbol.lowercase()}@kline_$interval"
        val request = Request.Builder()
            .url("${BinanceApi.WS_BASE_URL}/$streamName")
            .build()

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JsonParser.parseString(text).asJsonObject
                    val k = json.getAsJsonObject("k")
                    val candle = Candle(
                        timestampMs = k.get("t").asLong,
                        open = k.get("o").asString.toDouble(),
                        high = k.get("h").asString.toDouble(),
                        low = k.get("l").asString.toDouble(),
                        close = k.get("c").asString.toDouble(),
                        volume = k.get("v").asString.toDouble()
                    )
                    trySend(candle)
                } catch (_: Exception) {
                    // message inattendu ignoré
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: OkResponse?) {
                close(t)
            }
        }

        val ws = client.newWebSocket(request, listener)

        awaitClose { ws.close(1000, "closed") }
    }
}
