package com.tradingsim.app.data.remote

/**
 * Une ligne de la réponse Binance /api/v3/klines est un tableau JSON hétérogène :
 * [ openTime, open, high, low, close, volume, closeTime, ... ]
 * On la parse manuellement en List<Any> via Gson (voir BinanceApi).
 */
typealias RawKline = List<Any>

data class TickerPrice(
    val symbol: String,
    val price: String
)

data class Ticker24h(
    val symbol: String,
    val lastPrice: String,
    val priceChangePercent: String
)
