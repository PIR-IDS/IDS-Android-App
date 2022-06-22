package fr.pirids.idsapp.model.items.bluetooth

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceIDS(val name: String, val address: String, val device: BluetoothDevice? = null)