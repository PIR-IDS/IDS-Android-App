package fr.pirids.idsapp.data.model.dao

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.Device

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device")
    fun getAll(): List<Device>

    @Query("SELECT * FROM device WHERE id = :id")
    fun get(id: Int): Device

    @Query("SELECT * FROM device WHERE address = :address")
    fun getFromAddress(address: String): Device

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(device: Device) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg devices: Device) : List<Long>

    @Delete
    fun delete(device: Device)
}