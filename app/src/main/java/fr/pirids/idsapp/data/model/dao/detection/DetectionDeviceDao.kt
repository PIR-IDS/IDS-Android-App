package fr.pirids.idsapp.data.model.dao.detection

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.pirids.idsapp.data.model.entity.detection.DetectionDevice

@Dao
interface DetectionDeviceDao {
    @Query("SELECT * FROM detection_device")
    fun getAll(): List<DetectionDevice>

    @Query("SELECT * FROM detection_device WHERE id = :id")
    fun get(id: Int): DetectionDevice

    @Query("SELECT * FROM detection_device WHERE detection_id = :detection_id")
    fun getAllFromDetectionId(detection_id: Int): List<DetectionDevice>

    @Insert
    fun insert(detectionDevice: DetectionDevice) : Long

    @Insert
    fun insertAll(vararg detectionDevices: DetectionDevice) : List<Long>

    @Delete
    fun delete(detectionDevice: DetectionDevice)
}