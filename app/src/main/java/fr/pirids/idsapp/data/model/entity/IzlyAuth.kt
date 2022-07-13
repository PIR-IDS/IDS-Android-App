package fr.pirids.idsapp.data.model.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "izly_auth", foreignKeys = [
    ForeignKey(entity = ApiAuth::class, parentColumns = ["id"], childColumns = ["api_id"])
])
data class IzlyAuth(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @NonNull
    @ColumnInfo(name = "api_id")
    val apiId: Int,

    @NonNull
    @ColumnInfo(name = "identifier")
    val identifier: String,

    @NonNull
    @ColumnInfo(name = "password")
    val password: String
)