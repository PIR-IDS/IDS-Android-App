package fr.pirids.idsapp.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.pirids.idsapp.controller.security.DatabaseCipherHandler
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.device.data.WalletCardData as WalletCardDataItem
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
    DeviceDataType::class,
    IzlyAuth::class,
    IzlyData::class,
    ServiceType::class,
    WalletCardData::class,
], version = 3)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var INSTANCE: AppDatabase? = null
        private val mutex = Mutex()
        suspend fun initInstance(context: Context) = getInstance(context)
        suspend fun getInstance(context: Context? = null): AppDatabase {
            mutex.withLock {
                return withContext(Dispatchers.IO) {
                    var instance = INSTANCE

                    if (instance == null) {
                        context ?: throw IllegalStateException("Context is null")
                        instance = Room.databaseBuilder(context, AppDatabase::class.java,"encrypted_idsapp_db")
                            .openHelperFactory(DatabaseCipherHandler.getSupportFactory(DatabaseCipherHandler.getDatabaseRawKey(context)))
                            //TODO: remove the destructive migration when the database is stable and write migration code to migrate the database to the new version
                            .fallbackToDestructiveMigration()
                            .build()

                        INSTANCE = instance

                        // Prepopulate database with data types (will be ignored if already present)
                        prepopulateDatabase()
                    }
                    return@withContext instance
                }
            }
        }

        private fun prepopulateDatabase() {
            INSTANCE?.deviceDataTypeDao()?.insertAll(
                DeviceDataType(dataName = WalletCardDataItem.tag),
            )
            INSTANCE?.serviceTypeDao()?.insertAll(
                ServiceType(serviceName = ServiceId.IZLY.tag),
            )
        }
    }

    abstract fun apiAuthDao(): ApiAuthDao
    abstract fun apiDataDao(): ApiDataDao
    abstract fun deviceDao(): DeviceDao
    abstract fun deviceDataDao(): DeviceDataDao
    abstract fun deviceDataTypeDao(): DeviceDataTypeDao
    abstract fun izlyAuthDao(): IzlyAuthDao
    abstract fun izlyDataDao(): IzlyDataDao
    abstract fun serviceTypeDao(): ServiceTypeDao
    abstract fun walletCardDataDao(): WalletCardDataDao
}