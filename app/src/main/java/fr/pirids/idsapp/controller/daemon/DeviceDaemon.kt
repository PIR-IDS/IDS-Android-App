package fr.pirids.idsapp.controller.daemon

import android.util.Log
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.data.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.data.model.AppDatabase

object DeviceDaemon {
    suspend fun searchForDevice(ble: BluetoothConnection): Boolean {
        var searchForKnownDevices = false
        try {
            AppDatabase.getInstance().deviceDao().getAll().forEach {
                if(!searchForKnownDevices) {
                    searchForKnownDevices = true
                }
                try {
                    Device.addToKnownDevices(BluetoothDeviceIDS(it.name, it.address, ble.getDeviceDataByName(it.name)))
                } catch (e: Exception) {
                    Log.e("DeviceDaemon", "Error while adding device to known devices", e)
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceDaemon", "Error while getting all devices", e)
        }
        return searchForKnownDevices
    }
}