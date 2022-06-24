package fr.pirids.idsapp.controller.view.device

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavHostController
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.model.items.bluetooth.BluetoothDeviceIDS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import fr.pirids.idsapp.model.items.Device as DeviceItem

object AddDeviceViewController {
    fun closeModal(navController: NavHostController) = navController.popBackStack()
    fun getDeviceItemFromBluetoothDevice(bleDevice: BluetoothDeviceIDS): DeviceItem? = DeviceItem.list.find { it.name == bleDevice.name }
    fun getBluetoothDeviceFromDeviceItem(device: DeviceItem): BluetoothDeviceIDS? = Device.foundDevices.value.find { it.name == device.name }
    fun getScannedDevices(): List<DeviceItem> = Device.foundDevices.value.mapNotNull { getDeviceItemFromBluetoothDevice(it) }

    fun connectToDevice(
        device: DeviceItem,
        ble: BluetoothConnection,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ) = getBluetoothDeviceFromDeviceItem(device)
        ?.let { bleDevice -> scope.launch {
            bleDevice.device
            ?.let { ble.connect(it) }
            ?: throw Exception("Device null") }
        } ?: throw Exception("Device not found")
}