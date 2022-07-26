package fr.pirids.idsapp.controller.view.menus

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavHostController
import fr.pirids.idsapp.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object SettingsViewController {
    val fullScreenState = mutableStateOf(UserPreferencesRepository.DEFAULT_FULLSCREEN_NOTIFICATION)

    fun initPreferences(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            updateFullScreenState(context)
        }
    }

    fun goBack(navController: NavHostController) = navController.popBackStack()

    suspend fun toggleFullScreenAlert(context: Context, value: Boolean) {
        UserPreferencesRepository(context).updateFullscreenNotification(value)
    }

    private suspend fun updateFullScreenState(context: Context) {
        try {
            val value = UserPreferencesRepository(context).userPreferencesFlow.first().fullscreenNotification
            withContext(Dispatchers.Main) {
                fullScreenState.value = value
            }
        } catch (e: NoSuchElementException) { }
    }
}