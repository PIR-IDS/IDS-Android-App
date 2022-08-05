package fr.pirids.idsapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inversePrimary = md_theme_dark_inversePrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary = md_theme_light_inversePrimary,
)

@Composable
fun IDSAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // We avoid bad contrast
    //TODO: handle this in a better way
    val luminanceTolerance = 0.2
    if(
        (darkTheme && ColorUtils.calculateLuminance(colorScheme.primary.toArgb()) < ColorUtils.calculateLuminance(colorScheme.background.toArgb()) + luminanceTolerance)
        ||
        (!darkTheme && ColorUtils.calculateLuminance(colorScheme.primary.toArgb()) > ColorUtils.calculateLuminance(colorScheme.background.toArgb()) - luminanceTolerance)
    ) {
        colorScheme = colorScheme.copy(primary = colorScheme.onPrimary, onPrimary = colorScheme.primary)
    }
    if(
        (darkTheme && ColorUtils.calculateLuminance(colorScheme.secondary.toArgb()) < ColorUtils.calculateLuminance(colorScheme.background.toArgb()) + luminanceTolerance)
        ||
        (!darkTheme && ColorUtils.calculateLuminance(colorScheme.secondary.toArgb()) > ColorUtils.calculateLuminance(colorScheme.background.toArgb()) - luminanceTolerance)
    ) {
        colorScheme = colorScheme.copy(secondary = colorScheme.onSecondary, onSecondary = colorScheme.secondary)
    }
    if(
        (darkTheme && ColorUtils.calculateLuminance(colorScheme.tertiary.toArgb()) < ColorUtils.calculateLuminance(colorScheme.background.toArgb()) + luminanceTolerance)
        ||
        (!darkTheme && ColorUtils.calculateLuminance(colorScheme.tertiary.toArgb()) > ColorUtils.calculateLuminance(colorScheme.background.toArgb()) - luminanceTolerance)
    ) {
        colorScheme = colorScheme.copy(tertiary = colorScheme.onTertiary, onTertiary = colorScheme.tertiary)
    }

    val view = LocalView.current
    val systemUiController = rememberSystemUiController()
    val useDarkIconsPrimary = (ColorUtils.calculateLuminance(colorScheme.onPrimary.toArgb()) < 0.5)
    val colorPrimary = colorScheme.primary
    val useDarkIconsBackground = (ColorUtils.calculateLuminance(colorScheme.onBackground.toArgb()) < 0.5)
    val colorBackground = colorScheme.background
    if (!view.isInEditMode) {
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = colorPrimary,
                darkIcons = useDarkIconsPrimary
            )
            systemUiController.setStatusBarColor(
                color = colorPrimary,
                darkIcons = useDarkIconsPrimary
            )
            systemUiController.setNavigationBarColor(
                color = colorBackground,
                darkIcons = useDarkIconsBackground
            )
            WindowCompat.getInsetsController((view.context as Activity).window, view).isAppearanceLightStatusBars = useDarkIconsPrimary
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun IDSAlertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = (ColorUtils.calculateLuminance(colorScheme.onError.toArgb()) < 0.5)
    val color = colorScheme.error
    if (!view.isInEditMode) {
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = color,
                darkIcons = useDarkIcons
            )
            systemUiController.setStatusBarColor(
                color = color,
                darkIcons = useDarkIcons
            )
            systemUiController.setNavigationBarColor(
                color = color,
                darkIcons = useDarkIcons
            )
            WindowCompat.getInsetsController((view.context as Activity).window, view).isAppearanceLightStatusBars = useDarkIcons
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}