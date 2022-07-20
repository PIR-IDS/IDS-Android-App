package fr.pirids.idsapp.controller.detection

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.data.api.auth.IzlyAuth
import fr.pirids.idsapp.data.api.data.ApiData
import fr.pirids.idsapp.data.items.Service
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import fr.pirids.idsapp.data.model.entity.ApiAuth as ApiAuthEntity
import fr.pirids.idsapp.data.model.entity.IzlyAuth as IzlyAuthEntity

object Service {
    val connectedServices : MutableState<Set<ApiInterface>> = mutableStateOf(setOf())
    val knownServices : MutableState<Set<ApiInterface>> = mutableStateOf(setOf())

    fun getServiceItemFromApiService(apiService: ApiInterface): Service? = Service.list.find { it.id == apiService.serviceId }

    suspend fun checkService(username: String, password: String, service: Service, updateDatabase: Boolean = true) : Boolean = getServiceAndStatus(username, password, service, updateDatabase).second

    /**
     * @return Pair<ApiInterface, Boolean> the instance of the connected service if success and the status of the service connection
     */
    suspend fun getServiceAndStatus(username: String, password: String, service: Service, updateDatabase: Boolean = true) : Pair<ApiInterface, Boolean> {
        val (serviceData, serviceConnected) = connectToService(username, password, service, updateDatabase)
        return Pair(serviceData, serviceConnected)
    }

    private suspend fun connectToService(username: String, password: String, service: Service, updateDatabase: Boolean = true): Pair<ApiInterface, Boolean> {
        when(service.id) {
            ServiceId.IZLY -> {
                val api = IzlyApi(IzlyAuth(username, password))
                val serviceConnected = api.checkConnection()

                if(serviceConnected) {
                    // Add to lists
                    addToKnownServices(api)
                    addToConnectedServices(api)

                    if(updateDatabase) {
                        // Save in database
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val apiAuthId = AppDatabase.getInstance().apiAuthDao().insert(
                                    ApiAuthEntity(
                                        serviceId = AppDatabase.getInstance().serviceTypeDao().getByName(ServiceId.IZLY.tag).id
                                    )
                                )
                                AppDatabase.getInstance().izlyAuthDao().insert(
                                    IzlyAuthEntity(
                                        apiId = apiAuthId.toInt(),
                                        identifier = username,
                                        password = password
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("Service", "Error while saving credentials in database: $e")
                            }
                        }
                    }
                }
                return Pair(api, serviceConnected)
            }
            else -> throw Exception("Service not supported")
        }
    }

    suspend fun getServiceData(apiInterface: ApiInterface): ApiData? = try { apiInterface.getData() } catch (e: Exception) { null }

    //TODO: create a Service comparator with service id and use it there, like we did for Devices
    fun getNotAddedCompatibleServices() : List<Service> = Service.list.filter { serv -> knownServices.value.find { it.serviceId == serv.id } == null }

    /**
     * This should guarantee that the service is unique in the set and update it if it's already connected
     */
    private fun addToServicesList(apiInterface: ApiInterface, list: MutableState<Set<ApiInterface>>) {
        list.value.forEach {
            //FIXME: this is not working (always true)
            // Also maybe use something else than reflection to check if the same instance is already connected
            if (it::class === apiInterface::class) {
                list.value = list.value.minus(it).plus(apiInterface)
                return@addToServicesList
            }
        }
        list.value = list.value.plus(apiInterface)
    }

    private fun addToConnectedServices(apiInterface: ApiInterface) = addToServicesList(apiInterface, connectedServices)
    private fun addToKnownServices(apiInterface: ApiInterface) = addToServicesList(apiInterface, knownServices)
}