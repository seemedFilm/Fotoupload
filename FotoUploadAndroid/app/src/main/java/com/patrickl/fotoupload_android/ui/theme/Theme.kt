package com.patrickl.fotoupload_android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 1. Data class for your extra colors
@Immutable
data class ExtraColors(
    val activeStatus: Color = Green40
)

// 2. Local provider
val LocalExtraColors = staticCompositionLocalOf { ExtraColors() }

// 3. THE TRICK: Extension property on the official ColorScheme
val ColorScheme.activeStatus: Color
    @Composable
    get() = LocalExtraColors.current.activeStatus


private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun FotoUploadAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // 4. Decide which color to use
    val extraColors = ExtraColors(
        activeStatus = if (darkTheme) Green80 else Green40
    )

    val colorScheme = when {
        // ... your existing logic for dynamic color and theme ...
        else -> LightColorScheme
    }

    // 5. Provide both standard and extra colors
    CompositionLocalProvider(LocalExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }

    MaterialTheme(
        typography = AppTypography
    ) {
        content()
    }
}
