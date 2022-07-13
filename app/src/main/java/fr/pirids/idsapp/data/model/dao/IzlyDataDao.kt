package fr.pirids.idsapp.data.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.pirids.idsapp.data.model.entity.IzlyData

@Dao
interface IzlyDataDao {
    @Query("SELECT * FROM izly_data")
    fun getAll(): List<IzlyData>

    @Insert
    fun insertAll(vararg izlyDatas: IzlyData)

    @Delete
    fun delete(izlyData: IzlyData)
}