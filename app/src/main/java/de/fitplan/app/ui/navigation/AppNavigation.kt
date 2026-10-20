package de.fitplan.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.fitplan.app.MainActivity
import de.fitplan.app.R
import de.fitplan.app.core.model.AppData
import de.fitplan.app.ui.LaunchRequest
import de.fitplan.app.ui.evidence.EvidenceScreen
import de.fitplan.app.ui.history.HistoryScreen
import de.fitplan.app.ui.home.HomeScreen
import de.fitplan.app.ui.player.PlayerScreen
import de.fitplan.app.ui.plans.ExerciseLibraryScreen
import de.fitplan.app.ui.plans.PlanEditorScreen
import de.fitplan.app.ui.plans.PlansScreen
import de.fitplan.app.ui.settings.SettingsScreen
import de.fitplan.app.ui.supplements.SupplementEditorScreen
import de.fitplan.app.ui.supplements.SupplementsScreen
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute

@Serializable data object PlansRoute

@Serializable data object SupplementsRoute

@Serializable data object HistoryRoute

@Serializable data object SettingsRoute

@Serializable data class ExerciseLibraryRoute(val filter: String? = null)

@Serializable data object EvidenceRoute

@Serializable data class PlanEditorRoute(val planId: String? = null, val templateKey: String? = null)

@Serializable data class PlayerRoute(val planId: String)

@Serializable data class SupplementEditorRoute(val supplementId: String? = null, val templateKey: String? = null)

private enum class Tab(val route: Any, @StringRes val label: Int, val icon: ImageVector) {
    HOME(HomeRoute, R.string.tab_today, Icons.Rounded.Home),
    PLANS(PlansRoute, R.string.tab_training, Icons.Rounded.FitnessCenter),
    SUPPLEMENTS(SupplementsRoute, R.string.tab_supplements, Icons.Rounded.Medication),
    HISTORY(HistoryRoute, R.string.tab_history, Icons.Rounded.Insights),
}

fun NavHostController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Navigation aus einem Bildschirm heraus wirkt nur, solange er der aktive ist. Ein doppeltes Tippen
 * öffnet so nichts doppelt und „Zurück“ leert nicht versehentlich den ganzen Verlauf (leerer Bildschirm).
 */
private val NavBackStackEntry.isActive: Boolean get() = lifecycle.currentState == Lifecycle.State.RESUMED

private fun NavHostController.navigateFrom(entry: NavBackStackEntry, route: Any) {
    if (entry.isActive) navigate(route)
}

private fun NavHostController.tabFrom(entry: NavBackStackEntry, route: Any) {
    if (entry.isActive) navigateToTab(route)
}

private fun NavHostController.closeFrom(entry: NavBackStackEntry) {
    if (entry.isActive) popBackStack()
}

@Composable
fun FitPlanNavHost(data: AppData, launchRequest: LaunchRequest?, onLaunchHandled: () -> Unit) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val showBar = Tab.entries.any { tab -> destination?.hasRoute(tab.route::class) == true }

    LaunchedEffect(launchRequest) {
        val request = launchRequest ?: return@LaunchedEffect
        when (request.open) {
            MainActivity.OPEN_PLAYER -> request.planId?.let { planId ->
                // Läuft gerade ein anderes Training, wird es ersetzt; dasselbe Training läuft einfach weiter.
                val top = nav.currentBackStackEntry
                val running = top != null && top.destination.hasRoute(PlayerRoute::class) && top.toRoute<PlayerRoute>().planId == planId
                if (!running) nav.navigate(PlayerRoute(planId)) { popUpTo<PlayerRoute> { inclusive = true } }
            }
            MainActivity.OPEN_SUPPLEMENTS -> nav.navigateToTab(SupplementsRoute)
        }
        onLaunchHandled()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        val selected = destination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { nav.navigateToTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = HomeRoute,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) {
            composable<HomeRoute> { entry ->
                HomeScreen(
                    data = data,
                    onStartPlan = { nav.navigateFrom(entry, PlayerRoute(it)) },
                    onOpenPlans = { nav.tabFrom(entry, PlansRoute) },
                    onOpenSupplements = { nav.tabFrom(entry, SupplementsRoute) },
                    onAddSupplement = { nav.navigateFrom(entry, SupplementEditorRoute()) },
                    onOpenSettings = { nav.navigateFrom(entry, SettingsRoute) },
                )
            }
            composable<PlansRoute> { entry ->
                PlansScreen(
                    data = data,
                    onStartPlan = { nav.navigateFrom(entry, PlayerRoute(it)) },
                    onEditPlan = { nav.navigateFrom(entry, PlanEditorRoute(planId = it)) },
                    onNewPlan = { nav.navigateFrom(entry, PlanEditorRoute(templateKey = it)) },
                    onOpenLibrary = { nav.navigateFrom(entry, ExerciseLibraryRoute()) },
                    onOpenEvidence = { nav.navigateFrom(entry, EvidenceRoute) },
                )
            }
            composable<SupplementsRoute> { entry ->
                SupplementsScreen(
                    data = data,
                    onEdit = { nav.navigateFrom(entry, SupplementEditorRoute(supplementId = it)) },
                    onAdd = { nav.navigateFrom(entry, SupplementEditorRoute(templateKey = it)) },
                )
            }
            composable<HistoryRoute> { HistoryScreen(data = data) }
            composable<SettingsRoute> { entry -> SettingsScreen(data = data, onBack = { nav.closeFrom(entry) }) }
            composable<ExerciseLibraryRoute> { entry ->
                ExerciseLibraryScreen(
                    data = data,
                    initialFilter = entry.toRoute<ExerciseLibraryRoute>().filter,
                    onBack = { nav.closeFrom(entry) },
                    onOpenEvidence = { nav.navigateFrom(entry, EvidenceRoute) },
                )
            }
            composable<EvidenceRoute> { entry ->
                EvidenceScreen(
                    onBack = { nav.closeFrom(entry) },
                    onShowExercises = { approach -> nav.navigateFrom(entry, ExerciseLibraryRoute(filter = approach.name)) },
                )
            }
            composable<PlanEditorRoute> { entry ->
                val route = entry.toRoute<PlanEditorRoute>()
                PlanEditorScreen(
                    data = data,
                    planId = route.planId,
                    templateKey = route.templateKey,
                    onClose = { nav.closeFrom(entry) },
                )
            }
            composable<PlayerRoute> { entry ->
                PlayerScreen(planId = entry.toRoute<PlayerRoute>().planId, onClose = { nav.closeFrom(entry) })
            }
            composable<SupplementEditorRoute> { entry ->
                val route = entry.toRoute<SupplementEditorRoute>()
                SupplementEditorScreen(
                    data = data,
                    supplementId = route.supplementId,
                    templateKey = route.templateKey,
                    onClose = { nav.closeFrom(entry) },
                )
            }
        }
    }
}
