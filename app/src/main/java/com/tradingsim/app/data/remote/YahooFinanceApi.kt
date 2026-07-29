package com.tradingsim.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API publique non-officielle de Yahoo Finance (aucune clé requise).
 * Utilisée uniquement pour les actions (Amazon, Google, Apple...), Binance ne
 * fournissant que des données crypto.
 */
interface YahooFinanceApi {

    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("range") range: String
    ): YahooChartResponse

    companion object {
        const val BASE_URL = "https://query1.finance.yahoo.com/"
    }
}
