package fr.pirids.idsapp.data.model.dao.detection

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.pirids.idsapp.data.model.entity.detection.Detection

@Dao
interface DetectionDao {
    @Query("SELECT * FROM detection")
    fun getAll(): List<Detection>

    @Query("SELECT * FROM detection WHERE id = :id")
    fun get(id: Int): Detection

    @Query("SELECT * FROM detection WHERE api_data_id = :api_data_id AND timestamp = :timestamp")
    fun getFromApiDataIdAndTimestamp(api_data_id: Int, timestamp: Long): Detection

    @Insert
    fun insert(detection: Detection) : Long

    @Insert
    fun insertAll(vararg detections: Detection) : List<Long>

    @Delete
    fun delete(detection: Detection)
}