package fr.pirids.idsapp.controller.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.pirids.idsapp.model.navigation.NavRoutes
import fr.pirids.idsapp.ui.views.HomeView
import fr.pirids.idsapp.ui.views.IzlyView

@Composable
fun IDSApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.Home.route
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(NavRoutes.Home.route) { HomeView(navController) }
        composable(NavRoutes.Izly.route) { IzlyView(navController) }
    }
}

