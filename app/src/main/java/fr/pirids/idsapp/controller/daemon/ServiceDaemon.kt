package fr.pirids.idsapp.controller.daemon

import android.util.Log
import fr.pirids.idsapp.controller.detection.Service
import fr.pirids.idsapp.data.api.data.ApiData
import fr.pirids.idsapp.data.api.data.IzlyData
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.model.AppDatabase
import fr.pirids.idsapp.data.model.entity.service.ApiAuth
import fr.pirids.idsapp.data.model.entity.service.ApiData as ApiDataEntity
import fr.pirids.idsapp.data.model.entity.service.IzlyData as IzlyDataEntity
import kotlinx.coroutines.*
import fr.pirids.idsapp.data.items.Service as ServiceItem

object ServiceDaemon {
    const val CHECKING_DELAY_MILLIS = 60_000L

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

    private suspend fun connectToService(apiAuth: ApiAuth) : Boolean {
        return try {
            when(AppDatabase.getInstance().serviceTypeDao().get(apiAuth.serviceId).serviceName) {
                ServiceId.IZLY.tag -> {
                    val izlyAuth = AppDatabase.getInstance().izlyAuthDao().getFromApi(apiAuth.id)
                    // The checkService function calls the connectToService function
                    Service.checkService(izlyAuth.identifier, izlyAuth.password, ServiceItem.get(ServiceId.IZLY), false)
                }
                else -> { false }
            }
        } catch (e: Exception) {
            Log.e("ServiceDaemon", "Error while connecting to service")
            false
        }
    }

    suspend fun handleServiceStatus() : Nothing {
        while (true) {
            delay(CHECKING_DELAY_MILLIS)
            Service.connectedServices.value.forEach {
                try {
                    val api = it.api
                    var apiConnected = true
                    if(api?.checkConnection() != true) {
                        apiConnected = false
                        // If the reconnection failed, we disconnect the service
                        Service.removeFromConnectedServices(it)
                        if(api != null) {
                            // Try to reconnect (maybe the token has expired)
                            apiConnected = connectToService(
                                AppDatabase.getInstance().apiAuthDao().get(
                                    AppDatabase.getInstance().serviceTypeDao().getByName(api.serviceId.tag).id
                                )
                            )
                        }
                    }

                    if(apiConnected && api != null) {
                        // If we are still connected, we update the service data
                        it.data.value = try {
                            getMergedRemoteAndLocalData(api.getData(), it.data.value, serviceId = api.serviceId)
                        } catch (e: Exception) {
                            Log.e("ServiceDaemon", "Error while updating service data", e)
                            it.data.value
                        }

                        // Save the data in the database
                        try {
                            when(api.serviceId) {
                                ServiceId.IZLY -> {
                                    val serviceId = AppDatabase.getInstance().serviceTypeDao().getByName(api.serviceId.tag).id
                                    //TODO: improve this logic, maybe by including timestamp in ApiData, we will always have it anyway...
                                    // Also the timestamp probably will be unique for EACH service data, so find a way to handle all of that correctly...
                                    // Right now this is a bit overkill to check for EACH timestamp EVERY 15 seconds, we have to improve this
                                    (it.data.value as IzlyData).transactionList.forEach { timestamp ->
                                        try {
                                            // If the transaction is already in the database, we don't want to insert it again
                                            AppDatabase.getInstance().izlyDataDao().getByTimestamp(timestamp)
                                                ?: AppDatabase.getInstance().izlyDataDao().insert(
                                                    IzlyDataEntity(
                                                        apiId = AppDatabase.getInstance().apiDataDao().insert(ApiDataEntity(serviceId = serviceId)).toInt(),
                                                        timestamp = timestamp,
                                                        amount = null,
                                                        localization = null
                                                    )
                                            )
                                        } catch (e: Exception) {
                                            Log.e("ServiceDaemon", "Error while saving Izly data", e)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ServiceDaemon", "Error while saving service data", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ServiceDaemon", "Error while checking service status", e)
                }
            }
        }
    }

    private fun getMergedRemoteAndLocalData(remoteData: ApiData?, localData: ApiData, serviceId: ServiceId) : ApiData {
        return when(serviceId) {
            ServiceId.IZLY -> {
                IzlyData(
                    (localData as IzlyData).transactionList.plus(
                        (remoteData as IzlyData?)?.transactionList ?: emptySet()
                    )
                )
            }
            else -> { localData }
        }
    }

    suspend fun handleDisconnectedKnownServices() : Nothing {
        while (true) {
            delay(CHECKING_DELAY_MILLIS)
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