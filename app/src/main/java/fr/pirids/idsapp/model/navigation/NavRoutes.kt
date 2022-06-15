package fr.pirids.idsapp.model.navigation

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object AddService : NavRoutes("add_service")
    object Service : NavRoutes("service")
}