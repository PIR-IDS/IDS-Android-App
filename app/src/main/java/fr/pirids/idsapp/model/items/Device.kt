package fr.pirids.idsapp.model.items

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import fr.pirids.idsapp.R
import fr.pirids.idsapp.model.items.bluetooth.BluetoothService

enum class DeviceId {
    WALLET_CARD
}

data class Device(val id: DeviceId, val name: String, @StringRes val description: Int, @DrawableRes val logo: Int, val bluetoothServices : List<BluetoothService>) {
    companion object {
        val list = listOf(
            Device(DeviceId.WALLET_CARD, "WALLET CARD", R.string.wallet_card_desc, R.drawable.ids_logo, listOf())
        )
        fun get(id: DeviceId) = list.first { it.id == id }
    }
}
