package fr.pirids.idsapp.model.device.data

import android.bluetooth.BluetoothGattCharacteristic
import java.time.ZonedDateTime

class WalletCardData(
    val whenWalletOutArray: MutableSet<ZonedDateTime> = mutableSetOf<ZonedDateTime>(),
    var walletOutCharacteristic: BluetoothGattCharacteristic? = null,
    var whenWalletOutCharacteristic: BluetoothGattCharacteristic? = null,
) : DeviceData()