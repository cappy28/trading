package com.tradingsim.app.data.simulation

/**
 * Les différents "régimes" de comportement du marché fictif.
 * Le moteur bascule d'un régime à l'autre de façon semi-aléatoire mais pondérée,
 * pour éviter un marché totalement imprévisible ou totalement mécanique.
 */
enum class MarketRegime {
    BULL,           // marché haussier
    BEAR,           // marché baissier
    HIGH_VOLATILITY,
    LOW_VOLATILITY,
    CONSOLIDATION,  // range serré
    CRASH,          // krach brutal
    PUMP,           // pump violent
    CORRECTION,     // repli après pump/bull
    REBOUND,        // rebond après crash/bear
    ACCUMULATION,   // range serré avant breakout haussier
    DISTRIBUTION;   // range serré avant breakout baissier

    /** Durée moyenne du régime en nombre de bougies avant ré-évaluation. */
    val averageDurationCandles: Int
        get() = when (this) {
            BULL, BEAR -> 120
            HIGH_VOLATILITY -> 40
            LOW_VOLATILITY, CONSOLIDATION -> 80
            CRASH, PUMP -> 8
            CORRECTION, REBOUND -> 25
            ACCUMULATION, DISTRIBUTION -> 60
        }

    /** Dérive (drift) moyenne par bougie, en fraction du prix. */
    val baseDrift: Double
        get() = when (this) {
            BULL -> 0.0009
            BEAR -> -0.0009
            HIGH_VOLATILITY -> 0.0002
            LOW_VOLATILITY -> 0.0001
            CONSOLIDATION -> 0.0
            CRASH -> -0.02
            PUMP -> 0.02
            CORRECTION -> -0.004
            REBOUND -> 0.004
            ACCUMULATION -> 0.0003
            DISTRIBUTION -> -0.0003
        }

    /** Volatilité relative (écart-type des rendements par bougie). */
    val baseVolatility: Double
        get() = when (this) {
            BULL -> 0.006
            BEAR -> 0.007
            HIGH_VOLATILITY -> 0.018
            LOW_VOLATILITY -> 0.0025
            CONSOLIDATION -> 0.004
            CRASH -> 0.03
            PUMP -> 0.028
            CORRECTION -> 0.012
            REBOUND -> 0.012
            ACCUMULATION -> 0.0045
            DISTRIBUTION -> 0.0045
        }

    /** Régimes vers lesquels ce régime peut naturellement transiter, avec poids. */
    fun nextRegimeWeights(): Map<MarketRegime, Double> = when (this) {
        BULL -> mapOf(BULL to 0.45, HIGH_VOLATILITY to 0.1, CONSOLIDATION to 0.15, CORRECTION to 0.15, DISTRIBUTION to 0.1, PUMP to 0.05)
        BEAR -> mapOf(BEAR to 0.45, HIGH_VOLATILITY to 0.1, CONSOLIDATION to 0.15, REBOUND to 0.15, ACCUMULATION to 0.1, CRASH to 0.05)
        HIGH_VOLATILITY -> mapOf(HIGH_VOLATILITY to 0.25, BULL to 0.15, BEAR to 0.15, CRASH to 0.1, PUMP to 0.1, CONSOLIDATION to 0.25)
        LOW_VOLATILITY -> mapOf(LOW_VOLATILITY to 0.4, CONSOLIDATION to 0.3, ACCUMULATION to 0.15, DISTRIBUTION to 0.15)
        CONSOLIDATION -> mapOf(CONSOLIDATION to 0.3, ACCUMULATION to 0.2, DISTRIBUTION to 0.2, BULL to 0.15, BEAR to 0.15)
        CRASH -> mapOf(REBOUND to 0.55, HIGH_VOLATILITY to 0.25, BEAR to 0.2)
        PUMP -> mapOf(CORRECTION to 0.55, HIGH_VOLATILITY to 0.25, BULL to 0.2)
        CORRECTION -> mapOf(CONSOLIDATION to 0.35, BEAR to 0.25, LOW_VOLATILITY to 0.2, REBOUND to 0.2)
        REBOUND -> mapOf(CONSOLIDATION to 0.35, BULL to 0.25, LOW_VOLATILITY to 0.2, CORRECTION to 0.2)
        ACCUMULATION -> mapOf(BULL to 0.4, PUMP to 0.15, CONSOLIDATION to 0.25, HIGH_VOLATILITY to 0.2)
        DISTRIBUTION -> mapOf(BEAR to 0.4, CRASH to 0.15, CONSOLIDATION to 0.25, HIGH_VOLATILITY to 0.2)
    }
}
