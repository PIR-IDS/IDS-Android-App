package fr.pirids.idsapp.data.model.dao

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.ApiAuth

@Dao
interface ApiAuthDao {
    @Query("SELECT * FROM api_auth")
    fun getAll(): List<ApiAuth>

    @Query("SELECT * FROM api_auth WHERE id = :id")
    fun get(id: Int): ApiAuth

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(apiAuth: ApiAuth) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg apiAuths: ApiAuth) : List<Long>

    @Delete
    fun delete(apiAuth: ApiAuth)
}