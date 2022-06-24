package fr.pirids.idsapp.model.items.bluetooth

import java.util.*

enum class ServiceId {
    CURRENT_TIME,
    CUSTOM_IDS_IMU
}

data class BluetoothService(val id: ServiceId, val uuid: UUID, val characteristics: List<BluetoothCharacteristic>) {
    companion object {
        val list = listOf(
            BluetoothService(ServiceId.CURRENT_TIME, UUID.fromString("00001805-0000-1000-8000-00805f9b34fb"), listOf(
                BluetoothCharacteristic.get(CharacteristicId.CURRENT_TIME),
            )),
            BluetoothService(ServiceId.CUSTOM_IDS_IMU, UUID.fromString("D70C4BB1-98E4-4EBF-9EA5-F9898690D428"), listOf(
                BluetoothCharacteristic.get(CharacteristicId.DATE_UTC),
                BluetoothCharacteristic.get(CharacteristicId.BOOLEAN),
            )),
        )
        fun get(id: ServiceId) = list.first { it.id == id }
        fun get(uuid: UUID) = list.first { it.uuid == uuid }
    }
    fun getBluetoothCharacteristic(id: CharacteristicId) : BluetoothCharacteristic? = list.flatMap { it.characteristics }.find { it.id == id }
}