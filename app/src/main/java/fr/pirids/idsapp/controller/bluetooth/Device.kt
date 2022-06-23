package fr.pirids.idsapp.controller.bluetooth

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.model.items.bluetooth.BluetoothDeviceIDS

object Device {
    val foundDevices : MutableState<Set<BluetoothDeviceIDS>> = mutableStateOf(setOf())
}