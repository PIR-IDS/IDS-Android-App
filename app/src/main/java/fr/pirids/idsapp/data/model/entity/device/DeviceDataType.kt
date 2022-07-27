package fr.pirids.idsapp.data.model.entity.device

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_data_type",
    indices = [Index(value = ["data_name"], unique = true)]
)
data class DeviceDataType(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @NonNull
    @ColumnInfo(name = "data_name")
    val dataName: String
)
