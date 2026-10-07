package io.github.vinaooo.vinkit.designsystem

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.vinaooo.vinkit.core.ThemeColor
import io.github.vinaooo.vinkit.core.ThemeMode

fun isDarkTheme(themeMode: ThemeMode, systemInDark: Boolean): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> systemInDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/** Dynamic (wallpaper) colors on Android 12+, the chosen [themeColor]'s otherwise. */
fun colorSchemeFor(context: Context, darkTheme: Boolean, dynamicColor: Boolean, themeColor: ThemeColor): ColorScheme =
    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        paletteScheme(themeColor, darkTheme)
    }

/**
 * Material 3 Expressive with the expressive motion scheme. [themeColor] is the player's choice, or the game's brand
 * color by default. A game with its own colors (a board, cards) provides them inside [content] from
 * `MaterialTheme.colorScheme`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VinkitTheme(
    themeColor: ThemeColor,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    typography: Typography = Typography(),
    content: @Composable () -> Unit,
) {
    val darkTheme = isDarkTheme(themeMode, isSystemInDarkTheme())
    val context = LocalContext.current
    val colorScheme = remember(context, darkTheme, dynamicColor, themeColor) {
        colorSchemeFor(context, darkTheme, dynamicColor, themeColor)
    }
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = typography,
        content = content,
    )
}
