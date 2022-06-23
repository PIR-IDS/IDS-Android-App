package fr.pirids.idsapp.model.items.bluetooth

import android.bluetooth.BluetoothDevice

//TODO: maybe change the name of the device if it has changed
data class BluetoothDeviceIDS(val name: String, val address: String, var device: BluetoothDevice? = null) : Comparator<BluetoothDeviceIDS> {
    override fun compare(o1: BluetoothDeviceIDS, o2: BluetoothDeviceIDS): Int {
        return o1.address.compareTo(o2.address)
    }
}