package fr.pirids.idsapp.controller.api

import fr.pirids.idsapp.data.api.auth.ApiAuth
import fr.pirids.idsapp.data.api.data.ApiData

interface ApiInterface {
    /**
     * Authentication method
     */
    fun authenticate(credentials: ApiAuth)

    /**
     * Check if the Service answers correctly
     */
    fun checkConnection(): Boolean

    /**
     * Returns the data from the server
     */
    suspend fun getData() : ApiData
}