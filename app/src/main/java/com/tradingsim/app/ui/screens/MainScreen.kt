package com.tradingsim.app.ui.screens.main

import android.app.Activity
import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tradingsim.app.ui.components.AssetSelector
import com.tradingsim.app.ui.components.CandlestickChart
import com.tradingsim.app.ui.components.MarketHeader
import com.tradingsim.app.ui.components.TimeframeSelector
import com.tradingsim.app.ui.components.TradeActionBar
import com.tradingsim.app.ui.theme.BackgroundDark
import com.tradingsim.app.util.ChartSnapshotCapturer
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    var chartBounds by remember { mutableStateOf<Rect?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        MarketHeader(
            assetName = state.asset.displayName,
            price = state.currentPrice,
            changePercent = state.changePercent,
            isOnline = state.isOnline
        )

        AssetSelector(
            selected = state.asset,
            onSelect = viewModel::selectAsset,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        TimeframeSelector(
            selected = state.timeframe,
            onSelect = viewModel::selectTimeframe
        )

        CandlestickChart(
            candles = state.candles,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onGloballyPositioned { coordinates ->
                    val b = coordinates.boundsInWindow()
                    chartBounds = Rect(
                        b.left.roundToInt(), b.top.roundToInt(),
                        b.right.roundToInt(), b.bottom.roundToInt()
                    )
                }
        )

        TradeActionBar(
            hasOpenPosition = state.openPosition != null,
            onBuy = viewModel::buy,
            onSell = viewModel::sell,
            onClosePosition = {
                val act = activity
                val bounds = chartBounds
                if (act != null && bounds != null) {
                    coroutineScope.launch {
                        val bitmap = ChartSnapshotCapturer.captureWindowRegion(act, bounds)
                        val path = bitmap?.let { ChartSnapshotCapturer.saveToFile(act.filesDir, it) }
                        viewModel.closePosition(path)
                    }
                } else {
                    viewModel.closePosition(null)
                }
            }
        )
    }
}
