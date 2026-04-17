package com.pokesearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokesearch.ui.MainScreen
import com.pokesearch.ui.theme.PokeSearchTheme
import com.pokesearch.viewmodel.MainViewModel
import com.pokesearch.viewmodel.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val uiState by vm.uiState.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.DARK   -> true
                ThemeMode.LIGHT  -> false
            }
            PokeSearchTheme(darkTheme = darkTheme) {
                MainScreen(vm = vm)
            }
        }
    }
}
