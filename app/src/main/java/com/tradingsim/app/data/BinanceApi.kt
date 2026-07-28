package com.tradingsim.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApi {

    /** Bougies historiques : https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=1m&limit=200 */
    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 200
    ): List<RawKline>

    @GET("api/v3/ticker/24hr")
    suspend fun get24hTicker(@Query("symbol") symbol: String): Ticker24h

    companion object {
        const val BASE_URL = "https://api.binance.com/"
        const val WS_BASE_URL = "wss://stream.binance.com:9443/ws"
    }
}
