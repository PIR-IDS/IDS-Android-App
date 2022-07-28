package fr.pirids.idsapp.data.model.dao.device

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.device.Device

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device")
    fun getAll(): List<Device>

    @Query("SELECT * FROM device WHERE id = :id")
    fun get(id: Int): Device

    @Query("SELECT * FROM device WHERE address = :address")
    fun getFromAddress(address: String): Device?

    @Update
    fun update(device: Device)

    @Insert
    fun insert(device: Device) : Long

    @Insert
    fun insertAll(vararg devices: Device) : List<Long>

    @Delete
    fun delete(device: Device)
}