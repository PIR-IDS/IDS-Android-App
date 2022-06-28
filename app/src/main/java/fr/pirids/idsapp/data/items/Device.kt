package fr.pirids.idsapp.data.items

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import fr.pirids.idsapp.R
import fr.pirids.idsapp.data.items.bluetooth.BluetoothService
import fr.pirids.idsapp.data.items.bluetooth.ServiceId

enum class DeviceId {
    WALLET_CARD,
}

/**
 * All the supported sensors
 * We will use the name of the sensor as the identifier to link the BLE sensor to its Device type
 */
data class Device(val id: DeviceId, val name: String, @StringRes val description: Int, @DrawableRes val logo: Int, val bluetoothServices : List<BluetoothService>) {
    companion object {
        const val idsPrefix = "PIR-IDS"
        val list = listOf(
            Device(DeviceId.WALLET_CARD, "$idsPrefix WALLET CARD", R.string.wallet_card_desc, R.drawable.ids_logo, listOf(
                BluetoothService.get(ServiceId.CURRENT_TIME),
                BluetoothService.get(ServiceId.CUSTOM_IDS_IMU),
            )),
        )
        fun get(id: DeviceId) = list.first { it.id == id }
    }
    fun getBluetoothService(id: ServiceId) : BluetoothService? = list.flatMap { it.bluetoothServices }.find { it.id == id }
}
