package fr.pirids.idsapp.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.pirids.idsapp.controller.security.DatabaseCipherHandler
import fr.pirids.idsapp.data.model.dao.*
import fr.pirids.idsapp.data.model.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Database(entities = [
    ApiAuth::class,
    ApiData::class,
    Device::class,
    DeviceData::class,
    IzlyAuth::class,
    IzlyData::class,
    WalletCardData::class,
], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var INSTANCE: AppDatabase? = null
        private val mutex = Mutex()
        suspend fun getInstance(context: Context): AppDatabase {
            mutex.withLock {
                return withContext(Dispatchers.IO) {
                    var instance = INSTANCE

                    if (instance == null) {
                        instance = Room.databaseBuilder(context, AppDatabase::class.java,"encrypted_idsapp_db")
                            .openHelperFactory(DatabaseCipherHandler.getSupportFactory(DatabaseCipherHandler.getDatabaseRawKey(context)))
                            //.fallbackToDestructiveMigration()
                            .build()

                        INSTANCE = instance
                    }
                    return@withContext instance
                }
            }
        }
    }

    abstract fun apiAuthDao(): ApiAuthDao
    abstract fun apiDataDao(): ApiDataDao
    abstract fun deviceDao(): DeviceDao
    abstract fun deviceDataDao(): DeviceDataDao
    abstract fun izlyAuthDao(): IzlyAuthDao
    abstract fun izlyDataDao(): IzlyDataDao
    abstract fun walletCardDataDao(): WalletCardDataDao
}