package fr.pirids.idsapp.controller.detection

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.data.api.data.IzlyData
import fr.pirids.idsapp.data.detection.Detection
import fr.pirids.idsapp.data.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.data.device.data.WalletCardData
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.model.AppDatabase
import fr.pirids.idsapp.data.model.entity.detection.DetectionDevice
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs
import fr.pirids.idsapp.data.items.Service as ServiceItem
import fr.pirids.idsapp.data.model.entity.detection.Detection as DetectionEntity

object Detection {
    const val CHECKING_DELAY_MILLIS = 15_000L
    private const val TIME_TOLERANCE = 10_000.0
    private var startupTimestamp: Long = System.currentTimeMillis()
    val detectedIntrusions = mutableStateOf(setOf<Detection>())

    suspend fun loadPastDetections(context: Context) {
        try {
            AppDatabase.getInstance(context).detectionDao().getAll().forEach {
                detectedIntrusions.value = detectedIntrusions.value.plus(
                    Detection(
                        it.timestamp,
                        Service.getServiceItemFromTag(
                            AppDatabase.getInstance(context).serviceTypeDao().get(
                                AppDatabase.getInstance(context).apiDataDao()
                                    .get(it.apiDataId).serviceId
                            ).serviceName
                        )!!,
                        AppDatabase.getInstance(context).detectionDeviceDao().getAllFromDetectionId(it.id).map { dd ->
                            Device.getBluetoothDeviceFromAddress(AppDatabase.getInstance(context).deviceDao().get(dd.deviceId).address)!!
                        }
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("Detection", "Error while loading past detected intrusions", e)
        }
    }

    suspend fun removeDetectionData(detection: Detection) {
        detectedIntrusions.value = detectedIntrusions.value.minus(detection)
        try {
            //TODO: improve this, once again we still don't know if we want the timestamp in the ApiData or in the Detection, etc...
            var apiDataId = -1
            when(detection.service.id) {
                ServiceId.IZLY -> {
                    AppDatabase.getInstance().izlyDataDao().getByTimestamp(detection.timestamp)?.let {
                        apiDataId = it.apiId
                    }
                }
            }
            AppDatabase.getInstance().detectionDao().delete(
                AppDatabase.getInstance().detectionDao().getFromApiDataIdAndTimestamp(
                    apiDataId,
                    detection.timestamp
                )
            )
        } catch (e: Exception) {
            Log.e("Detection", "Error while deleting detection data", e)
        }
    }

    suspend fun removeAllDetectionData() {
        detectedIntrusions.value = setOf()
        AppDatabase.getInstance().detectionDao().getAll().forEach { AppDatabase.getInstance().detectionDao().delete(it) }
    }

    suspend fun monitorServices(context: Context) {
        Service.monitoredServices.value.forEach {
            if(it !in Service.connectedServices.value) {
                // We mark the service as not monitored
                Service.monitoredServices.value = Service.monitoredServices.value.minus(it)
            }
        }

        // For each service connected we are checking its compatible devices
        Service.connectedServices.value.forEach {
            try {
                // We check the service data
                when (val apiData = it.data.value) {
                    is IzlyData -> {
                        val compatibleDevices = ServiceItem.get(ServiceId.IZLY).compatibleDevices

                        // We list all the devices connected and we check if some of them are compatible with the service
                        val currentlyConnectedCompatibleDevices = Device.connectedDevices.value.filter { device -> Device.getDeviceItemFromBluetoothDevice(device) in compatibleDevices }

                        if(it in Service.monitoredServices.value && currentlyConnectedCompatibleDevices.isEmpty()) {
                            // We mark the service as not monitored
                            Service.monitoredServices.value = Service.monitoredServices.value.minus(it)
                        }

                        currentlyConnectedCompatibleDevices.forEach { device ->
                            // We mark the service as monitored
                            if(it !in Service.monitoredServices.value) {
                                Service.addToMonitoredServices(it)
                            }
                            // We analyze the data
                            analyzeIzlyData(context, device, apiData, ServiceItem.get(it.serviceId), currentlyConnectedCompatibleDevices)
                        }
                    }
                    else -> {
                        Log.e("Detection", "Unsupported service data type")
                    }
                }
            } catch (e: Exception) {
                Log.e("Detection", "Error while getting data from service", e)
            }
        }
    }

    /**
     * Analyze the data from the device and find out if there is an intrusion by comparing the device data with the service data
     * This also trigger a notification if there is an intrusion
     */
    private suspend fun analyzeIzlyData(context: Context, device: BluetoothDeviceIDS, apiData: IzlyData, service: ServiceItem, currentlyConnectedCompatibleDevices: List<BluetoothDeviceIDS>) {
        try {
            when (val devData = device.data) {
                is WalletCardData -> {
                    apiData.transactionList.forEach { timestamp ->
                        // If the timestamp is in the past, it means that the transaction won't be processed
                        if(timestamp > startupTimestamp && timestamp !in detectedIntrusions.value.map { it.timestamp }) {

                            var intrusionDetected = true
                            devData.whenWalletOutArray.value.forEach { idsTime ->
                                val idsTimestamp = idsTime.toInstant().toEpochMilli()
                                if (abs(timestamp - idsTimestamp) < TIME_TOLERANCE) {
                                    intrusionDetected = false
                                }
                            }

                            if (intrusionDetected) {
                                Log.i("DETECTION", "IDS timestamp : $timestamp")
                                val detection = Detection(timestamp, service, currentlyConnectedCompatibleDevices)
                                detectedIntrusions.value = detectedIntrusions.value.plus(detection)

                                // Trigger a notification
                                NotificationHandler.triggerNotification(
                                    context = context,
                                    title = context.resources.getString(R.string.alert_notify),
                                    message =
                                        context.resources.getString(R.string.suspicious_transaction) +
                                        " " + Instant
                                            .ofEpochMilli(timestamp)
                                            .atZone(ZoneId.of("UTC"))
                                            .withZoneSameInstant(TimeZone.getDefault().toZoneId())
                                            .format(DateTimeFormatter.ofPattern("HH'H'mm:ss (d MMMM yyyy)")),
                                    idsAlert = true
                                )

                                try {
                                    // Save in database
                                    val detectionId = AppDatabase.getInstance().detectionDao().insert(
                                        DetectionEntity(
                                            //TODO: improve this, maybe by including timestamp in ApiData, we will always have it anyway...
                                            // Also the timestamp probably will be unique for EACH service data, so that's why we are doing this
                                            apiDataId =  AppDatabase.getInstance().izlyDataDao().getByTimestamp(detection.timestamp)!!.id,
                                            timestamp = detection.timestamp,
                                        )
                                    )
                                    detection.connectedDevicesDuringDetection.forEach {
                                        AppDatabase.getInstance().detectionDeviceDao().insert(
                                            DetectionDevice(
                                                detectionId = detectionId.toInt(),
                                                deviceId = AppDatabase.getInstance().deviceDao().getFromAddress(it.address)!!.id,
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("Detection", "Error while saving detection in database", e)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Detection", "Error while getting data from device", e)
        }
    }
}