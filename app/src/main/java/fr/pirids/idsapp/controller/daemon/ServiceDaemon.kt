package fr.pirids.idsapp.controller.daemon

import android.util.Log
import fr.pirids.idsapp.controller.detection.Service
import fr.pirids.idsapp.data.items.ServiceId
import fr.pirids.idsapp.data.model.AppDatabase
import fr.pirids.idsapp.data.items.Service.Companion as ServiceItem

object ServiceDaemon {
    suspend fun connectToServices() {
        try {
            AppDatabase.getInstance().apiAuthDao().getAll().forEach {
                try {
                    when(it.serviceName) {
                        "izly" -> {
                            val izlyAuth = AppDatabase.getInstance().izlyAuthDao().getFromApi(it.id)

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
        } catch (e: Exception) {
            Log.e("ServiceDaemon", "Error while getting all services")
        }
    }
}