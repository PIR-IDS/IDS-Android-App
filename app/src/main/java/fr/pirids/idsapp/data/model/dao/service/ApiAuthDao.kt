package fr.pirids.idsapp.data.model.dao.service

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.service.ApiAuth

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