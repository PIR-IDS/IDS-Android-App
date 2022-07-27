package fr.pirids.idsapp.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.pirids.idsapp.controller.security.DatabaseCipherHandler
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.device.data.WalletCardData as WalletCardDataItem
import fr.pirids.idsapp.data.model.dao.detection.DetectionDao
import fr.pirids.idsapp.data.model.dao.detection.DetectionDeviceDao
import fr.pirids.idsapp.data.model.dao.device.DeviceDao
import fr.pirids.idsapp.data.model.dao.device.DeviceDataDao
import fr.pirids.idsapp.data.model.dao.device.DeviceDataTypeDao
import fr.pirids.idsapp.data.model.dao.device.WalletCardDataDao
import fr.pirids.idsapp.data.model.dao.service.*
import fr.pirids.idsapp.data.model.entity.detection.Detection
import fr.pirids.idsapp.data.model.entity.detection.DetectionDevice
import fr.pirids.idsapp.data.model.entity.device.Device
import fr.pirids.idsapp.data.model.entity.device.DeviceData
import fr.pirids.idsapp.data.model.entity.device.DeviceDataType
import fr.pirids.idsapp.data.model.entity.device.WalletCardData
import fr.pirids.idsapp.data.model.entity.service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Database(entities = [
    ApiAuth::class,
    ApiData::class,
    Detection::class,
    DetectionDevice::class,
    Device::class,
    DeviceData::class,
    DeviceDataType::class,
    IzlyAuth::class,
    IzlyData::class,
    ServiceType::class,
    WalletCardData::class,
], version = 5)
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
    abstract fun detectionDao(): DetectionDao
    abstract fun detectionDeviceDao(): DetectionDeviceDao
    abstract fun deviceDao(): DeviceDao
    abstract fun deviceDataDao(): DeviceDataDao
    abstract fun deviceDataTypeDao(): DeviceDataTypeDao
    abstract fun izlyAuthDao(): IzlyAuthDao
    abstract fun izlyDataDao(): IzlyDataDao
    abstract fun serviceTypeDao(): ServiceTypeDao
    abstract fun walletCardDataDao(): WalletCardDataDao
}