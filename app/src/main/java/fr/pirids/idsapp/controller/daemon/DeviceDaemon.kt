package fr.pirids.idsapp.controller.daemon

import android.content.Context
import android.os.Build
import android.util.Log
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.controller.bluetooth.Device
import fr.pirids.idsapp.data.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.data.device.data.WalletCardData
import fr.pirids.idsapp.data.model.AppDatabase
import java.time.Instant
import java.time.ZoneId
import java.util.*

object DeviceDaemon {
    suspend fun loadDevice() {
        try {
            AppDatabase.getInstance().deviceDao().getAll().forEach {
                try {
                    val deviceData = Device.getDeviceDataByName(it.name)

                    when(deviceData) {
                        is WalletCardData -> {
                            // If there is data, we load it
                            try {
                                val deviceDataId = AppDatabase
                                    .getInstance()
                                    .deviceDataDao()
                                    .getFromDeviceAndType(
                                        it.id,
                                        AppDatabase.getInstance().deviceDataTypeDao().getByName(WalletCardData.tag).id
                                    ).id
                                val dataList = AppDatabase.getInstance().walletCardDataDao().getAllFromDeviceData(deviceDataId)
                                dataList.forEach { data ->
                                    val timestamp = Instant
                                        .ofEpochMilli(data.walletOutTimestamp)
                                        .atZone(ZoneId.of("UTC"))
                                        .withZoneSameInstant(TimeZone.getDefault().toZoneId())
                                    deviceData.whenWalletOutArray.value = deviceData.whenWalletOutArray.value.plus(timestamp)
                                }
                            } catch(e: Exception) { }
                        }
                    }

                    Device.addToKnownDevices(BluetoothDeviceIDS(it.name, it.address, deviceData))
                } catch (e: Exception) {
                    Log.e("DeviceDaemon", "Error while adding device to known devices", e)
                }
            }
        } catch (e: Exception) {
            Log.e("DeviceDaemon", "Error while getting all devices", e)
        }
    }

    fun searchForDevice(ctx: Context) {
        val bleBackground = BluetoothConnection(ctx)
        try {
            if(Build.VERSION.SDK_INT < 31) {
                bleBackground.registerBroadCast()
            }
            bleBackground.initSearch()
        } catch (e: Exception) {
            Log.e("DeviceDaemon", "Error while searching for devices", e)
            if(Build.VERSION.SDK_INT < 31) {
                bleBackground.unregisterBroadCast()
            }
        }
    }
}