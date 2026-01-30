package com.markduenas.homesteader.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary colors - Earthy greens for agriculture theme
private val PrimaryLight = Color(0xFF2E7D32)
private val OnPrimaryLight = Color(0xFFFFFFFF)
private val PrimaryContainerLight = Color(0xFFB8F5B8)
private val OnPrimaryContainerLight = Color(0xFF002204)

private val SecondaryLight = Color(0xFF52634F)
private val OnSecondaryLight = Color(0xFFFFFFFF)
private val SecondaryContainerLight = Color(0xFFD5E8CF)
private val OnSecondaryContainerLight = Color(0xFF101F0F)

private val TertiaryLight = Color(0xFF39656B)
private val OnTertiaryLight = Color(0xFFFFFFFF)
private val TertiaryContainerLight = Color(0xFFBCEBF2)
private val OnTertiaryContainerLight = Color(0xFF001F23)

private val ErrorLight = Color(0xFFBA1A1A)
private val OnErrorLight = Color(0xFFFFFFFF)
private val ErrorContainerLight = Color(0xFFFFDAD6)
private val OnErrorContainerLight = Color(0xFF410002)

private val BackgroundLight = Color(0xFFF8FAF5)
private val OnBackgroundLight = Color(0xFF191D17)
private val SurfaceLight = Color(0xFFF8FAF5)
private val OnSurfaceLight = Color(0xFF191D17)
private val SurfaceVariantLight = Color(0xFFDEE5D8)
private val OnSurfaceVariantLight = Color(0xFF424940)

private val OutlineLight = Color(0xFF72796F)
private val OutlineVariantLight = Color(0xFFC2C9BD)

// Dark theme colors
private val PrimaryDark = Color(0xFF9DD89D)
private val OnPrimaryDark = Color(0xFF00390B)
private val PrimaryContainerDark = Color(0xFF0F5318)
private val OnPrimaryContainerDark = Color(0xFFB8F5B8)

private val SecondaryDark = Color(0xFFB9CCB4)
private val OnSecondaryDark = Color(0xFF253423)
private val SecondaryContainerDark = Color(0xFF3B4B38)
private val OnSecondaryContainerDark = Color(0xFFD5E8CF)

private val TertiaryDark = Color(0xFFA1CED5)
private val OnTertiaryDark = Color(0xFF00363C)
private val TertiaryContainerDark = Color(0xFF1F4D53)
private val OnTertiaryContainerDark = Color(0xFFBCEBF2)

private val ErrorDark = Color(0xFFFFB4AB)
private val OnErrorDark = Color(0xFF690005)
private val ErrorContainerDark = Color(0xFF93000A)
private val OnErrorContainerDark = Color(0xFFFFDAD6)

private val BackgroundDark = Color(0xFF11140F)
private val OnBackgroundDark = Color(0xFFE1E4DC)
private val SurfaceDark = Color(0xFF11140F)
private val OnSurfaceDark = Color(0xFFE1E4DC)
private val SurfaceVariantDark = Color(0xFF424940)
private val OnSurfaceVariantDark = Color(0xFFC2C9BD)

private val OutlineDark = Color(0xFF8C9388)
private val OutlineVariantDark = Color(0xFF424940)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
)

@Composable
fun HomesteaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HomesteaderTypography,
        content = content
    )
}
