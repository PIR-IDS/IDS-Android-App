package fr.pirids.idsapp.data.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.pirids.idsapp.data.model.entity.WalletCardData

@Dao
interface WalletCardDataDao {
    @Query("SELECT * FROM wallet_card_data")
    fun getAll(): List<WalletCardData>

    @Insert
    fun insertAll(vararg walletCardDatas: WalletCardData)

    @Delete
    fun delete(walletCardData: WalletCardData)
}