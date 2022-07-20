package fr.pirids.idsapp.data.device.data

import android.bluetooth.BluetoothGattCharacteristic
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import fr.pirids.idsapp.R
import java.time.ZonedDateTime

class WalletCardData(
    val whenWalletOutArray: MutableState<Set<ZonedDateTime>> = mutableStateOf(setOf()),
    var walletOutCharacteristic: BluetoothGattCharacteristic? = null,
    var whenWalletOutCharacteristic: BluetoothGattCharacteristic? = null,

    override val intrusionTitle: Int = R.string.wallet_intrusion,
    override val intrusionMessage: Int = R.string.suspicious_transaction,
    override val dataTitle: Int = R.string.wallet_data,
    override val dataMessage: Int = R.string.wallet_event_message,
    override val eventIcon: ImageVector = Icons.Outlined.Animation
) : DeviceData() {
    companion object {
        const val tag: String = "wallet_card_data"
    }
}