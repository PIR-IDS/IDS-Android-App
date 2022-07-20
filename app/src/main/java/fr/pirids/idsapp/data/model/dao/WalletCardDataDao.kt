package fr.pirids.idsapp.data.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.pirids.idsapp.data.model.entity.DeviceData
import fr.pirids.idsapp.data.model.entity.WalletCardData

@Dao
interface WalletCardDataDao {
    @Query("SELECT * FROM wallet_card_data")
    fun getAll(): List<WalletCardData>

    @Query("SELECT * FROM wallet_card_data WHERE id = :id")
    fun get(id: Int): WalletCardData

    @Query("SELECT * FROM wallet_card_data WHERE device_data_id = :device_data_id")
    fun getAllFromDeviceData(device_data_id: Int): List<WalletCardData>

    @Insert
    fun insert(walletCardData: WalletCardData) : Long

    @Insert
    fun insertAll(vararg walletCardDatas: WalletCardData) : List<Long>

    @Delete
    fun delete(walletCardData: WalletCardData)
}