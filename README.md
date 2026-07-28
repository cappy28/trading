# TradingSim — Simulateur de trading (Android)

Application Android personnelle de simulation de trading. Kotlin + Jetpack Compose + Material Design 3, architecture MVVM.

## Ouvrir et compiler le projet

1. Ouvrir Android Studio (Koala/2024.x ou plus récent recommandé).
2. **File → Open** puis sélectionner le dossier `TradingSimulator`.
3. Laisser Android Studio synchroniser Gradle (il téléchargera automatiquement Gradle 8.7, AGP 8.5.2 et toutes les dépendances listées dans `app/build.gradle.kts` — connexion Internet nécessaire à la première synchro).
4. Sélectionner un appareil/émulateur avec **Android 8.0 (API 26) ou supérieur**.
5. **Run ▶** pour lancer l'app, ou **Build → Build Bundle(s) / APK(s) → Build APK(s)** pour générer le fichier `.apk` (dans `app/build/outputs/apk/debug/`).

Aucune clé API n'est nécessaire : les endpoints publics de Binance (`api.binance.com`, `stream.binance.com`) ne demandent pas d'authentification pour les données de marché en lecture seule.

## Fonctionnement

- **En ligne** : données réelles Binance (REST pour l'historique + WebSocket pour le temps réel), symbole par défaut `BTCUSDT`.
- **Hors ligne** : bascule automatique et transparente vers `MarketSimulationEngine`, qui génère un marché fictif réaliste (régimes haussier/baissier/krach/pump/consolidation/etc., supports/résistances, cassures, fake breakouts, gaps).
- Toutes les données du portefeuille (capital, positions, historique) sont persistées localement avec **Room**, et le capital de départ est mémorisé avec **DataStore** — tout survit à la fermeture de l'app.

## Structure du projet

```
app/src/main/java/com/tradingsim/app/
├── data/
│   ├── model/         # Candle, Timeframe, Position, Trade, Asset
│   ├── remote/        # API REST + WebSocket Binance
│   ├── local/          # Room (entités, DAO, base) + DataStore
│   ├── simulation/     # Moteur de marché fictif hors-ligne
│   └── repository/     # MarketRepository, PortfolioRepository
├── ui/
│   ├── theme/          # Couleurs, typographie, thème sombre
│   ├── components/     # Graphique en chandeliers, header, boutons, sélecteurs
│   ├── navigation/      # Navigation Compose (4 onglets)
│   └── screens/        # main, portfolio, history, settings (+ ViewModels)
└── util/               # Détection de connectivité réseau
```

## Pistes d'amélioration futures

- Capture d'écran réelle du graphique au moment du trade (actuellement le champ `chartSnapshot` existe en base mais n'est pas encore rempli — nécessite `graphicsLayer.toImageBitmap()` + sauvegarde PNG locale).
- Sélection de plusieurs actifs (actuellement BTC/USDT fixe).
- Effet de levier configurable depuis l'UI (le modèle le supporte déjà).
