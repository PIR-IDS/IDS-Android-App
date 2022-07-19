package fr.pirids.idsapp.data.navigation

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object AddService : NavRoutes("add_service")
    object Service : NavRoutes("service")
    object AddDevice : NavRoutes("add_device")
    object Device : NavRoutes("device")
    object Settings : NavRoutes("settings")
    object Notification : NavRoutes("notification")
    object NotificationDescription : NavRoutes("notification_description")
}