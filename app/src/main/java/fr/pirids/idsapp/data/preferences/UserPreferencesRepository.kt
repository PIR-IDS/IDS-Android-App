package fr.pirids.idsapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {
    companion object {
        const val DEFAULT_FULLSCREEN_NOTIFICATION = true
    }

    private object PreferencesKeys {
        val fullscreenNotification = booleanPreferencesKey("fullscreen_notification")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            // Get our show completed value, defaulting to DEFAULT_FULLSCREEN_NOTIFICATION if not set:
            val fullscreenNotification = preferences[PreferencesKeys.fullscreenNotification] ?: DEFAULT_FULLSCREEN_NOTIFICATION
            UserPreferences(fullscreenNotification)
        }

    suspend fun updateFullscreenNotification(fullscreenNotification: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.fullscreenNotification] = fullscreenNotification
        }
    }
}
