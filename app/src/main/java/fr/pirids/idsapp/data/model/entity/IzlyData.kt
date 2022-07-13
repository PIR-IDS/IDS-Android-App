package fr.pirids.idsapp.data.model.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "izly_data", foreignKeys = [
    ForeignKey(entity = DeviceData::class, parentColumns = ["id"], childColumns = ["api_id"])
])
data class IzlyData(
    @NonNull
    @PrimaryKey
    val id: Int,

    @NonNull
    @ColumnInfo(name = "api_id")
    val apiId: Int,

    @NonNull
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "amount")
    val amount: Int,

    @ColumnInfo(name = "localization")
    val localization: String,
)