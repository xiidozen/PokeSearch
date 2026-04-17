package com.pokesearch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary          = PokeRed,
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PokeRedLight,
    secondary        = PokeBlue,
    onSecondary      = androidx.compose.ui.graphics.Color.White,
    tertiary         = PokeYellow,
    background       = PokeLight,
    surface          = androidx.compose.ui.graphics.Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary          = PokeRedLight,
    onPrimary        = PokeDark,
    primaryContainer = PokeRedDark,
    secondary        = PokeBlueLight,
    onSecondary      = PokeDark,
    tertiary         = PokeYellow,
    background       = PokeDark,
    surface          = androidx.compose.ui.graphics.Color(0xFF2C2C2C),
)

@Composable
fun PokeSearchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
