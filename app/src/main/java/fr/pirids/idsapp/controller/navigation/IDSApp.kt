package fr.pirids.idsapp.controller.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import fr.pirids.idsapp.data.items.Device
import fr.pirids.idsapp.data.items.DeviceId
import fr.pirids.idsapp.data.items.Service
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.navigation.NavRoutes
import fr.pirids.idsapp.ui.views.HomeView
import fr.pirids.idsapp.ui.views.device.AddDeviceView
import fr.pirids.idsapp.ui.views.device.DeviceView
import fr.pirids.idsapp.ui.views.service.AddServiceView
import fr.pirids.idsapp.ui.views.service.ServiceView
import fr.pirids.idsapp.ui.views.errors.NotFoundView
import fr.pirids.idsapp.ui.views.menus.NotificationDescriptionView
import fr.pirids.idsapp.ui.views.menus.NotificationView
import fr.pirids.idsapp.ui.views.menus.SettingsView

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IDSApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    startDestination: String = NavRoutes.Home.route
) {
    val appSnackbarHostState = remember { SnackbarHostState() }
    val tweenDuration = 500

    AnimatedNavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(
            route = NavRoutes.Home.route,
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.Home.deepLink }),
        ) { HomeView(navController) }

        composable(
            route = NavRoutes.AddService.route,
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.AddService.deepLink }),
            enterTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Home.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(tweenDuration))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    NavRoutes.Home.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(tweenDuration))
                    else -> null
                }
            }
        ) { AddServiceView(navController) }

        composable(
            route = NavRoutes.Service.route + "/{id}",
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.Service.deepLink + "/{id}" }),
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
            enterTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Home.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(tweenDuration))
                    NavRoutes.AddService.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(tweenDuration))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    NavRoutes.Home.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(tweenDuration))
                    NavRoutes.AddService.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(tweenDuration))
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

        composable(
            route = NavRoutes.AddDevice.route,
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.AddDevice.deepLink }),
            enterTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Home.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(tweenDuration))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    NavRoutes.Home.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(tweenDuration))
                    else -> null
                }
            }
        ) { AddDeviceView(navController, appSnackbarHostState = appSnackbarHostState) }

        composable(
            route = NavRoutes.Device.route + "/{id}?address={address}",
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.Device.deepLink + "/{id}?address={address}" }),
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("address") { type = NavType.StringType },
            ),
            enterTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Home.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(tweenDuration))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    NavRoutes.Home.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(tweenDuration))
                    else -> null
                }
            }
        ) {
            it.arguments?.getInt("id")
                ?.let { id -> DeviceId.values().getOrNull(id) }
                ?.let { devId -> Device.get(devId) }
                ?.let { device -> DeviceView(navController, device, (it.arguments?.getString("address") ?: "")) }
                ?: NotFoundView(navController)
        }

        composable(
            route = NavRoutes.Settings.route,
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.Settings.deepLink }),
            enterTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Home.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Start, animationSpec = tween(tweenDuration))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    NavRoutes.Home.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.End, animationSpec = tween(tweenDuration))
                    else -> null
                }
            }
        ) { SettingsView(navController) }

        composable(
            route = NavRoutes.Notification.route,
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.Notification.deepLink }),
            enterTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Home.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Start, animationSpec = tween(tweenDuration))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    NavRoutes.Home.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.End, animationSpec = tween(tweenDuration))
                    else -> null
                }
            }
        ) { NotificationView(navController) }

        composable(
            route = NavRoutes.NotificationDescription.route + "/{id}",
            deepLinks = listOf(navDeepLink { uriPattern = NavRoutes.NotificationDescription.deepLink + "/{id}" }),
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
            enterTransition = {
                when (initialState.destination.route) {
                    NavRoutes.Notification.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(tweenDuration))
                    NavRoutes.Home.route -> slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(tweenDuration))
                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    NavRoutes.Notification.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(tweenDuration))
                    NavRoutes.Home.route -> slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(tweenDuration))
                    else -> null
                }
            }
        ) {
            it.arguments?.getInt("id")
                ?.let { NotificationDescriptionView(navController) }
                ?: NotFoundView(navController)
        }
    }
}
