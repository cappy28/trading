package com.tradingsim.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tradingsim.app.data.model.Candle
import com.tradingsim.app.ui.theme.BorderSubtle
import com.tradingsim.app.ui.theme.LossRed
import com.tradingsim.app.ui.theme.ProfitGreen
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Graphique en chandeliers japonais, dessiné manuellement sur un Canvas.
 * Supporte le zoom (pincement) et le déplacement horizontal (glissement).
 */
@Composable
fun CandlestickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    bullColor: Color = ProfitGreen,
    bearColor: Color = LossRed
) {
    // visibleCount = combien de bougies sont affichées à l'écran (contrôlé par le zoom)
    var visibleCount by remember { mutableFloatStateOf(60f) }
    // offsetCandles = décalage horizontal, en nombre de bougies, depuis la fin de la liste
    var offsetCandles by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newVisible = (visibleCount / zoom).coerceIn(15f, min(candles.size.toFloat(), 400f).coerceAtLeast(15f))
                    visibleCount = newVisible

                    val candleWidthPx = size.width / visibleCount
                    val panCandles = -pan.x / candleWidthPx
                    val maxOffset = max(0f, candles.size - visibleCount)
                    offsetCandles = (offsetCandles + panCandles).coerceIn(0f, maxOffset)
                }
            }
    ) {
        if (candles.isEmpty()) return@Canvas

        val count = min(visibleCount.roundToInt(), candles.size).coerceAtLeast(1)
        val startIndex = (candles.size - count - offsetCandles.roundToInt()).coerceIn(0, max(0, candles.size - count))
        val endIndex = (startIndex + count).coerceAtMost(candles.size)
        val visible = candles.subList(startIndex, endIndex)
        if (visible.isEmpty()) return@Canvas

        val maxPrice = visible.maxOf { it.high }
        val minPrice = visible.minOf { it.low }
        val priceRange = (maxPrice - minPrice).let { if (it <= 0.0) 1.0 else it }

        val chartHeight = size.height
        val chartWidth = size.width
        val candleSlotWidth = chartWidth / visible.size
        val candleBodyWidth = candleSlotWidth * 0.6f

        fun priceToY(price: Double): Float {
            val ratio = (price - minPrice) / priceRange
            return (chartHeight - (ratio * chartHeight).toFloat())
        }

        // Grille horizontale légère
        val gridLines = 4
        repeat(gridLines + 1) { i ->
            val y = chartHeight * i / gridLines
            drawLine(
                color = BorderSubtle,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 1f
            )
        }

        visible.forEachIndexed { index, candle ->
            val centerX = candleSlotWidth * index + candleSlotWidth / 2f
            val color = if (candle.isBullish) bullColor else bearColor

            // Mèche (high-low)
            drawLine(
                color = color,
                start = Offset(centerX, priceToY(candle.high)),
                end = Offset(centerX, priceToY(candle.low)),
                strokeWidth = 2f
            )

            // Corps (open-close)
            val bodyTop = priceToY(candle.bodyMax)
            val bodyBottom = priceToY(candle.bodyMin)
            val bodyHeight = max(2f, bodyBottom - bodyTop)

            drawRect(
                color = color,
                topLeft = Offset(centerX - candleBodyWidth / 2f, bodyTop),
                size = androidx.compose.ui.geometry.Size(candleBodyWidth, bodyHeight)
            )
        }
    }
}
