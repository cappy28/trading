package com.tradingsim.app.data.simulation

import com.tradingsim.app.data.model.Candle
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Générateur de marché fictif "extrêmement réaliste".
 *
 * Principes :
 * - Chaque nouvelle bougie dépend de l'état précédent (prix, momentum, régime courant).
 * - Le marché traverse des régimes (haussier, baissier, krach, pump, etc.) via une
 *   chaîne de Markov pondérée : jamais totalement aléatoire, jamais totalement mécanique.
 * - Des niveaux de support/résistance dynamiques sont maintenus ; le prix réagit dessus
 *   (rebond, cassure nette, ou "fake breakout" qui revient dans le range).
 * - Des gaps occasionnels simulent l'illiquidité / les news soudaines.
 * - Le momentum (moyenne mobile des derniers rendements) influence le rendement suivant,
 *   ce qui crée de l'autocorrélation à court terme (comme un vrai marché).
 */
class MarketSimulationEngine(
    seed: Long = System.currentTimeMillis(),
    startPrice: Double = 42_000.0
) {
    private val random = Random(seed)

    private var currentRegime: MarketRegime = MarketRegime.CONSOLIDATION
    private var candlesLeftInRegime: Int = currentRegime.averageDurationCandles
    private var momentum: Double = 0.0
    private var lastClose: Double = startPrice

    private var support: Double = startPrice * 0.985
    private var resistance: Double = startPrice * 1.015
    private var candlesSinceLevelUpdate = 0

    /** Génère la prochaine bougie à partir de la clôture précédente. */
    fun nextCandle(timestampMs: Long, timeframeMillis: Long): Candle {
        maybeTransitionRegime()

        val open = lastClose
        var driftPct = currentRegime.baseDrift
        val volPct = currentRegime.baseVolatility

        // Autocorrélation : le momentum récent influence légèrement le rendement suivant
        driftPct += momentum * 0.35

        // Réaction aux niveaux de support/résistance
        val levelAdjustment = reactToLevels(open, driftPct, volPct)
        driftPct = levelAdjustment.first
        val breakoutBoost = levelAdjustment.second

        // Bruit gaussien approximé (somme de variables uniformes -> approx normale)
        val noise = gaussianNoise() * volPct
        var returnPct = driftPct + noise + breakoutBoost

        // Gap occasionnel (simulateur de faible liquidité / news)
        val gapPct = maybeGap()
        returnPct += gapPct

        var close = open * (1.0 + returnPct)
        close = max(close, 0.0001)

        // Mèches réalistes : la bougie explore au-delà de open/close avant de s'y stabiliser
        val intrabarRange = abs(returnPct) + volPct * (0.6 + random.nextDouble() * 0.9)
        val high = max(open, close) * (1.0 + intrabarRange * random.nextDouble() * 0.8)
        val low = min(open, close) * (1.0 - intrabarRange * random.nextDouble() * 0.8)

        val volume = simulateVolume(volPct, returnPct)

        // Mise à jour de l'état
        momentum = momentum * 0.6 + returnPct * 0.4
        lastClose = close
        updateLevels(high, low)
        candlesLeftInRegime--

        return Candle(
            timestampMs = timestampMs,
            open = open,
            high = high,
            low = low,
            close = close,
            volume = volume
        )
    }

    fun currentRegimeName(): MarketRegime = currentRegime

    // --- Transition entre régimes (chaîne de Markov pondérée) ---
    private fun maybeTransitionRegime() {
        if (candlesLeftInRegime > 0) return
        val weights = currentRegime.nextRegimeWeights()
        val total = weights.values.sum()
        var pick = random.nextDouble() * total
        for ((regime, weight) in weights) {
            pick -= weight
            if (pick <= 0.0) {
                currentRegime = regime
                break
            }
        }
        candlesLeftInRegime = (currentRegime.averageDurationCandles * (0.6 + random.nextDouble() * 0.8)).toInt()
            .coerceAtLeast(3)
    }

    // --- Support / résistance : rebond, cassure franche, ou fake breakout ---
    private fun reactToLevels(price: Double, drift: Double, vol: Double): Pair<Double, Double> {
        var adjustedDrift = drift
        var breakoutBoost = 0.0

        val distToResistance = (resistance - price) / price
        val distToSupport = (price - support) / price

        if (distToResistance in 0.0..vol * 3) {
            // Prix proche de la résistance : soit rebond, soit cassure, soit fake breakout
            val roll = random.nextDouble()
            when {
                roll < 0.55 -> adjustedDrift -= vol * 1.2 // rebond baissier
                roll < 0.80 -> breakoutBoost += vol * 2.5 // cassure haussière franche
                else -> {
                    // fake breakout : petite poussée puis retour (simulé par un léger plus + momentum négatif futur)
                    breakoutBoost += vol * 0.8
                    momentum -= vol * 0.5
                }
            }
        } else if (distToSupport in 0.0..vol * 3) {
            val roll = random.nextDouble()
            when {
                roll < 0.55 -> adjustedDrift += vol * 1.2 // rebond haussier
                roll < 0.80 -> breakoutBoost -= vol * 2.5 // cassure baissière franche
                else -> {
                    breakoutBoost -= vol * 0.8
                    momentum += vol * 0.5
                }
            }
        }
        return adjustedDrift to breakoutBoost
    }

    private fun updateLevels(high: Double, low: Double) {
        candlesSinceLevelUpdate++
        // Les niveaux "respirent" : ils s'élargissent lentement et se recentrent périodiquement
        if (high > resistance) resistance = high * (1.0 + random.nextDouble() * 0.004)
        if (low < support) support = low * (1.0 - random.nextDouble() * 0.004)

        if (candlesSinceLevelUpdate > 50) {
            // Recentrage périodique pour que support/résistance suivent le prix sur le long terme
            val mid = (support + resistance) / 2.0
            val newMid = mid * 0.5 + lastClose * 0.5
            val halfRange = (resistance - support) / 2.0 * (0.9 + random.nextDouble() * 0.3)
            support = newMid - halfRange
            resistance = newMid + halfRange
            candlesSinceLevelUpdate = 0
        }
    }

    private fun maybeGap(): Double {
        // Gap rare (~1.2% de chance), amplitude modérée
        return if (random.nextDouble() < 0.012) {
            val sign = if (random.nextBoolean()) 1 else -1
            sign * (0.004 + random.nextDouble() * 0.012)
        } else 0.0
    }

    private fun simulateVolume(vol: Double, returnPct: Double): Double {
        val base = 100.0 + random.nextDouble() * 50.0
        val volMultiplier = 1.0 + (vol / 0.006) * 1.5
        val moveMultiplier = 1.0 + abs(returnPct) * 40.0
        return base * volMultiplier * moveMultiplier
    }

    /** Approximation gaussienne via somme de 6 tirages uniformes (théorème central limite). */
    private fun gaussianNoise(): Double {
        var sum = 0.0
        repeat(6) { sum += random.nextDouble() }
        return (sum - 3.0) / 3.0 // moyenne 0, écart-type ~1
    }
}
