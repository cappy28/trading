package com.tradingsim.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradingsim.app.data.model.Asset
import com.tradingsim.app.data.model.AssetCatalog
import com.tradingsim.app.ui.theme.AccentBlue
import com.tradingsim.app.ui.theme.CardDark
import com.tradingsim.app.ui.theme.TextMuted
import com.tradingsim.app.ui.theme.TextWhite

@Composable
fun AssetSelector(
    selected: Asset,
    onSelect: (Asset) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AssetCatalog.all, key = { it.symbol }) { asset ->
            val isSelected = asset.symbol == selected.symbol
            Text(
                text = asset.displayName,
                color = if (isSelected) TextWhite else TextMuted,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AccentBlue.copy(alpha = 0.18f) else CardDark)
                    .clickable { onSelect(asset) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}
