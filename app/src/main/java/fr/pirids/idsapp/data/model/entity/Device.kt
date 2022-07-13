package fr.pirids.idsapp.data.model.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device")
data class Device(
    @NonNull
    @PrimaryKey
    val id: Int,

    @NonNull
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "address")
    val address: String
)
