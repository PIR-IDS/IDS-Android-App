package fr.pirids.idsapp.model.items.bluetooth

import java.util.*

enum class CharacteristicId {
    CCCD,
    CURRENT_TIME,
    DATE_UTC,
    BOOLEAN
}

data class BluetoothCharacteristic(val id: CharacteristicId, val uuid: UUID) {
    companion object {
        val list = listOf(
            BluetoothCharacteristic(CharacteristicId.CCCD, UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")),
            BluetoothCharacteristic(CharacteristicId.BOOLEAN, UUID.fromString("00002AE2-0000-1000-8000-00805f9b34fb")),
            BluetoothCharacteristic(CharacteristicId.DATE_UTC, UUID.fromString("00002AED-0000-1000-8000-00805f9b34fb")),
            BluetoothCharacteristic(CharacteristicId.CURRENT_TIME, UUID.fromString("00002A2B-0000-1000-8000-00805f9b34fb")),
        )
        fun get(id: CharacteristicId) = list.first { it.id == id }
        fun get(uuid: UUID) = list.first { it.uuid == uuid }
    }
}