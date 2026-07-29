package com.tradingsim.app

import android.app.Application
import androidx.room.Room
import com.tradingsim.app.data.local.AppDatabase
import com.tradingsim.app.data.local.SettingsDataStore
import com.tradingsim.app.data.remote.BinanceApi
import com.tradingsim.app.data.remote.BinanceWebSocketClient
import com.tradingsim.app.data.remote.YahooFinanceApi
import com.tradingsim.app.data.remote.YahooFinanceClient
import com.tradingsim.app.data.repository.MarketRepository
import com.tradingsim.app.data.repository.PortfolioRepository
import com.tradingsim.app.util.NetworkConnectivityObserver
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Conteneur de dépendances simplifié (pas d'injection de dépendances lourde type Hilt,
 * pour garantir une compilation immédiate et sans configuration additionnelle).
 */
class TradingSimApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var marketRepository: MarketRepository
        private set
    lateinit var portfolioRepository: PortfolioRepository
        private set
    lateinit var settingsDataStore: SettingsDataStore
        private set
    lateinit var connectivityObserver: NetworkConnectivityObserver
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(this, AppDatabase::class.java, "trading_sim.db")
            .fallbackToDestructiveMigration()
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BinanceApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val binanceApi = retrofit.create(BinanceApi::class.java)
        val wsClient = BinanceWebSocketClient(okHttpClient)
        connectivityObserver = NetworkConnectivityObserver(this)

        val yahooOkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
                    )
                    .build()
                chain.proceed(request)
            }
            .build()

        val yahooRetrofit = Retrofit.Builder()
            .baseUrl(YahooFinanceApi.BASE_URL)
            .client(yahooOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val yahooApi = yahooRetrofit.create(YahooFinanceApi::class.java)
        val yahooClient = YahooFinanceClient(yahooApi)

        marketRepository = MarketRepository(binanceApi, wsClient, yahooClient, connectivityObserver)
        portfolioRepository = PortfolioRepository(database.tradingDao())
        settingsDataStore = SettingsDataStore(this)
    }
}
