package fr.pirids.idsapp.data.model.dao

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.DeviceData

@Dao
interface DeviceDataDao {
    @Query("SELECT * FROM device_data")
    fun getAll(): List<DeviceData>

    @Query("SELECT * FROM device_data WHERE id = :id")
    fun get(id: Int): DeviceData

    @Insert
    fun insert(deviceData: DeviceData) : Long

    @Insert
    fun insertAll(vararg deviceDatas: DeviceData) : List<Long>

    @Delete
    fun delete(deviceData: DeviceData)
}