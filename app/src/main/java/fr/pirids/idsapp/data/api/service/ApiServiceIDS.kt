package fr.pirids.idsapp.data.api.service

import androidx.compose.runtime.MutableState
import fr.pirids.idsapp.controller.api.ApiInterface
import fr.pirids.idsapp.data.api.data.ApiData
import fr.pirids.idsapp.data.items.ServiceId

data class ApiServiceIDS(val serviceId: ServiceId, var api: ApiInterface? = null, var data: MutableState<ApiData>)