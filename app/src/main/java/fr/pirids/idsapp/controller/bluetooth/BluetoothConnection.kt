package fr.pirids.idsapp.controller.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import fr.pirids.idsapp.data.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.data.device.data.DeviceData
import fr.pirids.idsapp.data.device.data.WalletCardData
import fr.pirids.idsapp.data.items.DeviceId
import fr.pirids.idsapp.data.items.bluetooth.BluetoothCharacteristic
import fr.pirids.idsapp.data.items.bluetooth.BluetoothService
import fr.pirids.idsapp.data.items.bluetooth.CharacteristicId
import fr.pirids.idsapp.data.items.bluetooth.ServiceId
import fr.pirids.idsapp.data.model.AppDatabase
import fr.pirids.idsapp.extensions.isIndicatable
import fr.pirids.idsapp.extensions.isNotifiable
import fr.pirids.idsapp.extensions.isWritable
import fr.pirids.idsapp.extensions.printGattTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import fr.pirids.idsapp.data.items.Device as DeviceItem
import fr.pirids.idsapp.data.model.entity.DeviceData as DeviceDataEntity
import fr.pirids.idsapp.data.model.entity.WalletCardData as WalletCardDataEntity

class BluetoothConnection(private val mContext: Context) {

    private val defaultScope = CoroutineScope(Dispatchers.IO)
    private lateinit var bluetoothAdapter: BluetoothAdapter

    fun getNecessaryPermissions() : List<String> {
        val permissions: MutableList<String> = mutableListOf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
                permissions.add(Manifest.permission.BLUETOOTH)
            }
        }
        return permissions.toList()
    }

    fun onPermissionsResult(result: Map<String, Boolean>) {
        if(result.all { it.value }) {
            Log.i("BluetoothGattDiscoverServices", "Bluetooth permission granted")
        } else {
            Log.i("BluetoothGattDiscoverServices", "Bluetooth permission not granted")
        }
    }

    fun searchForKnownDevices(devicesList: Set<BluetoothDeviceIDS>, resultLauncher: ActivityResultLauncher<Intent>, scope: CoroutineScope = defaultScope) {
        val context = Context.BLUETOOTH_SERVICE
        val bluetoothManager = mContext.getSystemService(context) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultLauncher.launch(enableBtIntent)
        } else {
            scope.launch { initSearch(devicesList) }
        }
    }

    fun handleSearchBluetoothIntent(result: ActivityResult, devicesList: Set<BluetoothDeviceIDS>, scope: CoroutineScope = defaultScope) {
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch { initSearch(devicesList) }
        }
    }

    fun launchScan(resultLauncher: ActivityResultLauncher<Intent>, scope: CoroutineScope = defaultScope) {
        val context = Context.BLUETOOTH_SERVICE
        val bluetoothManager = mContext.getSystemService(context) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultLauncher.launch(enableBtIntent)
        } else {
            scope.launch { initScan() }
        }
    }

    fun handleScanBluetoothIntent(result: ActivityResult, scope: CoroutineScope = defaultScope) {
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch { initScan() }
        }
    }

    private suspend fun initScan() {
        try {
            scanLE(deviceFlow(bluetoothAdapter.bluetoothLeScanner))
        } catch (e: Exception) {
            Log.e("BluetoothConnection", "Error while scanning for devices", e)
        }
    }

    private suspend fun initSearch(devicesList: Set<BluetoothDeviceIDS>) {
        try {
            searchLE(devicesList, deviceFlow(bluetoothAdapter.bluetoothLeScanner, true))
        } catch (e: Exception) {
            Log.e("BluetoothConnection", "Error while searching for devices", e)
        }
    }

    private fun deviceFlow(scanner: BluetoothLeScanner, background: Boolean = false) : Flow<ScanResult> = callbackFlow {
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(result).onFailure {
                    Log.e("BluetoothGattDiscoverServices", "Error while sending scan result", it)
                }
            }
            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach { trySend(it) }
            }
            override fun onScanFailed(errorCode: Int) {
                Log.e("BluetoothGattDiscoverServices", "Scan failed with error code $errorCode")
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(if (background) ScanSettings.SCAN_MODE_LOW_POWER else ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        try {
            scanner.startScan(null, settings, scanCallback)
        } catch (e: SecurityException) {
            Log.e("BluetoothGattDiscoverServices", "Error while starting scan", e)
        }

        awaitClose {
            try {
                scanner.stopScan(scanCallback)
            } catch (e: SecurityException) {
                Log.e("BluetoothGattDiscoverServices", "Error while stopping scan", e)
            }
        }
    }

    //FIXME: scanned devices not displayed when permission not granted yet, it should wait to have an answer before starting the bluetooth scan
    private suspend fun scanLE(flow: Flow<ScanResult>) {
        flow.collect {
            try {
                Log.d("BluetoothGattDiscoverServices", "Scan result: ${it.device.name} [${it.device.address}]")
                if (it.device.name?.startsWith(DeviceItem.idsPrefix) == true) {
                    Device.addToFoundDevices(BluetoothDeviceIDS(it.device.name, it.device.address, getDeviceData(it.device), it.device))
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
            }
        }
    }

    //FIXME: searched devices not found when permission not granted yet, it should wait to have an answer before starting the bluetooth search
    private suspend fun searchLE(devicesList: Set<BluetoothDeviceIDS>, flow: Flow<ScanResult>) {
        flow.collect {
            try {
                Log.d("BluetoothGattDiscoverServices", "Search result: ${it.device.name}")
                devicesList.find { found -> found.address == it.device.address }?.let { deviceFound ->
                    deviceFound.device = it.device
                    connect(deviceFound)
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothGattDiscoverServices", "Error while collecting search result", e)
            }
        }
    }

    private fun getDeviceData(device: BluetoothDevice) : DeviceData =
        try {
            getDeviceDataByName(device.name)
        } catch (e: SecurityException) {
            Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
            throw e
        }

    fun getDeviceDataByName(name: String) : DeviceData =
        when(Device.getDeviceItemFromName(name)?.id) {
            DeviceId.WALLET_CARD -> WalletCardData()
            else -> throw Exception("Unknown device type")
        }

    fun connect(idsDevice: BluetoothDeviceIDS, onConnected: (Boolean) -> Unit = {}) {
        lateinit var bluetoothGatt: BluetoothGatt
        var initialized = false
        val deviceItem = Device.getDeviceItemFromBluetoothDevice(idsDevice)

        // TODO: maybe create a callbackFlow for each connection
        val gattCallback = object : BluetoothGattCallback() {
            private val CCCD_UUID = BluetoothCharacteristic.get(CharacteristicId.CCCD).uuid
            private val TIME_SERVICE_UUID = BluetoothService.get(ServiceId.CURRENT_TIME).uuid

            fun initiateDeviceClock(gatt: BluetoothGatt) {
                val timeService = bluetoothGatt.getService(TIME_SERVICE_UUID)
                Log.i("time", "Time service found $timeService")

                val dateServiceUUID = BluetoothService.get(ServiceId.CURRENT_TIME).characteristics.find { it.id == CharacteristicId.CURRENT_TIME }!!.uuid
                val timeCharacteristic = timeService.getCharacteristic(dateServiceUUID)
                if (!timeCharacteristic.isWritable()) {
                    Log.i("BluetoothGattCallback", "not writable")
                }
                timeCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                timeCharacteristic.value = System.currentTimeMillis().toString().toByteArray()
                try {
                    val result = gatt.writeCharacteristic(timeCharacteristic)
                    Log.i("BluetoothGattCallback", "write result=$result")
                } catch (e: SecurityException) {
                    Log.e("BluetoothGattDiscoverServices", "Error while discovering services", e)
                }
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                val deviceAddress = gatt.device.address
                try {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        when(newState) {
                            BluetoothProfile.STATE_CONNECTED -> {
                                Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                                // TODO: Store a reference to BluetoothGatt
                                // This triggers the onServicesDiscovered callback
                                bluetoothGatt.discoverServices()
                            }
                            BluetoothProfile.STATE_DISCONNECTED -> {
                                Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                                bluetoothGatt.close()
                                Device.connectedDevices.value.minus(idsDevice)
                            }
                            else -> {}
                        }
                    } else {
                        Log.e("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                        bluetoothGatt.close()
                        Device.connectedDevices.value.minus(idsDevice)
                    }
                } catch (e: SecurityException) {
                    Log.e("BluetoothGattDiscoverServices", "Error while connecting to GATT", e)
                }
            }

            /**
             * We are probably here after being connected to the device, we launch the device clock initialization
             */
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                Log.w("BluetoothGattCallback", "Discovered ${gatt.services.size} services for ${gatt.device.address}")
                gatt.printGattTable()
                initiateDeviceClock(gatt)
            }

            fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
                try {
                    bluetoothGatt.let { gatt ->
                        descriptor.value = payload
                        gatt.writeDescriptor(descriptor)
                    }
                } catch (e: SecurityException) {
                    Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
                }
            }

            fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
                val cccdUuid = CCCD_UUID
                val payload = when {
                    characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    else -> {
                        Log.e("ConnectionManager", "${characteristic.uuid} doesn't support notifications/indications")
                        return
                    }
                }

                characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                    try {
                        if (!bluetoothGatt.setCharacteristicNotification(characteristic, true)) {
                            Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                            return
                        }
                    } catch (e: SecurityException) {
                        Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
                    }
                    writeDescriptor(cccDescriptor, payload)
                } ?: Log.e("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")
            }

            fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
                if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
                    Log.e("ConnectionManager", "${characteristic.uuid} doesn't support indications/notifications")
                    return
                }

                val cccdUuid = CCCD_UUID
                characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                    try {
                        if (!bluetoothGatt.setCharacteristicNotification(characteristic, false)) {
                            Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                            return
                        }
                    } catch (e: SecurityException) {
                        Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
                    }
                    writeDescriptor(cccDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
                } ?: Log.e("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                try {
                    gatt?.readCharacteristic(characteristic)
                } catch (e: SecurityException) {
                    Log.e("BluetoothGattDiscoverServices", "Error while reading characteristic", e)
                }
            }

            /**
             * We will get there when a characteristic is written, mainly when we are initializing the device clock.
             */
            override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                Log.i("BluetoothGattCallback", "onCharacteristicWrite")
                // If we just wrote the device clock, we can enable notifications for the wanted characteristics
                //TODO: handle this init phase in a better way...
                if(!initialized) {
                    when (deviceItem?.id) {
                        DeviceId.WALLET_CARD -> {
                            val walletServiceIDS = deviceItem.getBluetoothService(ServiceId.CUSTOM_IDS_IMU)
                            val walletService = walletServiceIDS
                                ?.let { bluetoothGatt.getService(it.uuid) }
                                ?: throw Exception("Wallet service not found")

                            (idsDevice.data as WalletCardData).walletOutCharacteristic = walletServiceIDS.getBluetoothCharacteristic(CharacteristicId.BOOLEAN)
                                ?.let { walletService.getCharacteristic(it.uuid) }
                                ?: throw Exception("walletOutCharacteristic not found")

                            idsDevice.data.whenWalletOutCharacteristic = walletServiceIDS.getBluetoothCharacteristic(CharacteristicId.DATE_UTC)
                                ?.let { walletService.getCharacteristic(it.uuid) }
                                ?: throw Exception("whenWalletOutCharacteristic not found")

                            enableNotifications(idsDevice.data.walletOutCharacteristic!!)
                        }
                        else -> {
                            Log.e(
                                "BluetoothGattCallback",
                                "Unknown device ${deviceItem?.id}"
                            )
                        }
                    }

                    // We add the newly initialized device to the list of connected devices
                    Device.foundDevices.value = Device.foundDevices.value.minus(idsDevice)
                    Device.addToKnownDevices(idsDevice)
                    Device.addToConnectedDevices(idsDevice)

                    // Save in database
                    Device.addKnownDeviceToDatabase(idsDevice)

                    // We also execute the onConnected callback
                    onConnected(true)

                    initialized = true
                }

                super.onCharacteristicWrite(gatt, characteristic, status)
            }

            /**
             * We will get there when a characteristic is read, notably after one has changed
             */
            override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                Log.i("BluetoothGattCallback", "Characteristic read callback")
                characteristic?.let { char ->
                    when(deviceItem?.id) {
                        DeviceId.WALLET_CARD -> {

                            // If we have the wallet out, we add the timestamp to the list
                            when(char) {
                                (idsDevice.data as WalletCardData).walletOutCharacteristic -> {
                                    try {
                                        gatt?.readCharacteristic(idsDevice.data.whenWalletOutCharacteristic)
                                    } catch (e: SecurityException) {
                                        Log.e("BluetoothGattDiscoverServices", "Error while connecting", e)
                                    }
                                }
                                idsDevice.data.whenWalletOutCharacteristic -> {
                                    val dateTime = LocalDateTime.parse(char.getStringValue(0)!!.dropLast(1)).atZone(ZoneId.of("UTC"))
                                    val localDateTime = dateTime.withZoneSameInstant(TimeZone.getDefault().toZoneId())

                                    // Add the timestamp to the list
                                    idsDevice.data.whenWalletOutArray.value = idsDevice.data.whenWalletOutArray.value.plus(localDateTime)

                                    // Save in database
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            // We define the entity
                                            val deviceDataEntity = DeviceDataEntity(
                                                deviceId = AppDatabase.getInstance()
                                                    .deviceDao()
                                                    .getFromAddress(idsDevice.address).id,
                                                dataTypeId = AppDatabase.getInstance()
                                                    .deviceDataTypeDao()
                                                    .getByName(WalletCardData.tag).id
                                            )

                                            // We retrieve the data entity or create a new one if it doesn't exist
                                            val deviceDataId = try {
                                                AppDatabase.getInstance().deviceDataDao().getFromDeviceAndType(
                                                    device_id = deviceDataEntity.deviceId,
                                                    data_type_id = deviceDataEntity.dataTypeId
                                                ).id
                                            } catch (e: Exception) {
                                                AppDatabase.getInstance().deviceDataDao().insert(deviceDataEntity)
                                            }

                                            // We add the timestamp to the database
                                            AppDatabase.getInstance().walletCardDataDao().insert(
                                                WalletCardDataEntity(
                                                    deviceDataId = deviceDataId.toInt(),
                                                    walletOutTimestamp = localDateTime.toInstant().toEpochMilli()
                                                )
                                            )
                                        } catch (e: Exception) {
                                            Log.e("BluetoothConnection", "Error while saving device data in database: $e")
                                        }
                                    }
                                }
                                else -> { }
                            }

                        }
                        else -> {
                            Log.e("BluetoothGattCallback", "Unknown device ${deviceItem?.id}")
                        }
                    }
                }
            }
        }

        // Connect to the device
        try {
            bluetoothGatt = idsDevice.device?.connectGatt(mContext, false, gattCallback) ?: throw Exception("Device is null")
        } catch (e: SecurityException) {
            Log.e("BluetoothGattDiscoverServices", "Error while connecting", e)
        }
    }
}