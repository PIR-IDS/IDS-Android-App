package fr.pirids.idsapp.model.navigation

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Izly : NavRoutes("izly")
}