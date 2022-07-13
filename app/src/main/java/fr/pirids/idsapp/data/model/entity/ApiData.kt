package fr.pirids.idsapp.data.model.entity

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_data")
data class ApiData(
    @NonNull
    @PrimaryKey
    val id: Int
)