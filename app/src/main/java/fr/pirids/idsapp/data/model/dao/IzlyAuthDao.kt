package fr.pirids.idsapp.data.model.dao

import androidx.room.*
import fr.pirids.idsapp.data.model.entity.IzlyAuth

@Dao
interface IzlyAuthDao {
    @Query("SELECT * FROM izly_auth")
    fun getAll(): List<IzlyAuth>

    @Query("SELECT * FROM izly_auth WHERE id = :id")
    fun get(id: Int): IzlyAuth

    @Query("SELECT * FROM izly_auth WHERE api_id = :api_id")
    fun getFromApi(api_id: Int): IzlyAuth

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(izlyAuth: IzlyAuth) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg izlyAuths: IzlyAuth) : List<Long>

    @Delete
    fun delete(izlyAuth: IzlyAuth)
}