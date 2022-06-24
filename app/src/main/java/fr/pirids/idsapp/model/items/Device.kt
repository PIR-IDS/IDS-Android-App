package fr.pirids.idsapp.model.items

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import fr.pirids.idsapp.R
import fr.pirids.idsapp.model.items.bluetooth.BluetoothService

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
            Device(DeviceId.WALLET_CARD, "$idsPrefix WALLET CARD", R.string.wallet_card_desc, R.drawable.ids_logo, listOf())
        )
        fun get(id: DeviceId) = list.first { it.id == id }
    }
}
