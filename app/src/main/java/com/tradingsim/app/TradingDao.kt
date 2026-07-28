package com.tradingsim.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tradingsim.app.data.local.entity.ClosedTradeEntity
import com.tradingsim.app.data.local.entity.OpenPositionEntity
import com.tradingsim.app.data.local.entity.PortfolioStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TradingDao {

    // --- Positions ouvertes ---
    @Query("SELECT * FROM open_positions ORDER BY openedAtMs DESC")
    fun observeOpenPositions(): Flow<List<OpenPositionEntity>>

    @Insert
    suspend fun insertOpenPosition(position: OpenPositionEntity): Long

    @Delete
    suspend fun deleteOpenPosition(position: OpenPositionEntity)

    // --- Historique des trades clôturés ---
    @Query("SELECT * FROM closed_trades ORDER BY closedAtMs DESC")
    fun observeClosedTrades(): Flow<List<ClosedTradeEntity>>

    @Insert
    suspend fun insertClosedTrade(trade: ClosedTradeEntity)

    @Query("DELETE FROM closed_trades")
    suspend fun clearHistory()

    // --- État du portefeuille (capital) ---
    @Query("SELECT * FROM portfolio_state WHERE id = 0")
    fun observePortfolioState(): Flow<PortfolioStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPortfolioState(state: PortfolioStateEntity)

    @Query("DELETE FROM open_positions")
    suspend fun clearOpenPositions()

    @Query("DELETE FROM portfolio_state")
    suspend fun clearPortfolioState()
}
