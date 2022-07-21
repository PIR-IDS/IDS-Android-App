package fr.pirids.idsapp.controller.detection

import android.content.Context
import android.util.Log
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.data.api.data.IzlyData
import fr.pirids.idsapp.data.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.data.device.data.WalletCardData
import fr.pirids.idsapp.data.items.ServiceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import fr.pirids.idsapp.data.items.Service as ServiceItem

object Detection {
    private const val checkingDelayMillis = 15_000L
    private const val timeTolerance = 10_000.0
    private var startupTimestamp: Long = System.currentTimeMillis()
    private val scope = CoroutineScope(Dispatchers.Default)
    private val detectedIntrusions = mutableListOf<Long>()

    fun launchDetection(context: Context) {
        scope.launch { monitorServices(context) }
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
                    // We retrieve the service data
                    when (val apiData = it.getData()) {
                        is IzlyData -> {
                            val compatibleDevices = ServiceItem.get(ServiceId.IZLY).compatibleDevices

                            if(it in Service.monitoredServices.value && Device.connectedDevices.value.none { device -> Device.getDeviceItemFromBluetoothDevice(device) in compatibleDevices }) {
                                // We mark the service as not monitored
                                Service.monitoredServices.value = Service.monitoredServices.value.minus(it)
                            }

                            // We list all the devices connected and we check if some of them are compatible with the service
                            Device.connectedDevices.value.forEach { device ->
                                if (Device.getDeviceItemFromBluetoothDevice(device) in compatibleDevices) {
                                    // We mark the service as monitored
                                    if(it !in Service.monitoredServices.value) {
                                        Service.addToMonitoredServices(it)
                                    }
                                    // We analyze the data
                                    analyzeIzlyData(context, device, apiData)
                                }
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
    private suspend fun analyzeIzlyData(context: Context, device: BluetoothDeviceIDS, apiData: IzlyData) {
        try {
            when (val devData = device.data) {
                is WalletCardData -> {
                    apiData.transactionList.forEach { timestamp ->
                        // If the timestamp is in the past, it means that the transaction won't be processed
                        if(timestamp > startupTimestamp && timestamp !in detectedIntrusions) {

                            var intrusionDetected = true
                            devData.whenWalletOutArray.value.forEach { idsTime ->
                                val idsTimestamp = idsTime.toInstant().toEpochMilli()
                                Log.i("DETECTION", "IDS timestamp : $idsTimestamp")

                                if (abs(timestamp - idsTimestamp) < timeTolerance) {
                                    intrusionDetected = false
                                }
                            }

                            if (intrusionDetected) {
                                detectedIntrusions.add(timestamp)
                                NotificationHandler.triggerNotification(context, timestamp.toString())
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Detection", "Error while getting data from device", e)
        }
    }

    private suspend fun updateDeviceData(context: Context) : Nothing {
        while(true) {
            Device.connectedDevices.value.forEach {
                try {
                    when (val data = it.data) {
                        is WalletCardData -> {

                        }
                    }
                } catch (e: Exception) {
                    Log.e("Detection", "Error while getting data from device", e)
                }
            }
            delay(checkingDelayMillis)
        }
    }
}