package fr.pirids.idsapp.controller.daemon.services

import android.companion.AssociationInfo
import android.companion.CompanionDeviceService
import android.util.Log
import androidx.annotation.RequiresApi
import fr.pirids.idsapp.controller.Initiator
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

@RequiresApi(31)
class BluetoothCompanionService: CompanionDeviceService() {
    @Deprecated("Deprecated in API 33", ReplaceWith(
        "super.onDeviceAppeared(address)",
        "android.companion.CompanionDeviceService"
    ))
    override fun onDeviceAppeared(address: String) {
        appeared(address)
    }

    @Deprecated("Deprecated in API 33", ReplaceWith(
        "super.onDeviceDisappeared(address)",
        "android.companion.CompanionDeviceService"
    ))
    override fun onDeviceDisappeared(address: String) {
        disappeared(address)
    }

    @RequiresApi(33)
    override fun onDeviceAppeared(associationInfo: AssociationInfo) {
        appeared(associationInfo.deviceMacAddress.toString())
    }

    @RequiresApi(33)
    override fun onDeviceDisappeared(associationInfo: AssociationInfo) {
        disappeared(associationInfo.deviceMacAddress.toString())
    }

    private fun appeared(address: String) {
        // If we have API >= 31 the IDS detection will happen there
        try {
            //TODO: optimize this: indeed for now we are launching the detection every time a device is connected
            // which means that we are calling the APIs every time a device is connected
            // Also, find a fix to correctly add the connectedDevicesDuringDetection, because here each device is processed separately
            // Update: it seems that it's not the case? That only one process is used for all the connections? Not sure though.
            // To improve this we could:
            // - launch a Worker that will fetch the APIs regularly, and save the data in the database
            // - instead of launching the service scan, retrieve the data from the database and use it in Detection
            // - use knownServices instead of connectedServices in monitorServices

            // We initialize the app
            Initiator.init(this)
            Initiator.handleServices(this)
            //TODO: improve this, it's way too dirty
            while(!Initiator.initialized.value) {
                runBlocking { delay(100) }
            }
            Initiator.monitorServicesIndefinitely(this)
            BluetoothConnection(this).connectFromAddress(address)
        } catch (e: Exception) {
            Log.e("BluetoothCompanionService", "Error while connecting to the device", e)
        }
    }

    private fun disappeared(address: String) { }
}