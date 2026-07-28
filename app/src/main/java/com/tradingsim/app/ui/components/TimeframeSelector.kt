package com.tradingsim.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradingsim.app.data.model.Timeframe
import com.tradingsim.app.ui.theme.AccentBlue
import com.tradingsim.app.ui.theme.CardDark
import com.tradingsim.app.ui.theme.TextMuted
import com.tradingsim.app.ui.theme.TextWhite

@Composable
fun TimeframeSelector(
    selected: Timeframe,
    onSelect: (Timeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Timeframe.entries.forEach { tf ->
            val isSelected = tf == selected
            Text(
                text = tf.label,
                color = if (isSelected) TextWhite else TextMuted,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) AccentBlue.copy(alpha = 0.18f) else CardDark)
                    .clickable { onSelect(tf) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
