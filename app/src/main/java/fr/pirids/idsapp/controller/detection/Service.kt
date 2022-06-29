package fr.pirids.idsapp.controller.detection

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.data.api.auth.IzlyAuth
import fr.pirids.idsapp.data.api.data.ApiData
import fr.pirids.idsapp.data.items.Service
import fr.pirids.idsapp.data.items.ServiceId

object Service {
    val connectedServices : MutableState<Set<ApiInterface>> = mutableStateOf(setOf())

    fun getServiceItemFromApiService(apiService: ApiInterface): Service? = Service.list.find { it.id == apiService.serviceId }

    fun checkService(username: String, password: String, service: Service) : Boolean = getServiceAndStatus(username, password, service).second

    /**
     * @return Pair<ApiInterface, Boolean> the instance of the connected service if success and the status of the service connection
     */
    fun getServiceAndStatus(username: String, password: String, service: Service) : Pair<ApiInterface, Boolean> {
        val (serviceData, serviceConnected) = connectToService(username, password, service)
        return Pair(serviceData, serviceConnected)
    }

    private fun connectToService(username: String, password: String, service: Service): Pair<ApiInterface, Boolean> {
        when(service.id) {
            ServiceId.IZLY -> {
                val api = IzlyApi(IzlyAuth(username, password))
                val serviceConnected = api.checkConnection()
                if(serviceConnected) {
                    addToConnectedServices(api)
                }
                return Pair(api, serviceConnected)
            }
            else -> throw Exception("Service not supported")
        }
    }

    suspend fun getServiceData(apiInterface: ApiInterface): ApiData? = try { apiInterface.getData() } catch (e: Exception) { null }

    //TODO: use "known" service list instead of connected list
    //TODO: create a Service comparator with service id and use it there, like we did for Devices
    fun getNotAddedCompatibleServices() : List<Service> = Service.list.filter { serv -> connectedServices.value.find { it.serviceId == serv.id } == null }

    /**
     * This should guarantee that the connected service is unique in the set and update it if it's already connected
     */
    private fun addToConnectedServices(apiInterface: ApiInterface) {
        connectedServices.value.forEach {
            //FIXME: this is not working (always true)
            // Also maybe use something else than reflection to check if the same instance is already connected
            if (it::class === apiInterface::class) {
                connectedServices.value = connectedServices.value.minus(it).plus(apiInterface)
                return@addToConnectedServices
            }
        }
        connectedServices.value = connectedServices.value.plus(apiInterface)
    }
}