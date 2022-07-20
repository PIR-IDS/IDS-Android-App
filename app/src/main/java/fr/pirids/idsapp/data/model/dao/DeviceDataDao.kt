package fr.pirids.idsapp.data.model.dao

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.DeviceData

@Dao
interface DeviceDataDao {
    @Query("SELECT * FROM device_data")
    fun getAll(): List<DeviceData>

    @Query("SELECT * FROM device_data WHERE id = :id")
    fun get(id: Int): DeviceData

    @Query("SELECT * FROM device_data WHERE device_id = :device_id AND data_type_id = :data_type_id")
    fun getFromDeviceAndType(device_id: Int, data_type_id: Int): DeviceData

    @Insert
    fun insert(deviceData: DeviceData) : Long

    @Insert
    fun insertAll(vararg deviceDatas: DeviceData) : List<Long>

    @Delete
    fun delete(deviceData: DeviceData)
}