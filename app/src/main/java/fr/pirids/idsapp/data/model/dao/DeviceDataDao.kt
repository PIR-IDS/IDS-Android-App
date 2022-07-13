package fr.pirids.idsapp.data.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.pirids.idsapp.data.model.entity.DeviceData

@Dao
interface DeviceDataDao {
    @Query("SELECT * FROM device_data")
    fun getAll(): List<DeviceData>

    @Insert
    fun insertAll(vararg deviceDatas: DeviceData)

    @Delete
    fun delete(deviceData: DeviceData)
}