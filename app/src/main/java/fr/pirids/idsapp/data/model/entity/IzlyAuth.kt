package fr.pirids.idsapp.data.model.entity

import androidx.annotation.NonNull
import androidx.room.*

@Entity(
    tableName = "izly_auth",
    foreignKeys = [
        ForeignKey(
            entity = ApiAuth::class,
            parentColumns = ["id"],
            childColumns = ["api_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["api_id"], unique = true)]
)
data class IzlyAuth(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

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