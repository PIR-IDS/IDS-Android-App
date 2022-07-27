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

    @Insert
    fun insert(detection: Detection) : Long

    @Insert
    fun insertAll(vararg detections: Detection) : List<Long>

    @Delete
    fun delete(detection: Detection)
}