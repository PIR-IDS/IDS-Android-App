package fr.pirids.idsapp.controller.daemon

import android.util.Log
import fr.pirids.idsapp.controller.detection.Service
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.model.AppDatabase
import fr.pirids.idsapp.data.model.entity.ApiAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import fr.pirids.idsapp.data.items.Service.Companion as ServiceItem

object ServiceDaemon {
    private const val checkingDelayMinutes = 5
    private const val checkingDelayMillis = 15_000L

    fun connectToServices() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getInstance().apiAuthDao().getAll().forEach {
                    connectToService(it)
                }
            } catch (e: Exception) {
                Log.e("ServiceDaemon", "Error while getting all services")
            }
        }
    }

    private suspend fun connectToService(apiAuth: ApiAuth) {
        try {
            when(AppDatabase.getInstance().serviceTypeDao().get(apiAuth.serviceId).serviceName) {
                ServiceId.IZLY.tag -> {
                    val izlyAuth = AppDatabase.getInstance().izlyAuthDao().getFromApi(apiAuth.id)

                    //FIXME: we should add the known services EVEN IF the user is not YET connected to the service
                    // so we have to refactor the addToKnownServices method with another parameter which doesn't depends on
                    // ApiInterface as well as the knownServices set ; maybe create a wrapper like we did with BluetoothDeviceIDS?

                    // The checkService function calls the connectToService function
                    Service.checkService(izlyAuth.identifier, izlyAuth.password, ServiceItem.get(ServiceId.IZLY), false)
                }
                else -> {}
            }
        } catch (e: Exception) {
            Log.e("ServiceDaemon", "Error while connecting to service")
        }
    }

    suspend fun handleServiceStatus() : Nothing {
        while(true) {
            delay(checkingDelayMinutes.minutes)
            Service.connectedServices.value.forEach {
                try {
                    if(!it.checkConnection()) {
                        // If the reconnection failed, we disconnect the service
                        Service.removeFromConnectedServices(it)
                        // Try to reconnect (maybe the token has expired)
                        connectToService(
                            AppDatabase.getInstance().apiAuthDao().get(
                                AppDatabase.getInstance().serviceTypeDao().getByName(it.serviceId.tag).id
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ServiceDaemon", "Error while checking service status", e)
                }
            }
        }
    }

    suspend fun handleDisconnectedKnownServices() : Nothing {
        while(true) {
            delay(checkingDelayMillis)
            reconnectToDisconnectedKnownServices()
        }
    }

    private suspend fun reconnectToDisconnectedKnownServices() {
        Service.knownServices.value.filter { it !in Service.connectedServices.value }.forEach {
            connectToService(
                AppDatabase.getInstance().apiAuthDao().get(
                    AppDatabase.getInstance().serviceTypeDao().getByName(it.serviceId.tag).id
                )
            )
        }
    }

    fun clearAllConnectedServices() {
        CoroutineScope(Dispatchers.IO).launch { Service.removeAllConnectedServices() }
    }
}