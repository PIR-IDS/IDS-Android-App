package fr.pirids.idsapp.data.model.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "device_data", foreignKeys = [
    ForeignKey(entity = DeviceData::class, parentColumns = ["id"], childColumns = ["device_id"])
])
data class DeviceData(
    @NonNull
    @PrimaryKey
    val id: Int,

    @NonNull
    @ColumnInfo(name = "device_id")
    val deviceId: Int,
)
