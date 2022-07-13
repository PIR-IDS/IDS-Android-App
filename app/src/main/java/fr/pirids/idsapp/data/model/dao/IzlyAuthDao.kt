package fr.pirids.idsapp.data.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.pirids.idsapp.data.model.entity.IzlyAuth

@Dao
interface IzlyAuthDao {
    @Query("SELECT * FROM izly_auth")
    fun getAll(): List<IzlyAuth>

    @Insert
    fun insertAll(vararg izlyAuths: IzlyAuth)

    @Delete
    fun delete(izlyAuth: IzlyAuth)
}