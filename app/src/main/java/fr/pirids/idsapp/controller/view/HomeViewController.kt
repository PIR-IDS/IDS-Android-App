package fr.pirids.idsapp.controller.view

import android.bluetooth.BluetoothAdapter
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavHostController
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.model.items.DeviceId
import fr.pirids.idsapp.model.items.Service
import fr.pirids.idsapp.model.items.ServiceId
import fr.pirids.idsapp.model.navigation.NavRoutes
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

object HomeViewController {


    //lateinit var notifBuilder : NotificationCompat.Builder

    val CHANNEL_ID = "ids"

    var notificationId = 0

    /*
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name = "IDS"
            val descriptionText = "Intrusion detected"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            intent.setAction(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val fullScreenIntent = Intent(this, AlertNotificationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(this, 1, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            // notification push
            notifBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(resources.getString(R.string.alert_notify))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)
    */

        /*private fun triggerNotification(serviceTime: Long) {
        val time = Instant.ofEpochMilli(serviceTime).atZone(ZoneId.of("UTC")).withZoneSameInstant(
            TimeZone.getDefault().toZoneId())
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(
                notificationId,
                //notifBuilder.setContentText("at $time").build()
            )
            notificationId++
        }
    }
*/
    fun addService(navController: NavHostController) {
        navController.navigate(NavRoutes.AddService.route)
    }

    fun showService(navController: NavHostController, service: ServiceId) {
        navController.navigate(NavRoutes.Service.route + "/" + service.ordinal)
    }

    fun addDevice(navController: NavHostController) {
        navController.navigate(NavRoutes.AddDevice.route)
    }

    fun showDevice(navController: NavHostController, device: DeviceId) {
        navController.navigate(NavRoutes.Device.route + "/" + device.ordinal)
    }
}