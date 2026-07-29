package com.tradingsim.app.ui.screens.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradingsim.app.data.model.PortfolioStats
import com.tradingsim.app.ui.theme.BackgroundDark
import com.tradingsim.app.ui.theme.CardDark
import com.tradingsim.app.ui.theme.LossRed
import com.tradingsim.app.ui.theme.ProfitGreen
import com.tradingsim.app.ui.theme.TextMuted
import java.util.Locale

@Composable
fun PortfolioScreen(viewModel: PortfolioViewModel) {
    val stats by viewModel.stats.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StatRow("Capital actuel", money(stats.currentCapital)) }
        item { StatRow("Capital de départ", money(stats.startingCapital)) }
        item {
            StatRow(
                "PNL total",
                money(stats.totalPnl),
                valueColor = if (stats.totalPnl >= 0) ProfitGreen else LossRed
            )
        }
        item { StatRow("Nombre de trades", stats.tradeCount.toString()) }
        item { StatRow("Winrate", String.format(Locale.US, "%.1f%%", stats.winRate)) }
        item { StatRow("Profit total", money(stats.totalProfit), valueColor = ProfitGreen) }
        item { StatRow("Perte totale", money(stats.totalLoss), valueColor = LossRed) }
        item {
            StatRow(
                "Meilleur trade",
                stats.bestTrade?.let { money(it.pnl) } ?: "—",
                valueColor = ProfitGreen
            )
        }
        item {
            StatRow(
                "Pire trade",
                stats.worstTrade?.let { money(it.pnl) } ?: "—",
                valueColor = LossRed
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun money(value: Double): String = "$" + String.format(Locale.US, "%,.2f", value)
