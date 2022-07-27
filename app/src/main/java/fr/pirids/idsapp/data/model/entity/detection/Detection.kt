package fr.pirids.idsapp.data.model.entity.detection

import androidx.annotation.NonNull
import androidx.room.*
import fr.pirids.idsapp.data.model.entity.service.ApiData

@Entity(
    tableName = "detection",
    foreignKeys = [
        ForeignKey(
            entity = ApiData::class,
            parentColumns = ["id"],
            childColumns = ["api_data_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Detection(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @NonNull
    @ColumnInfo(name = "api_data_id", index = true)
    val apiDataId: Int,

    @NonNull
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
)
