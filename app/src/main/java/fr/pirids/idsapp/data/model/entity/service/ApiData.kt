package fr.pirids.idsapp.data.model.entity.service

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "api_data",
    foreignKeys = [
        ForeignKey(
            entity = ServiceType::class,
            parentColumns = ["id"],
            childColumns = ["service_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ApiData(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @NonNull
    @ColumnInfo(name = "service_id", index = true)
    val serviceId: Int
)