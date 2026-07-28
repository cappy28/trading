package com.tradingsim.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "trading_sim_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val STARTING_CAPITAL_KEY = doublePreferencesKey("starting_capital")
        const val DEFAULT_STARTING_CAPITAL = 1000.0
    }

    val startingCapitalFlow: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[STARTING_CAPITAL_KEY] ?: DEFAULT_STARTING_CAPITAL
    }

    suspend fun setStartingCapital(value: Double) {
        context.dataStore.edit { it[STARTING_CAPITAL_KEY] = value }
    }
}
