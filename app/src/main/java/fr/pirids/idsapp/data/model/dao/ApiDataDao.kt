package fr.pirids.idsapp.data.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.pirids.idsapp.data.model.entity.ApiData

@Dao
interface ApiDataDao {
    @Query("SELECT * FROM api_data")
    fun getAll(): List<ApiData>

    @Insert
    fun insertAll(vararg apiDatas: ApiData)

    @Delete
    fun delete(apiData: ApiData)
}