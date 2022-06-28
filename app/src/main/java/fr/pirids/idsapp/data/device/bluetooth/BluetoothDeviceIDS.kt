package fr.pirids.idsapp.data.device.bluetooth

import android.bluetooth.BluetoothDevice
import fr.pirids.idsapp.data.device.data.DeviceData

//TODO: maybe change the name of the device if it has changed
data class BluetoothDeviceIDS(val name: String, val address: String, val data: DeviceData, var device: BluetoothDevice? = null) : Comparable<BluetoothDeviceIDS> {
    companion object {
        val comparator = Comparator<BluetoothDeviceIDS> { o1, o2 -> o1.address.compareTo(o2.address) }
    }
    override fun compareTo(other: BluetoothDeviceIDS): Int {
        return comparator.compare(this, other)
    }
}