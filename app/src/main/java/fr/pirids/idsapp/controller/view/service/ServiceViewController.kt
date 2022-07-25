package fr.pirids.idsapp.controller.view.service

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavHostController
import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.data.api.data.IzlyData
import fr.pirids.idsapp.data.items.Device
import fr.pirids.idsapp.data.items.Service
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import fr.pirids.idsapp.controller.detection.Service as ServiceController

object ServiceViewController {
    private const val checkingDelayMillis = 10_000L

    val isLoading : MutableState<Boolean> = mutableStateOf(false)
    val serviceHistory: MutableState<List<String>> = mutableStateOf(listOf())
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
                val (serviceData, serviceConnected) = ServiceController.getServiceAndStatus(
                    username,
                    password,
                    service
                )
                if (serviceConnected) {
                    historyScope.launch(Dispatchers.IO) {
                        updateServiceData(serviceData, service)
                    }
                } else {
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

    private suspend fun updateServiceData(serviceData: ApiInterface, service: Service) : Nothing {
        while(true) {
            ServiceController.getServiceData(serviceData)?.let {
                when(service.id) {
                    ServiceId.IZLY -> {
                        val historyList: MutableList<String> = mutableListOf()
                        (it as IzlyData).transactionList.forEach { timestamp ->
                            historyList.add((if(timestamp > 0) "+" else "-") + " : $timestamp")
                        }
                        serviceHistory.value = historyList
                    }
                }
            }
            delay(checkingDelayMillis)
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