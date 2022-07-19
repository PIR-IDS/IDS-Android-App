package fr.pirids.idsapp.data.model.dao

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.ApiData

@Dao
interface ApiDataDao {
    @Query("SELECT * FROM api_data")
    fun getAll(): List<ApiData>

    @Query("SELECT * FROM api_data WHERE id = :id")
    fun get(id: Int): ApiData

    @Insert
    fun insert(apiData: ApiData) : Long

    @Insert
    fun insertAll(vararg apiDatas: ApiData) : List<Long>

    @Delete
    fun delete(apiData: ApiData)
}