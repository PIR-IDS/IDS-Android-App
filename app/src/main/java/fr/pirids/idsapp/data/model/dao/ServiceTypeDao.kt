package fr.pirids.idsapp.data.model.dao

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.ApiAuth
import fr.pirids.idsapp.data.model.entity.ServiceType

@Dao
interface ServiceTypeDao {
    @Query("SELECT * FROM service_type")
    fun getAll(): List<ServiceType>

    @Query("SELECT * FROM service_type WHERE id = :id")
    fun get(id: Int): ServiceType

    @Query("SELECT * FROM service_type WHERE service_name = :name")
    fun getByName(name: String): ServiceType

    @Query("SELECT api_auth.* FROM service_type, api_auth WHERE service_type.id = :service_id AND api_auth.service_id = service_type.id")
    fun getApiAuthByServiceType(service_id: Int): ApiAuth

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(serviceType: ServiceType) : Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg serviceTypes: ServiceType) : List<Long>

    @Delete
    fun delete(serviceType: ServiceType)
}