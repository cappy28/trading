package com.tradingsim.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradingsim.app.ui.theme.LossRed
import com.tradingsim.app.ui.theme.ProfitGreen
import com.tradingsim.app.ui.theme.TextWhite

@Composable
fun TradeActionBar(
    hasOpenPosition: Boolean,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    onClosePosition: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onBuy,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen, contentColor = TextWhite)
        ) {
            Text("Acheter", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onSell,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LossRed, contentColor = TextWhite)
        ) {
            Text("Vendre", fontWeight = FontWeight.Bold)
        }
        if (hasOpenPosition) {
            OutlinedButton(
                onClick = onClosePosition,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Fermer")
            }
        }
    }
}
