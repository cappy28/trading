package com.tradingsim.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.tradingsim.app.ui.ViewModelFactory
import com.tradingsim.app.ui.navigation.AppNavGraph
import com.tradingsim.app.ui.theme.TradingSimulatorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TradingSimApplication
        val viewModelFactory = ViewModelFactory(app)

        setContent {
            TradingApp(viewModelFactory)
        }
    }
}

@Composable
private fun TradingApp(viewModelFactory: ViewModelFactory) {
    TradingSimulatorTheme {
        AppNavGraph(viewModelFactory)
    }
}
