package fr.pirids.idsapp.data.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.pirids.idsapp.data.model.entity.Device

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device")
    fun getAll(): List<Device>

    @Insert
    fun insertAll(vararg devices: Device)

    @Delete
    fun delete(device: Device)
}