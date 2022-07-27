package fr.pirids.idsapp.data.model.entity.service

import androidx.annotation.NonNull
import androidx.room.*

@Entity(
    tableName = "api_auth",
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
data class ApiAuth(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @NonNull
    @ColumnInfo(name = "service_id", index = true)
    val serviceId: Int
)
