package com.tradingsim.app.ui.screens.history

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradingsim.app.data.model.ClosedTrade
import com.tradingsim.app.data.model.PositionSide
import com.tradingsim.app.ui.theme.BackgroundDark
import com.tradingsim.app.ui.theme.CardDark
import com.tradingsim.app.ui.theme.LossRed
import com.tradingsim.app.ui.theme.ProfitGreen
import com.tradingsim.app.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val trades by viewModel.trades.collectAsState()

    if (trades.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Aucun trade pour l'instant", color = TextMuted)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(trades, key = { it.id }) { trade -> TradeRow(trade) }
    }
}

@Composable
private fun TradeRow(trade: ClosedTrade) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }
    val pnlColor = if (trade.pnl >= 0) ProfitGreen else LossRed
    val sideLabel = if (trade.side == PositionSide.LONG) "Achat" else "Vente"

    val thumbnail = trade.chartSnapshot?.let { path ->
        remember(path) {
            runCatching {
                val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                BitmapFactory.decodeFile(path, options)?.asImageBitmap()
            }.getOrNull()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = "${trade.symbol} · $sideLabel", fontWeight = FontWeight.Bold)
            Text(
                text = dateFormat.format(Date(trade.closedAtMs)),
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Entrée: ${"%.2f".format(trade.entryPrice)}  →  Sortie: ${"%.2f".format(trade.exitPrice)}",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = (if (trade.pnl >= 0) "+" else "") + "$" + "%.2f".format(trade.pnl),
            color = pnlColor,
            fontWeight = FontWeight.Bold
        )
    }
}
