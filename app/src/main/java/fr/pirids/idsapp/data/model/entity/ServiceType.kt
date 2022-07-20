package fr.pirids.idsapp.data.model.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_type",
    indices = [Index(value = ["service_name"], unique = true)]
)
data class ServiceType(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @NonNull
    @ColumnInfo(name = "service_name")
    val serviceName: String
)
