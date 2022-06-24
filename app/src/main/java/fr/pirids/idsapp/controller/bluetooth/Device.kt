package fr.pirids.idsapp.controller.bluetooth

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.model.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.model.device.data.WalletCardData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import fr.pirids.idsapp.model.items.Device as DeviceItem

object Device {
    val foundDevices : MutableState<Set<BluetoothDeviceIDS>> = mutableStateOf(setOf())
    fun getDeviceItemFromBluetoothDevice(bleDevice: BluetoothDeviceIDS): DeviceItem? = DeviceItem.list.find { it.name == bleDevice.name }
    fun getBluetoothDeviceFromDeviceItem(device: DeviceItem): BluetoothDeviceIDS? = foundDevices.value.find { it.name == device.name }
    fun getScannedDevices(): List<DeviceItem> = foundDevices.value.mapNotNull { getDeviceItemFromBluetoothDevice(it) }

    fun connectToDevice(
        device: DeviceItem,
        ble: BluetoothConnection,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) = getBluetoothDeviceFromDeviceItem(device)
        ?.let { bleDevice -> scope.launch {
            bleDevice.device
                ?.let { ble.connect(bleDevice, WalletCardData()) } //TODO: replace this data placeholder with a real data structure
                ?: throw Exception("Device null") }
        } ?: throw Exception("Device not found")
}