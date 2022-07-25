package fr.pirids.idsapp.controller.view.service

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavHostController
import fr.pirids.idsapp.data.items.Device
import fr.pirids.idsapp.data.items.Service
import fr.pirids.idsapp.data.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import fr.pirids.idsapp.controller.detection.Service as ServiceController

object ServiceViewController {
    val isLoading : MutableState<Boolean> = mutableStateOf(false)
    lateinit var serviceScope: CoroutineScope
    lateinit var historyScope: CoroutineScope

    fun getProbesList(service: Service): List<Device> = service.compatibleDevices

    fun closeModal(navController: NavHostController) = navController.popBackStack()

    fun onLoginAction(
        focusManager: FocusManager,
        service: Service,
        snackbarHostState: SnackbarHostState,
        username: String,
        password: String,
        notFoundText: String
    ) {
        focusManager.clearFocus()
        isLoading.value = true
        serviceScope.launch(Dispatchers.IO) {
            try {
                val serviceConnected = ServiceController.checkService(
                    username,
                    password,
                    service
                )
                if (!serviceConnected) {
                    serviceScope.launch(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(notFoundText)
                    }
                }
            } catch (e: Exception) {
                Log.e("ServiceViewController", "Error while trying to login", e)
                serviceScope.launch(Dispatchers.Main) {
                    snackbarHostState.showSnackbar(notFoundText)
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    fun disconnectService(service: Service) {
        ServiceController.getKnownApiServiceFromServiceItem(service)?.let {
            ServiceController.knownServices.value = ServiceController.knownServices.value.minus(it)
            if(it in ServiceController.connectedServices.value) {
                ServiceController.removeFromConnectedServices(it)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getInstance().apiAuthDao().delete(
                    AppDatabase.getInstance().serviceTypeDao().getApiAuthByServiceType(
                        AppDatabase.getInstance().serviceTypeDao().getByName(service.id.tag).id
                    )
                )
            } catch (e: Exception) {
                Log.e("ServiceViewController", "Error while deleting service", e)
            }
        }
    }
}