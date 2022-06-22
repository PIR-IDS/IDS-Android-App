package fr.pirids.idsapp.controller.bluetooth

import fr.pirids.idsapp.model.items.bluetooth.BluetoothDeviceIDS

object Device {
    val foundDevices : MutableSet<BluetoothDeviceIDS> = mutableSetOf()
}