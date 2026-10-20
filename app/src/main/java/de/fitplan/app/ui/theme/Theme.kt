package de.fitplan.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
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
import androidx.compose.ui.text.font.FontWeight
import de.fitplan.app.core.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF9EF2E4),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A635F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8E2),
    onSecondaryContainer = Color(0xFF05201C),
    tertiary = Color(0xFF456179),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCCE5FF),
    onTertiaryContainer = Color(0xFF001E31),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF4FBF8),
    onBackground = Color(0xFF161D1C),
    surface = Color(0xFFF4FBF8),
    onSurface = Color(0xFF161D1C),
    surfaceVariant = Color(0xFFDAE5E1),
    onSurfaceVariant = Color(0xFF3F4947),
    outline = Color(0xFF6F7977),
    outlineVariant = Color(0xFFBEC9C6),
    inverseSurface = Color(0xFF2B3230),
    inverseOnSurface = Color(0xFFECF2EF),
    inversePrimary = Color(0xFF82D5C8),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEEF5F2),
    surfaceContainer = Color(0xFFE9EFED),
    surfaceContainerHigh = Color(0xFFE3EAE7),
    surfaceContainerHighest = Color(0xFFDDE4E1),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF82D5C8),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF9EF2E4),
    secondary = Color(0xFFB1CCC6),
    onSecondary = Color(0xFF1C3531),
    secondaryContainer = Color(0xFF334B47),
    onSecondaryContainer = Color(0xFFCCE8E2),
    tertiary = Color(0xFFADCAE6),
    onTertiary = Color(0xFF153349),
    tertiaryContainer = Color(0xFF2D4961),
    onTertiaryContainer = Color(0xFFCCE5FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0E1513),
    onBackground = Color(0xFFDDE4E1),
    surface = Color(0xFF0E1513),
    onSurface = Color(0xFFDDE4E1),
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBEC9C6),
    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF3F4947),
    inverseSurface = Color(0xFFDDE4E1),
    inverseOnSurface = Color(0xFF2B3230),
    inversePrimary = Color(0xFF006A60),
    surfaceContainerLowest = Color(0xFF090F0E),
    surfaceContainerLow = Color(0xFF161D1C),
    surfaceContainer = Color(0xFF1A2120),
    surfaceContainerHigh = Color(0xFF252B2A),
    surfaceContainerHighest = Color(0xFF303635),
)

/** Zusätzliche Farben, die Material 3 nicht abdeckt. */
@Immutable
data class ExtraColors(
    val flame: Color,
    val onFlame: Color,
    val flameContainer: Color,
    val success: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val accents: List<Accent>,
)

/** Kräftige Farbe plus heller Hintergrund für Avatare von Plänen und Supplements. */
@Immutable
data class Accent(val strong: Color, val soft: Color)

private val LightExtra = ExtraColors(
    flame = Color(0xFFE8590C),
    onFlame = Color(0xFFFFFFFF),
    flameContainer = Color(0xFFFFE3D3),
    success = Color(0xFF2E7D32),
    warning = Color(0xFF8A5100),
    warningContainer = Color(0xFFFFDDB8),
    onWarningContainer = Color(0xFF2C1600),
    accents = listOf(
        Accent(Color(0xFF00897B), Color(0xFFD5F2EE)),
        Accent(Color(0xFFE0A100), Color(0xFFFFF0C7)),
        Accent(Color(0xFFE5533D), Color(0xFFFFE1DB)),
        Accent(Color(0xFF1E88E5), Color(0xFFDCEBFB)),
        Accent(Color(0xFF8E44AD), Color(0xFFF1E2F7)),
        Accent(Color(0xFF3F51B5), Color(0xFFE1E5F8)),
        Accent(Color(0xFFD81B60), Color(0xFFFBE0EA)),
        Accent(Color(0xFF43A047), Color(0xFFE0F2E1)),
    ),
)

private val DarkExtra = ExtraColors(
    flame = Color(0xFFFF8A4C),
    onFlame = Color(0xFF3A1300),
    flameContainer = Color(0xFF5A2600),
    success = Color(0xFF81C784),
    warning = Color(0xFFFFB95F),
    warningContainer = Color(0xFF693C00),
    onWarningContainer = Color(0xFFFFDDB8),
    accents = listOf(
        Accent(Color(0xFF4DD0C1), Color(0xFF12322E)),
        Accent(Color(0xFFFFD25E), Color(0xFF3A2E07)),
        Accent(Color(0xFFFF8A75), Color(0xFF431B14)),
        Accent(Color(0xFF7DB8F5), Color(0xFF13293F)),
        Accent(Color(0xFFD39BEA), Color(0xFF34203D)),
        Accent(Color(0xFF9FA8F0), Color(0xFF1F2445)),
        Accent(Color(0xFFF48FB1), Color(0xFF431829)),
        Accent(Color(0xFF8BD18E), Color(0xFF1A3A1C)),
    ),
)

val LocalExtraColors = staticCompositionLocalOf { LightExtra }

object FitTheme {
    val extra: ExtraColors @Composable get() = LocalExtraColors.current

    @Composable
    fun accent(index: Int): Accent = extra.accents[Math.floorMod(index, extra.accents.size)]
}

const val ACCENT_COUNT = 8

private val BaseTypography = Typography()

private val AppTypography = BaseTypography.copy(
    displayLarge = BaseTypography.displayLarge.copy(fontWeight = FontWeight.SemiBold),
    displayMedium = BaseTypography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineLarge = BaseTypography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
    headlineMedium = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
)

@Composable
fun FitPlanTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkColors
        else -> LightColors
    }
    CompositionLocalProvider(LocalExtraColors provides if (dark) DarkExtra else LightExtra) {
        MaterialTheme(colorScheme = colors, typography = AppTypography, content = content)
    }
}
