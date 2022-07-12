package fr.pirids.idsapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import fr.pirids.idsapp.controller.security.hack.encryptedPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

//TODO: replace this with a real data store when official encrypted DataStore is available
private val Context.dataStore by encryptedPreferencesDataStore(name = "app_encrypted_keys")

class AppEncryptedKeysRepository(private val context: Context) {
    private object PreferencesKeys {
        val databaseEncryptedKey = stringPreferencesKey("database_encrypted_key")
    }

    val appEncryptedKeysFlow: Flow<AppEncryptedKeys> = context.dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            // Get our show completed value, defaulting to null if not set:
            val databaseEncryptedKey = preferences[PreferencesKeys.databaseEncryptedKey]
            AppEncryptedKeys(databaseEncryptedKey)
        }

    suspend fun saveDatabaseEncryptedKey(databaseEncryptedKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.databaseEncryptedKey] = databaseEncryptedKey
        }
    }
}
