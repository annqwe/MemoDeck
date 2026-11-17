package com.example.memodeck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Палитра "Персик и Шалфей" (Светлая)
val PeachPrimary = Color(0xFFFFB5A7)
val PeachSecondary = Color(0xFFA8DADC)
val TextDark = Color(0xFF333333)
val BackgroundCream = Color(0xFFFDFBF7)
val SurfaceWhite = Color(0xFFFFFFFF)

// Дополнительные яркие цвета для профиля и карточек
val SoftBlue = Color(0xFF90CAF9)
val SoftGreen = Color(0xFFA5D6A7)
val SoftYellow = Color(0xFFFFF59D)

// Цвета для темной темы (уютный темно-синий, а не черный)
val DarkBackground = Color(0xFF1E1E24)
val DarkSurface = Color(0xFF2B2B36)
val DarkText = Color(0xFFE5E5E5)

private val LightColorScheme = lightColorScheme(
    primary = PeachPrimary,
    secondary = PeachSecondary,
    background = BackgroundCream,
    surface = SurfaceWhite,
    onPrimary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

private val DarkColorScheme = darkColorScheme(
    primary = PeachPrimary,
    secondary = PeachSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun MemoDeckTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}