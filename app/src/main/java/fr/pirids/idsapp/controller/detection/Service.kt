package fr.pirids.idsapp.controller.detection

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.data.api.auth.IzlyAuth
import fr.pirids.idsapp.data.api.data.ApiData
import fr.pirids.idsapp.data.api.data.IzlyData
import fr.pirids.idsapp.data.api.service.ApiServiceIDS
import fr.pirids.idsapp.data.items.Service
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import fr.pirids.idsapp.data.model.entity.service.ApiAuth as ApiAuthEntity
import fr.pirids.idsapp.data.model.entity.service.IzlyAuth as IzlyAuthEntity

object Service {
    val monitoredServices : MutableState<Set<ApiServiceIDS>> = mutableStateOf(setOf())
    val connectedServices : MutableState<Set<ApiServiceIDS>> = mutableStateOf(setOf())
    val knownServices : MutableState<Set<ApiServiceIDS>> = mutableStateOf(setOf())

    fun getServiceItemFromApiService(apiService: ApiServiceIDS): Service? = Service.list.find { it.id == apiService.serviceId }
    fun getKnownApiServiceFromServiceItem(service: Service): ApiServiceIDS? = knownServices.value.find { it.serviceId == service.id }

    suspend fun checkService(username: String, password: String, service: Service, updateDatabase: Boolean = true) : Boolean = getServiceAndStatus(username, password, service, updateDatabase).second

    /**
     * @return Pair<ApiServiceIDS, Boolean> the instance of the connected service if success and the status of the service connection
     */
    suspend fun getServiceAndStatus(username: String, password: String, service: Service, updateDatabase: Boolean = true) : Pair<ApiServiceIDS, Boolean> {
        val (serviceData, serviceConnected) = connectToService(username, password, service, updateDatabase)
        return Pair(serviceData, serviceConnected)
    }

    private suspend fun connectToService(username: String, password: String, service: Service, updateDatabase: Boolean = true): Pair<ApiServiceIDS, Boolean> {
        when(service.id) {
            ServiceId.IZLY -> {
                val idsService = getKnownApiServiceFromServiceItem(service)
                    ?: ApiServiceIDS(service.id, data = mutableStateOf(
                        getServiceDataFromDatabase(service.id)
                            ?: IzlyData(setOf())
                        )
                    )
                if(!updateDatabase) {
                    addToKnownServices(idsService)
                }

                idsService.api = idsService.api?.let {
                    it.authenticate(IzlyAuth(username, password))
                    it
                } ?: IzlyApi(IzlyAuth(username, password))
                val serviceConnected = idsService.api!!.checkConnection()

                if(serviceConnected) {
                    // Add to lists
                    addToKnownServices(idsService)
                    addToConnectedServices(idsService)

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
                return Pair(idsService, serviceConnected)
            }
            else -> throw Exception("Service not supported")
        }
    }

    suspend fun getServiceDataFromDatabase(serviceId: ServiceId) : ApiData? {
        try {
            val apiData = AppDatabase.getInstance().apiDataDao().getAllFromType(
                AppDatabase.getInstance().serviceTypeDao().getByName(serviceId.tag).id
            )
            return when(serviceId) {
                ServiceId.IZLY -> {
                    val dataSet = mutableSetOf<Long>()
                    apiData.forEach {
                        dataSet.add(AppDatabase.getInstance().izlyDataDao().getFromApi(it.id).timestamp)
                    }
                    IzlyData(dataSet.toSet())
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("Service", "Error while getting service data from database")
        }
        return null
    }

    suspend fun getServiceData(apiInterface: ApiServiceIDS): ApiData? = try { apiInterface.api?.getData() } catch (e: Exception) { null }

    //TODO: create a Service comparator with service id and use it there, like we did for Devices
    fun getNotAddedCompatibleServices() : List<Service> = Service.list.filter { serv -> knownServices.value.find { it.serviceId == serv.id } == null }

    /**
     * This should guarantee that the service is unique in the set and update it if it's already connected
     */
    private fun addToServicesList(apiInterface: ApiServiceIDS, list: MutableState<Set<ApiServiceIDS>>) {
        list.value.forEach {
            if (it.serviceId == apiInterface.serviceId) {
                list.value = list.value.minus(it).plus(apiInterface)
                return@addToServicesList
            }
        }

        if(apiInterface in list.value) return

        list.value = list.value.plus(apiInterface)
    }

    private fun addToConnectedServices(apiInterface: ApiServiceIDS) = addToServicesList(apiInterface, connectedServices)
    private fun addToKnownServices(apiInterface: ApiServiceIDS) = addToServicesList(apiInterface, knownServices)
    fun addToMonitoredServices(apiInterface: ApiServiceIDS) = addToServicesList(apiInterface, monitoredServices)

    fun removeFromConnectedServices(apiInterface: ApiServiceIDS) {
        connectedServices.value = connectedServices.value.minus(apiInterface)
        if(apiInterface in monitoredServices.value) monitoredServices.value = monitoredServices.value.minus(apiInterface)
    }
    fun removeAllConnectedServices() {
        connectedServices.value = setOf()
        monitoredServices.value = setOf()
    }
}