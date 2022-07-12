package fr.pirids.idsapp.controller.security

import android.content.Context
import fr.pirids.idsapp.data.preferences.AppEncryptedKeysRepository
import fr.pirids.idsapp.extensions.decodeHexToByteArray
import fr.pirids.idsapp.extensions.toHexString
import kotlinx.coroutines.flow.first
import net.sqlcipher.database.SupportFactory

object DatabaseCipherHandler {
    suspend fun encryptAndSaveDatabaseKey(context: Context, rawKey: String) =
        AppEncryptedKeysRepository(context).saveDatabaseEncryptedKey(rawKey)

    suspend fun getDatabaseRawKey(context: Context): String =
        AppEncryptedKeysRepository(context).appEncryptedKeysFlow.first().encryptedDatabaseKey ?: run {
            val rawKey = SecurityHandler.generateRandomKey().toHexString()
            encryptAndSaveDatabaseKey(context, rawKey)
            rawKey
        }

    fun getSupportFactory(dbKey: String) : SupportFactory = SupportFactory(dbKey.decodeHexToByteArray())
}