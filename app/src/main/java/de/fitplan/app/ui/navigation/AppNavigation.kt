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

@Serializable data object ExerciseLibraryRoute

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

@Composable
fun FitPlanNavHost(data: AppData, launchRequest: LaunchRequest?, onLaunchHandled: () -> Unit) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val showBar = Tab.entries.any { tab -> destination?.hasRoute(tab.route::class) == true }

    LaunchedEffect(launchRequest) {
        val request = launchRequest ?: return@LaunchedEffect
        when (request.open) {
            MainActivity.OPEN_PLAYER -> request.planId?.let { nav.navigate(PlayerRoute(it)) { launchSingleTop = true } }
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
            composable<HomeRoute> {
                HomeScreen(
                    data = data,
                    onStartPlan = { nav.navigate(PlayerRoute(it)) },
                    onOpenPlans = { nav.navigateToTab(PlansRoute) },
                    onOpenSupplements = { nav.navigateToTab(SupplementsRoute) },
                    onAddSupplement = { nav.navigate(SupplementEditorRoute()) },
                    onOpenSettings = { nav.navigate(SettingsRoute) },
                )
            }
            composable<PlansRoute> {
                PlansScreen(
                    data = data,
                    onStartPlan = { nav.navigate(PlayerRoute(it)) },
                    onEditPlan = { nav.navigate(PlanEditorRoute(planId = it)) },
                    onNewPlan = { nav.navigate(PlanEditorRoute(templateKey = it)) },
                    onOpenLibrary = { nav.navigate(ExerciseLibraryRoute) },
                )
            }
            composable<SupplementsRoute> {
                SupplementsScreen(
                    data = data,
                    onEdit = { nav.navigate(SupplementEditorRoute(supplementId = it)) },
                    onAdd = { nav.navigate(SupplementEditorRoute(templateKey = it)) },
                )
            }
            composable<HistoryRoute> { HistoryScreen(data = data) }
            composable<SettingsRoute> { SettingsScreen(data = data, onBack = { nav.popBackStack() }) }
            composable<ExerciseLibraryRoute> { ExerciseLibraryScreen(data = data, onBack = { nav.popBackStack() }) }
            composable<PlanEditorRoute> { entry ->
                val route = entry.toRoute<PlanEditorRoute>()
                PlanEditorScreen(
                    data = data,
                    planId = route.planId,
                    templateKey = route.templateKey,
                    onClose = { nav.popBackStack() },
                )
            }
            composable<PlayerRoute> { entry ->
                PlayerScreen(planId = entry.toRoute<PlayerRoute>().planId, onClose = { nav.popBackStack() })
            }
            composable<SupplementEditorRoute> { entry ->
                val route = entry.toRoute<SupplementEditorRoute>()
                SupplementEditorScreen(
                    data = data,
                    supplementId = route.supplementId,
                    templateKey = route.templateKey,
                    onClose = { nav.popBackStack() },
                )
            }
        }
    }
}
