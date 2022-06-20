package fr.pirids.idsapp.controller.detection

import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.model.api.auth.IzlyAuth
import fr.pirids.idsapp.model.api.data.ApiData
import fr.pirids.idsapp.model.items.Service
import fr.pirids.idsapp.model.items.ServiceId

object Service {
    private const val checkingDelayMillis = 10_000L

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
            ServiceId.IZLY -> return IzlyApi(IzlyAuth(username, password))
            else -> throw Exception("Service not supported")
        }
    }

    suspend fun getServiceData(apiInterface: ApiInterface): ApiData? = try { apiInterface.getData() } catch (e: Exception) { null }

}