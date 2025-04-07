package io.cnvs.example.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.navigator.Navigator
import com.lynxal.logging.LogLevel
import com.lynxal.logging.Logger
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {

    LaunchedEffect(Unit) {
        Logger.minLevel = LogLevel.Verbose
    }

    MaterialTheme {
        Navigator(HomeScreen())
    }
}
