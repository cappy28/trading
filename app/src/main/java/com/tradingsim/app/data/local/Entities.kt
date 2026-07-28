package com.tradingsim.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "open_positions")
data class OpenPositionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val symbol: String,
    val side: String, // "LONG" | "SHORT"
    val entryPrice: Double,
    val quantity: Double,
    val leverage: Int,
    val openedAtMs: Long
)

@Entity(tableName = "closed_trades")
data class ClosedTradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val symbol: String,
    val side: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val quantity: Double,
    val leverage: Int,
    val openedAtMs: Long,
    val closedAtMs: Long,
    val pnl: Double,
    val chartSnapshotPath: String?
)

@Entity(tableName = "portfolio_state")
data class PortfolioStateEntity(
    @PrimaryKey val id: Int = 0, // ligne unique
    val currentCapital: Double,
    val startingCapital: Double
)
