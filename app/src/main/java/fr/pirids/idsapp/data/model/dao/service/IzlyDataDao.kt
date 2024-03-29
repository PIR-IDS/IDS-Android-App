package fr.pirids.idsapp.data.model.dao.service

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.service.IzlyData

@Dao
interface IzlyDataDao {
    @Query("SELECT * FROM izly_data")
    fun getAll(): List<IzlyData>

    @Query("SELECT * FROM izly_data WHERE id = :id")
    fun get(id: Int): IzlyData

    @Query("SELECT * FROM izly_data WHERE api_id = :api_id")
    fun getFromApi(api_id: Int): IzlyData

    @Query("SELECT * FROM izly_data WHERE timestamp = :timestamp ORDER BY id DESC LIMIT 1")
    fun getByTimestamp(timestamp: Long): IzlyData?

    @Insert
    fun insert(izlyData: IzlyData) : Long

    @Insert
    fun insertAll(vararg izlyDatas: IzlyData) : List<Long>

    @Delete
    fun delete(izlyData: IzlyData)
}