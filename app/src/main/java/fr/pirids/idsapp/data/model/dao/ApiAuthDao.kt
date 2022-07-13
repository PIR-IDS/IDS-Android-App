package fr.pirids.idsapp.data.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.pirids.idsapp.data.model.entity.ApiAuth

@Dao
interface ApiAuthDao {
    @Query("SELECT * FROM api_auth")
    fun getAll(): List<ApiAuth>

    @Insert
    fun insertAll(vararg apiAuths: ApiAuth)

    @Delete
    fun delete(apiAuth: ApiAuth)
}