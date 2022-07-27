package fr.pirids.idsapp.data.model.entity.device

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallet_card_data",
    foreignKeys = [
        ForeignKey(
            entity = DeviceData::class,
            parentColumns = ["id"],
            childColumns = ["device_data_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WalletCardData(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @NonNull
    @ColumnInfo(name = "device_data_id", index = true)
    val deviceDataId: Int,

    @NonNull
    @ColumnInfo(name = "wallet_out_timestamp")
    val walletOutTimestamp: Long,
)