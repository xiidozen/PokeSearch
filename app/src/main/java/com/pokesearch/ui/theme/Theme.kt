package com.pokesearch.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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
)

@Composable
fun PokeSearchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
