package fr.pirids.idsapp.controller.detection

import android.util.Log
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.model.api.data.IzlyData
import fr.pirids.idsapp.model.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.model.device.data.WalletCardData
import fr.pirids.idsapp.model.items.ServiceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import fr.pirids.idsapp.model.items.Service as ServiceItem

object Detection {
    private const val checkingDelayMillis = 15_000L
    private const val timeTolerance = 10_000.0
    private var startupTimestamp: Long = System.currentTimeMillis()
    private val scope = CoroutineScope(Dispatchers.Default)

    fun launchDetection() {
        scope.launch { monitorServices() }
    }

    private suspend fun monitorServices() : Nothing {
        while(true) {
            startupTimestamp = System.currentTimeMillis()

            // For each service connected we are checking its compatible devices
            Service.connectedServices.value.forEach {
                try {
                    // We retrieve the service data
                    when (val apiData = it.getData()) {
                        is IzlyData -> {
                            val compatibleDevices = ServiceItem.get(ServiceId.IZLY).compatibleDevices

                            // We list all the devices connected and we check if some of them are compatible with the service
                            Device.connectedDevices.value.forEach { device ->
                                if (Device.getDeviceItemFromBluetoothDevice(device) in compatibleDevices) {
                                    // We analyze the data
                                    analyzeIzlyData(device, apiData)
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
    private fun analyzeIzlyData(device: BluetoothDeviceIDS, apiData: IzlyData) {
        try {
            when (val devData = device.data) {
                is WalletCardData -> {
                    apiData.transactionList.forEach { timestamp ->

                        // If the timestamp is in the past, it means that the transaction won't be processed
                        if(timestamp > startupTimestamp) {

                            var intrusionDetected = true
                            devData.whenWalletOutArray.forEach { idsTime ->
                                val idsTimestamp = idsTime.toInstant().toEpochMilli()
                                Log.i("DETECTION", "IDS timestamp : $idsTimestamp")

                                if (abs(timestamp - idsTimestamp) < timeTolerance) {
                                    intrusionDetected = false
                                }
                            }

                            if (intrusionDetected) {
                                NotificationHandler.triggerNotification(timestamp.toString())
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Detection", "Error while getting data from device", e)
        }
    }

    private suspend fun updateDeviceData() : Nothing {
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