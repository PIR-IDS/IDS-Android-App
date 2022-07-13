package fr.pirids.idsapp.data.model.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_card_data", foreignKeys = [
    ForeignKey(entity = DeviceData::class, parentColumns = ["id"], childColumns = ["device_data_id"])
])
data class WalletCardData(
    @NonNull
    @PrimaryKey
    val id: Int,

    @NonNull
    @ColumnInfo(name = "device_data_id")
    val deviceDataId: Int,

    @NonNull
    @ColumnInfo(name = "wallet_out_timestamp")
    val walletOutTimestamp: Long,
)