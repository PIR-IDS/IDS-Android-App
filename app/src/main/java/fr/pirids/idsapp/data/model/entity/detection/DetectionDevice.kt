package fr.pirids.idsapp.data.model.entity.detection

import androidx.annotation.NonNull
import androidx.room.*
import fr.pirids.idsapp.data.model.entity.device.Device

@Entity(
    tableName = "detection_device",
    foreignKeys = [
        ForeignKey(
            entity = Detection::class,
            parentColumns = ["id"],
            childColumns = ["detection_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Device::class,
            parentColumns = ["id"],
            childColumns = ["device_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["detection_id", "device_id"], unique = true)]
)
data class DetectionDevice(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @NonNull
    @ColumnInfo(name = "detection_id")
    val detectionId: Int,

    @NonNull
    @ColumnInfo(name = "device_id", index = true)
    val deviceId: Int
)