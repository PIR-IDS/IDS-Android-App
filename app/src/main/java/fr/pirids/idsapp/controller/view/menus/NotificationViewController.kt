package fr.pirids.idsapp.controller.view.menus

import androidx.navigation.NavHostController
import fr.pirids.idsapp.R
import fr.pirids.idsapp.data.detection.Detection
import fr.pirids.idsapp.data.device.data.WalletCardData
import fr.pirids.idsapp.data.navigation.NavRoutes
import fr.pirids.idsapp.data.notifications.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import fr.pirids.idsapp.controller.detection.Detection as DetectionController

object NotificationViewController {
    fun goBack(navController: NavHostController) = navController.popBackStack()

    fun showDescription(navController: NavHostController) {
        navController.navigate(NavRoutes.NotificationDescription.route + "/" + 1) //TODO: replace 1 with id
    }

    fun getNotificationFromDetection(detection: Detection) : Notification {
        val errorNotification = Notification(
            R.string.not_found,
            R.string.not_found,
            detection.service
        )

        //FIXME: this is NOT a good way to get the kind of intrusion that happened,
        // here we are just retrieving the first service-compatible connected device
        // and assume that it is the one that was used to detect the intrusion, there could be other devices!!!!
        detection.connectedDevicesDuringDetection.forEach {
            return when(it.data) {
                is WalletCardData -> Notification(
                    it.data.intrusionTitle,
                    it.data.intrusionMessage,
                    detection.service
                )
                else -> errorNotification
            }
        }
        return errorNotification
    }

    fun removeDetectionData(detection: Detection) {
        CoroutineScope(Dispatchers.IO).launch {
            DetectionController.removeDetectionData(detection)
        }
    }

    fun removeAllDetectionData() {
        CoroutineScope(Dispatchers.IO).launch {
            DetectionController.removeAllDetectionData()
        }
    }
}