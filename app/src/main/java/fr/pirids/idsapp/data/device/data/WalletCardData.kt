package fr.pirids.idsapp.data.device.data

import android.bluetooth.BluetoothGattCharacteristic
import java.time.ZonedDateTime

class WalletCardData(
    val whenWalletOutArray: MutableSet<ZonedDateTime> = mutableSetOf(),
    var walletOutCharacteristic: BluetoothGattCharacteristic? = null,
    var whenWalletOutCharacteristic: BluetoothGattCharacteristic? = null,
) : DeviceData()