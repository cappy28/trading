package com.tradingsim.app.data.remote

data class YahooChartResponse(val chart: YahooChart)
data class YahooChart(val result: List<YahooChartResult>?, val error: YahooChartError?)
data class YahooChartError(val code: String?, val description: String?)

data class YahooChartResult(
    val meta: YahooMeta,
    val timestamp: List<Long>?,
    val indicators: YahooIndicators
)

data class YahooMeta(
    val regularMarketPrice: Double?,
    val previousClose: Double?,
    val currency: String?,
    val symbol: String?
)

data class YahooIndicators(val quote: List<YahooQuote>)

data class YahooQuote(
    val open: List<Double?>?,
    val high: List<Double?>?,
    val low: List<Double?>?,
    val close: List<Double?>?,
    val volume: List<Long?>?
)
