package fr.pirids.idsapp.data.model.dao.service

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.service.ApiData

@Dao
interface ApiDataDao {
    @Query("SELECT * FROM api_data")
    fun getAll(): List<ApiData>

    @Query("SELECT * FROM api_data WHERE id = :id")
    fun get(id: Int): ApiData

    @Query("SELECT * FROM api_data WHERE service_id = :serviceId")
    fun getAllFromType(serviceId: Int): List<ApiData>

    @Insert
    fun insert(apiData: ApiData) : Long

    @Insert
    fun insertAll(vararg apiDatas: ApiData) : List<Long>

    @Delete
    fun delete(apiData: ApiData)
}