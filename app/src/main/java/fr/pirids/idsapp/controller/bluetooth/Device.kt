package fr.pirids.idsapp.controller.bluetooth

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.model.device.bluetooth.BluetoothDeviceIDS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import fr.pirids.idsapp.model.items.Device as DeviceItem

object Device {
    val foundDevices : MutableState<Set<BluetoothDeviceIDS>> = mutableStateOf(setOf())
    fun getDeviceItemFromBluetoothDevice(bleDevice: BluetoothDeviceIDS): DeviceItem? = DeviceItem.list.find { it.name == bleDevice.name }
    fun getDeviceItemFromName(name: String): DeviceItem? = DeviceItem.list.find { it.name == name }
    fun getBluetoothDeviceFromDeviceItem(device: DeviceItem): BluetoothDeviceIDS? = foundDevices.value.find { it.name == device.name }
    fun getScannedDevices(): List<DeviceItem> = foundDevices.value.mapNotNull { getDeviceItemFromBluetoothDevice(it) }

    fun connectToDevice(
        device: DeviceItem,
        ble: BluetoothConnection,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) = getBluetoothDeviceFromDeviceItem(device)
        ?.let { bleDevice -> scope.launch {
            bleDevice.device
                ?.let { ble.connect(bleDevice) }
                ?: throw Exception("Device null") }
        } ?: throw Exception("Device not found")
}