package de.fitplan.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.fitplan.app.AppContainer
import de.fitplan.app.core.model.AppData
import de.fitplan.app.core.model.ThemeMode
import de.fitplan.app.ui.navigation.FitPlanNavHost
import de.fitplan.app.ui.onboarding.OnboardingScreen
import de.fitplan.app.ui.theme.FitPlanTheme
import kotlinx.coroutines.flow.StateFlow

/** Wunsch, beim Start direkt einen Bereich zu öffnen (z. B. aus einer Benachrichtigung). */
data class LaunchRequest(val open: String, val planId: String?)

val LocalActions = staticCompositionLocalOf<AppActions> { error("AppActions fehlen") }
val LocalContainer = staticCompositionLocalOf<AppContainer> { error("AppContainer fehlt") }

@Composable
fun FitPlanRoot(
    container: AppContainer,
    launchRequests: StateFlow<LaunchRequest?>,
    onLaunchHandled: () -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
) {
    val data by container.repository.data.collectAsStateWithLifecycle<AppData?>(initialValue = null)
    val launchRequest by launchRequests.collectAsStateWithLifecycle()
    val actions = remember(container) { AppActions(container) }
    val settings = data?.settings
    val themeMode = settings?.themeMode ?: ThemeMode.SYSTEM
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    LaunchedEffect(dark) { onDarkThemeChanged(dark) }

    CompositionLocalProvider(LocalActions provides actions, LocalContainer provides container) {
        FitPlanTheme(themeMode = themeMode, dynamicColor = settings?.dynamicColor == true) {
            val current = data
            when {
                current == null -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                !current.settings.onboardingDone -> OnboardingScreen()
                else -> FitPlanNavHost(current, launchRequest, onLaunchHandled)
            }
        }
    }
}
