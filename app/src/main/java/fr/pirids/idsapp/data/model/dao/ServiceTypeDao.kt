package fr.pirids.idsapp.data.model.dao

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.ServiceType

@Dao
interface ServiceTypeDao {
    @Query("SELECT * FROM service_type")
    fun getAll(): List<ServiceType>

    @Query("SELECT * FROM service_type WHERE id = :id")
    fun get(id: Int): ServiceType

    @Query("SELECT * FROM service_type WHERE service_name = :name")
    fun getByName(name: String): ServiceType

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(serviceType: ServiceType) : Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg serviceTypes: ServiceType) : List<Long>

    @Delete
    fun delete(serviceType: ServiceType)
}