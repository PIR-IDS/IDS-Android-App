package fr.pirids.idsapp.data.navigation

sealed class NavRoutes(val route: String, val deepLink: String) {
    companion object {
        const val DEEP_LINK_PREFIX = "idsapp://ids"
    }
    object Home : NavRoutes("home", "$DEEP_LINK_PREFIX/home")
    object AddService : NavRoutes("add_service", "$DEEP_LINK_PREFIX/add_service")
    object Service : NavRoutes("service", "$DEEP_LINK_PREFIX/service")
    object AddDevice : NavRoutes("add_device", "$DEEP_LINK_PREFIX/add_device")
    object Device : NavRoutes("device", "$DEEP_LINK_PREFIX/device")
    object Settings : NavRoutes("settings", "$DEEP_LINK_PREFIX/settings")
    object Notification : NavRoutes("notification", "$DEEP_LINK_PREFIX/notification")
    object NotificationDescription : NavRoutes("notification_description", "$DEEP_LINK_PREFIX/notification_description")
}