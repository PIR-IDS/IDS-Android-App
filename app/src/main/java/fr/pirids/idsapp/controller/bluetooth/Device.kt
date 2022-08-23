package fr.pirids.idsapp.controller.bluetooth

import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.data.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.data.device.data.DeviceData
import fr.pirids.idsapp.data.device.data.WalletCardData
import fr.pirids.idsapp.data.items.DeviceId
import fr.pirids.idsapp.data.model.AppDatabase
import fr.pirids.idsapp.data.model.entity.device.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import fr.pirids.idsapp.data.items.Device as DeviceItem

object Device {
    val foundDevices : MutableState<Set<BluetoothDeviceIDS>> = mutableStateOf(setOf())
    val knownDevices : MutableState<Set<BluetoothDeviceIDS>> = mutableStateOf(setOf())
    val connectedDevices : MutableState<Set<BluetoothDeviceIDS>> = mutableStateOf(setOf())

    fun getBluetoothDeviceFromAddress(address: String) : BluetoothDeviceIDS? = knownDevices.value.find { it.address.uppercase() == address.uppercase() }
    fun getDeviceItemFromBluetoothDevice(bleDevice: BluetoothDeviceIDS): DeviceItem? = DeviceItem.list.find { it.name == bleDevice.name }
    fun getDeviceItemFromName(name: String): DeviceItem? = DeviceItem.list.find { it.name == name }

    fun getDeviceDataByName(name: String) : DeviceData =
        when(getDeviceItemFromName(name)?.id) {
            DeviceId.WALLET_CARD -> WalletCardData()
            else -> throw Exception("Unknown device type")
        }

    fun connectToDevice(
        device: BluetoothDeviceIDS,
        ble: BluetoothConnection,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        managedActivity: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        onConnected: (Boolean) -> Unit
    ) = scope.launch {
            device.device
                ?.let { ble.initiateAssociation(managedActivity, device.address, onConnected) }
                ?: run { onConnected(false) ; throw Exception("Device null") }
    }

    fun addKnownDeviceToDatabase(device: BluetoothDeviceIDS) {
        CoroutineScope(Dispatchers.IO).launch {
            val deviceEntity = Device(name = device.name, address = device.address)
            try {
                AppDatabase.getInstance().deviceDao().getFromAddress(device.address)
                    ?.let {
                        AppDatabase.getInstance().deviceDao().update(
                            Device(
                                id = it.id,
                                name = deviceEntity.name,
                                address = deviceEntity.address
                            )
                        )
                    } ?: AppDatabase.getInstance().deviceDao().insert(deviceEntity)
            } catch (e: Exception) {
                Log.e("Service", "Error while saving device in database: $e")
            }
        }
    }

    /**
     * We have to compare manually the content of the Set because the unicity of the Set is not based
     * on the address of the device even when the Comparator is based on it.
     */
    private fun addToDevicesList(newDevice: BluetoothDeviceIDS, list: MutableState<Set<BluetoothDeviceIDS>>) : Boolean {
        list.value.forEach {
            // If that's the same device, we don't add it
            if (BluetoothDeviceIDS.comparator.compare(it, newDevice) == 0) return@addToDevicesList false
        }

        // If we try to add a known device in the found list, we don't add it
        if (list.hashCode() == foundDevices.hashCode() &&
            knownDevices.value.any { knownDev ->
                BluetoothDeviceIDS.comparator.compare(knownDev, newDevice) == 0
            }
        ) return false

        list.value = list.value.plus(newDevice)
        Log.i("Device", "Added ${newDevice.address} to list #${list.hashCode()}")

        return true
    }

    fun addToFoundDevices(newDevice: BluetoothDeviceIDS) : Boolean = addToDevicesList(newDevice, foundDevices)
    fun addToKnownDevices(newDevice: BluetoothDeviceIDS) : Boolean = addToDevicesList(newDevice, knownDevices)
    fun addToConnectedDevices(newDevice: BluetoothDeviceIDS) : Boolean = addToDevicesList(newDevice, connectedDevices)

    fun removeFromConnectedDevices(device: BluetoothDeviceIDS) : Boolean =
        if(device in connectedDevices.value) {
            connectedDevices.value = connectedDevices.value.minus(device)
            true
        } else false
}