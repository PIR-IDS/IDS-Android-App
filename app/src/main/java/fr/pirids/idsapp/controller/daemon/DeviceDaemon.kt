package fr.pirids.idsapp.controller.daemon

import android.content.Context
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.data.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.data.model.AppDatabase

object DeviceDaemon {
    suspend fun searchForDevice(context: Context, ble: BluetoothConnection): Boolean {
        var searchForKnownDevices = false
        AppDatabase.getInstance(context).deviceDao().getAll().forEach {
            if(!searchForKnownDevices) {
                searchForKnownDevices = true
            }
            Device.addToKnownDevices(BluetoothDeviceIDS(it.name, it.address, ble.getDeviceDataByName(it.name)))
        }
        return searchForKnownDevices
    }
}