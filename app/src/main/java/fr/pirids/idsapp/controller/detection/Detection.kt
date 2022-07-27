package fr.pirids.idsapp.controller.detection

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.data.api.data.IzlyData
import fr.pirids.idsapp.data.detection.Detection
import fr.pirids.idsapp.data.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.data.device.data.WalletCardData
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.model.AppDatabase
import fr.pirids.idsapp.data.model.entity.detection.DetectionDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import fr.pirids.idsapp.data.items.Service as ServiceItem
import fr.pirids.idsapp.data.model.entity.detection.Detection as DetectionEntity

object Detection {
    private const val checkingDelayMillis = 15_000L
    private const val timeTolerance = 10_000.0
    private var startupTimestamp: Long = System.currentTimeMillis()
    private val scope = CoroutineScope(Dispatchers.Default)
    val detectedIntrusions = mutableStateOf(setOf<Detection>())

    fun launchDetection(context: Context) {
        scope.launch {
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
            monitorServices(context)
        }
    }

    suspend fun removeDetectionData(detection: Detection) {
        detectedIntrusions.value = detectedIntrusions.value.minus(detection)
        //TODO: delete from database
    }

    suspend fun removeAllDetectionData() {
        detectedIntrusions.value = setOf()
        //TODO: delete from database
    }

    private suspend fun monitorServices(context: Context) : Nothing {
        while(true) {
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
            delay(checkingDelayMillis)
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
                                if (abs(timestamp - idsTimestamp) < timeTolerance) {
                                    intrusionDetected = false
                                }
                            }

                            if (intrusionDetected) {
                                Log.i("DETECTION", "IDS timestamp : $timestamp")
                                val detection = Detection(timestamp, service, currentlyConnectedCompatibleDevices)
                                detectedIntrusions.value = detectedIntrusions.value.plus(detection)
                                // Trigger a notification
                                NotificationHandler.triggerNotification(context, timestamp.toString())
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
                                                deviceId = AppDatabase.getInstance().deviceDao().getFromAddress(it.address).id,
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