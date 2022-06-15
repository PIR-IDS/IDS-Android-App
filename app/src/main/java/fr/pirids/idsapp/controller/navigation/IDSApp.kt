package fr.pirids.idsapp.controller.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.model.items.Service
import fr.pirids.idsapp.model.items.ServiceId
import fr.pirids.idsapp.model.navigation.NavRoutes
import fr.pirids.idsapp.ui.views.HomeView
import fr.pirids.idsapp.ui.views.AddServiceView
import fr.pirids.idsapp.ui.views.ServiceView
import fr.pirids.idsapp.ui.views.errors.NotFoundView

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IDSApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    startDestination: String = NavRoutes.Home.route
) {
    AnimatedNavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(
            NavRoutes.Home.route
        ) { HomeView(navController) }

        composable(
            NavRoutes.AddService.route,
            enterTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Home.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(700))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    NavRoutes.Home.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(700))
                    else -> null
                }
            }
        ) { AddServiceView(navController) }

        composable(
            NavRoutes.Service.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
            enterTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Home.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(700))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    NavRoutes.Home.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(700))
                    else -> null
                }
            }
        ) {
            it.arguments?.getInt("id")
                ?.let { id -> ServiceId.values().getOrNull(id) }
                ?.let { servId -> Service.get(servId) }
                ?.let { service -> ServiceView(navController, service) }
                ?: NotFoundView(navController)
        }
    }
}
