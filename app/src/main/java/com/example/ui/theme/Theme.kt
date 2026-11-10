package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.AccentPalette
import com.example.data.model.ThemeMode

val LifeOsShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun LifeOsTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentPalette: AccentPalette = AccentPalette.EMERALD,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(accentPalette.darkPrimary),
            onPrimary = DarkBackground,
            primaryContainer = Color(accentPalette.darkContainer),
            onPrimaryContainer = Color(accentPalette.lightContainer),
            secondary = IndigoSecondaryDark,
            onSecondary = DarkOnSurface,
            secondaryContainer = IndigoContainerDark,
            onSecondaryContainer = IndigoContainerLight,
            tertiary = AmberTertiaryDark,
            onTertiary = DarkBackground,
            tertiaryContainer = AmberContainerDark,
            onTertiaryContainer = AmberContainerLight,
            error = CoralErrorDark,
            onError = DarkBackground,
            errorContainer = CoralContainerDark,
            onErrorContainer = CoralContainerLight,
            background = DarkBackground,
            onBackground = DarkOnSurface,
            surface = DarkSurface,
            onSurface = DarkOnSurface,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkOnSurfaceVariant,
            outline = DarkOutline
        )
    } else {
        lightColorScheme(
            primary = Color(accentPalette.lightPrimary),
            onPrimary = Color.White,
            primaryContainer = Color(accentPalette.lightContainer),
            onPrimaryContainer = Color(accentPalette.darkContainer),
            secondary = IndigoSecondary,
            onSecondary = Color.White,
            secondaryContainer = IndigoContainerLight,
            onSecondaryContainer = IndigoContainerDark,
            tertiary = AmberTertiary,
            onTertiary = Color.White,
            tertiaryContainer = AmberContainerLight,
            onTertiaryContainer = AmberContainerDark,
            error = CoralError,
            onError = Color.White,
            errorContainer = CoralContainerLight,
            onErrorContainer = CoralContainerDark,
            background = LightBackground,
            onBackground = LightOnSurface,
            surface = LightSurface,
            onSurface = LightOnSurface,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightOnSurfaceVariant,
            outline = LightOutline
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = LifeOsShapes,
        content = content
    )
}
