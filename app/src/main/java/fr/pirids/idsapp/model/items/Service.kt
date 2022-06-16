package fr.pirids.idsapp.model.items

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import fr.pirids.idsapp.R

enum class ServiceId {
    IZLY
}

data class Service(val id: ServiceId, val name: String, @StringRes val description: Int, @DrawableRes val logo: Int, val compatibleDevices: List<Device>) {
    companion object {
        val list = listOf(
            Service(ServiceId.IZLY, "IZLY", R.string.izly_description, R.drawable.izly_logo, listOf(Device.get(DeviceId.WALLET_CARD))),
        )
        fun get(id: ServiceId) = list.first { it.id == id }
    }
}
