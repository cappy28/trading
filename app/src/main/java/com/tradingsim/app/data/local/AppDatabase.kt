package com.tradingsim.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tradingsim.app.data.local.dao.TradingDao
import com.tradingsim.app.data.local.entity.ClosedTradeEntity
import com.tradingsim.app.data.local.entity.OpenPositionEntity
import com.tradingsim.app.data.local.entity.PortfolioStateEntity

@Database(
    entities = [OpenPositionEntity::class, ClosedTradeEntity::class, PortfolioStateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradingDao(): TradingDao
}
