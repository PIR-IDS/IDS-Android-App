package fr.pirids.idsapp.controller.detection

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.model.api.auth.IzlyAuth
import fr.pirids.idsapp.model.api.data.ApiData
import fr.pirids.idsapp.model.items.Service
import fr.pirids.idsapp.model.items.ServiceId

object Service {
    val connectedServices : MutableState<Set<ApiInterface>> = mutableStateOf(setOf())

    fun checkService(username: String, password: String, service: Service) : Boolean = getServiceAndStatus(username, password, service).second

    /**
     * @return Pair<ApiInterface, Boolean> the instance of the connected service if success and the status of the service connection
     */
    fun getServiceAndStatus(username: String, password: String, service: Service) : Pair<ApiInterface, Boolean> {
        val serviceData = connectToService(username, password, service)
        return Pair(serviceData, serviceData.checkConnection())
    }

    private fun connectToService(username: String, password: String, service: Service): ApiInterface {
        when(service.id) {
            ServiceId.IZLY -> {
                val api = IzlyApi(IzlyAuth(username, password))
                addToConnectedServices(api)
                return api
            }
            else -> throw Exception("Service not supported")
        }
    }

    suspend fun getServiceData(apiInterface: ApiInterface): ApiData? = try { apiInterface.getData() } catch (e: Exception) { null }

    /**
     * This should guarantee that the connected service is unique in the set and update it if it's already connected
     */
    private fun addToConnectedServices(apiInterface: ApiInterface) {
        connectedServices.value.forEach {
            //TODO: check if this works (could be always true, FIXME in that case)
            // Also maybe use something else than reflection to check if the same instance is already connected
            if (it::class === apiInterface::class) {
                connectedServices.value = connectedServices.value.minus(it).plus(apiInterface)
                return@addToConnectedServices
            }
        }
        connectedServices.value = connectedServices.value.plus(apiInterface)
    }
}