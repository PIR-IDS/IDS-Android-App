package fr.pirids.idsapp.data.model.entity

import androidx.annotation.NonNull
import androidx.room.*

@Entity(
    tableName = "device_data",
    foreignKeys = [
        ForeignKey(
            entity = Device::class,
            parentColumns = ["id"],
            childColumns = ["device_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DeviceDataType::class,
            parentColumns = ["id"],
            childColumns = ["data_type_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["device_id", "data_type_id"], unique = true)]
)
data class DeviceData(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @NonNull
    @ColumnInfo(name = "device_id")
    val deviceId: Int,

    @NonNull
    @ColumnInfo(name = "data_type_id", index = true)
    val dataTypeId: Int
)
