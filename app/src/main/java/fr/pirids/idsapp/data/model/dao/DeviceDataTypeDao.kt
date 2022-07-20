package fr.pirids.idsapp.data.model.dao

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.DeviceDataType

@Dao
interface DeviceDataTypeDao {
    @Query("SELECT * FROM device_data_type")
    fun getAll(): List<DeviceDataType>

    @Query("SELECT * FROM device_data_type WHERE id = :id")
    fun get(id: Int): DeviceDataType

    @Query("SELECT * FROM device_data_type WHERE data_name = :name")
    fun getByName(name: String): DeviceDataType

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(deviceDataType: DeviceDataType) : Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg deviceDataTypes: DeviceDataType) : List<Long>

    @Delete
    fun delete(deviceDataType: DeviceDataType)
}