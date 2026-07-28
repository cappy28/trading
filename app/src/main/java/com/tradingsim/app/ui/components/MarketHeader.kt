package com.tradingsim.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradingsim.app.ui.theme.LossRed
import com.tradingsim.app.ui.theme.ProfitGreen
import com.tradingsim.app.ui.theme.TextMuted
import java.util.Locale

@Composable
fun MarketHeader(
    assetName: String,
    price: Double,
    changePercent: Double,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = assetName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "$" + String.format(Locale.US, "%,.2f", price),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            val changeColor = if (changePercent >= 0) ProfitGreen else LossRed
            val sign = if (changePercent >= 0) "+" else ""
            Text(
                text = "$sign${String.format(Locale.US, "%.2f", changePercent)}%",
                color = changeColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        ConnectionIndicator(isOnline = isOnline)
    }
}

@Composable
fun ConnectionIndicator(isOnline: Boolean, modifier: Modifier = Modifier) {
    val color = if (isOnline) ProfitGreen else LossRed
    val label = if (isOnline) "En ligne" else "Hors ligne"
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .padding(end = 6.dp)
                .size(8.dp)
                .background(color = color, shape = CircleShape)
        )
        Text(text = label, color = TextMuted, fontSize = 12.sp)
    }
}
