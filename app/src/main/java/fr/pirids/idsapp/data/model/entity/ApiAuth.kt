package fr.pirids.idsapp.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ApiAuth(
    @PrimaryKey val id: Int
)
