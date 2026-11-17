package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class SileniExtendedColors(
    val puas: Color,
    val puasContainer: Color,
    val onPuasContainer: Color,
    val cukup: Color,
    val cukupContainer: Color,
    val onCukupContainer: Color,
    val kurang: Color,
    val kurangContainer: Color,
    val onKurangContainer: Color,
    val navyHeroStart: Color,
    val navyHeroEnd: Color,
)

val LocalSileniColors = staticCompositionLocalOf {
    SileniExtendedColors(
        puas = CivicEmerald,
        puasContainer = CivicEmeraldContainer,
        onPuasContainer = CivicEmeraldOnContainer,
        cukup = CivicAmber,
        cukupContainer = CivicAmberContainer,
        onCukupContainer = CivicAmberOnContainer,
        kurang = CivicCrimson,
        kurangContainer = CivicCrimsonContainer,
        onKurangContainer = CivicCrimsonOnContainer,
        navyHeroStart = CivicNavy,
        navyHeroEnd = CivicNavyDark,
    )
}

private val LightColorScheme = lightColorScheme(
    primary = CivicNavy,
    onPrimary = Color.White,
    primaryContainer = CivicNavyContainer,
    onPrimaryContainer = CivicNavyOnContainer,
    secondary = CivicEmerald,
    onSecondary = Color.White,
    secondaryContainer = CivicEmeraldContainer,
    onSecondaryContainer = CivicEmeraldOnContainer,
    tertiary = CivicAmber,
    onTertiary = Color.White,
    tertiaryContainer = CivicAmberContainer,
    onTertiaryContainer = CivicAmberOnContainer,
    error = CivicCrimson,
    onError = Color.White,
    errorContainer = CivicCrimsonContainer,
    onErrorContainer = CivicCrimsonOnContainer,
    background = SurfaceBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = CivicNavy80,
    onPrimary = CivicNavyDark,
    primaryContainer = CivicNavy,
    onPrimaryContainer = CivicNavyContainer,
    secondary = CivicEmerald80,
    onSecondary = Color(0xFF00382B),
    secondaryContainer = Color(0xFF00513F),
    onSecondaryContainer = CivicEmeraldContainer,
    tertiary = CivicAmber80,
    onTertiary = Color(0xFF472A00),
    tertiaryContainer = Color(0xFF663E00),
    onTertiaryContainer = CivicAmberContainer,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = CivicCrimsonContainer,
    background = SurfaceBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceCardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
)

private val SileniShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun SileniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        SileniExtendedColors(
            puas = CivicEmerald80,
            puasContainer = Color(0xFF064E3B),
            onPuasContainer = CivicEmeraldContainer,
            cukup = CivicAmber80,
            cukupContainer = Color(0xFF5C3300),
            onCukupContainer = CivicAmberContainer,
            kurang = Color(0xFFFFB4AB),
            kurangContainer = Color(0xFF7F1D1D),
            onKurangContainer = CivicCrimsonContainer,
            navyHeroStart = Color(0xFF14396F),
            navyHeroEnd = Color(0xFF091D3C),
        )
    } else {
        SileniExtendedColors(
            puas = CivicEmerald,
            puasContainer = CivicEmeraldContainer,
            onPuasContainer = CivicEmeraldOnContainer,
            cukup = CivicAmber,
            cukupContainer = CivicAmberContainer,
            onCukupContainer = CivicAmberOnContainer,
            kurang = CivicCrimson,
            kurangContainer = CivicCrimsonContainer,
            onKurangContainer = CivicCrimsonOnContainer,
            navyHeroStart = CivicNavy,
            navyHeroEnd = CivicNavyDark,
        )
    }

    CompositionLocalProvider(LocalSileniColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SileniTypography,
            shapes = SileniShapes,
            content = content,
        )
    }
}
