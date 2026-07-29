package com.tradingsim.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tradingsim.app.ui.theme.AccentBlue
import com.tradingsim.app.ui.theme.BackgroundDark
import com.tradingsim.app.ui.theme.CardDark
import com.tradingsim.app.ui.theme.LossRed
import com.tradingsim.app.ui.theme.TextMuted
import java.util.Locale

private val capitalOptions = listOf(100.0, 500.0, 1000.0, 5000.0, 10000.0, 100000.0)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val currentCapital by viewModel.startingCapital.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Capital de départ",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(capitalOptions) { option ->
            val selected = option == currentCapital
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardDark)
                    .clickable { viewModel.setStartingCapital(option) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "$" + String.format(Locale.US, "%,.0f", option),
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
                if (selected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = AccentBlue)
                }
            }
        }

        item {
            Text(
                "Général",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardDark)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Thème")
                Text("Sombre (fixe)", color = TextMuted)
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardDark)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Langue")
                Text("Français", color = TextMuted)
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardDark)
                    .clickable { showClearDialog = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Effacer toutes les données", color = LossRed, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = LossRed)
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Effacer toutes les données ?") },
            text = { Text("Cette action supprime toutes les positions, l'historique et réinitialise le capital. Elle est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showClearDialog = false
                }) { Text("Effacer", color = LossRed) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Annuler") }
            }
        )
    }
}
