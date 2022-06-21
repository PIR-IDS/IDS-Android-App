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
import androidx.lifecycle.LiveData
import fr.pirids.idsapp.model.items.bluetooth.BluetoothCharacteristic
import fr.pirids.idsapp.model.items.bluetooth.BluetoothService
import fr.pirids.idsapp.model.items.bluetooth.CharacteristicId
import fr.pirids.idsapp.model.items.bluetooth.ServiceId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.*
import java.util.*

class BluetoothConnection(private val mContext: Context) {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothGatt: BluetoothGatt

    private lateinit var idsDevice : BluetoothDevice

    private lateinit var timeService: BluetoothGattService
    private lateinit var walletService: BluetoothGattService

    private lateinit var walletOutCharacteristic: BluetoothGattCharacteristic
    private lateinit var whenWalletOutCharacteristic: BluetoothGattCharacteristic

    private val CCCD_UUID = BluetoothCharacteristic.get(CharacteristicId.CCCD).uuid
    private val TIME_SERVICE_UUID = BluetoothService.get(ServiceId.CURRENT_TIME).uuid
    private val WALLET_SERVICE_UUID = BluetoothService.get(ServiceId.CUSTOM_IDS_IMU).uuid
    private val DATE_SERVICE_UUID = BluetoothService.get(ServiceId.CURRENT_TIME).characteristics.find { it.id == CharacteristicId.CURRENT_TIME }!!.uuid
    private val WALLET_OUT_UUID = BluetoothService.get(ServiceId.CUSTOM_IDS_IMU).characteristics.find { it.id == CharacteristicId.BOOLEAN }!!.uuid
    private val WHEN_WALLET_OUT_UUID = BluetoothService.get(ServiceId.CUSTOM_IDS_IMU).characteristics.find { it.id == CharacteristicId.DATE_UTC }!!.uuid

    private val whenWalletOutArray = mutableListOf<ZonedDateTime>()


    fun setUpBluetooth(resultLauncher: ActivityResultLauncher<Intent>) {
        enableScan(resultLauncher)
    }

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

    private fun enableScan(resultLauncher: ActivityResultLauncher<Intent>) {
        val context = Context.BLUETOOTH_SERVICE;
        val bluetoothManager = mContext.getSystemService(context) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultLauncher.launch(enableBtIntent)
        } else {
            CoroutineScope(Dispatchers.Default).launch { initScan() }
        }
    }

    fun handleBluetoothIntent(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            CoroutineScope(Dispatchers.Default).launch { initScan() }
        }
    }

    suspend fun initScan() {
        scanLE(deviceFlow(bluetoothAdapter.bluetoothLeScanner))
    }

    private fun deviceFlow(scanner: BluetoothLeScanner) : Flow<ScanResult> = callbackFlow {
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
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
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

    private suspend fun scanLE(flow: Flow<ScanResult>) {
        flow.collect {
            try {
                Log.d("BluetoothGattDiscoverServices", "Scan result: ${it.device.name}")
                if (it.device.name == "PIR-IDS") {
                    idsDevice = it.device
                    connect()
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
            }
        }
    }

    fun connect() {
        try {
            bluetoothGatt = idsDevice.connectGatt(mContext, false, gattCallback)
        } catch (e: SecurityException) {
            Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
        }

        //afficherToast("device connected" + bluetoothGatt.device)


        // connect to time service

        // send date before discovering service
        //val dateServiceUUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
        //val currentTimeMillis = System.currentTimeMillis()
        //val timeCharacteristic = timeService.getCharacteristic(dateServiceUUID)
        //writeCharacteristic(timeCharacteristic, currentTimeMillis.toString().toByteArray())

        //Log.i("BluetoothGattDiscoverServices", "Discovering gatt services")
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                        // TODO: Store a reference to BluetoothGatt
                        bluetoothGatt.discoverServices()
                        //afficherToast("IDS device connected");
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                        //afficherToast("IDS device disconnected");
                        bluetoothGatt.close()
                    }
                } else {
                    Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                    //afficherToast("IDS device not connected");
                    bluetoothGatt.close()
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                try {
                    Log.w("BluetoothGattCallback", "Discovered ${services.size} services for ${device.address}")
                    printGattTable()

                    timeService = bluetoothGatt.getService(TIME_SERVICE_UUID)
                    Log.i("time", "Time service found $timeService")

                    val currentTimeMillis = System.currentTimeMillis()
                    Log.i("time", currentTimeMillis.toString())

                    Log.i("BluetoothGattCallback", "semaphore ok")

                    val dateServiceUUID = DATE_SERVICE_UUID
                    val timeCharacteristic = timeService.getCharacteristic(dateServiceUUID)
                    if (!timeCharacteristic.isWritable()) {
                        Log.i("BluetoothGattCallback", "not writable")
                    }
                    timeCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    timeCharacteristic.value = currentTimeMillis.toString().toByteArray()
                    val result = writeCharacteristic(timeCharacteristic)

                    Log.i("BluetoothGattCallback", "write result=$result")
                } catch (e: SecurityException) {
                    Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            with(characteristic) {
                //Log.i("BluetoothGattCallback", "Characteristic ${this?.uuid} changed | value: ${this?.value}")
                try {
                    gatt?.readCharacteristic(whenWalletOutCharacteristic)
                } catch (e: SecurityException) {
                    Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Log.i("BluetoothGattCallback", "ack received")
            walletService = bluetoothGatt.getService(WALLET_SERVICE_UUID)
            val walletOutCharacteristicUUID = WALLET_OUT_UUID
            walletOutCharacteristic = walletService.getCharacteristic(walletOutCharacteristicUUID)

            val whenWalletOutCharacteristicUUID = WHEN_WALLET_OUT_UUID
            whenWalletOutCharacteristic = walletService.getCharacteristic(whenWalletOutCharacteristicUUID)

            enableNotifications(walletOutCharacteristic)

            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
            try {
                bluetoothGatt?.let { gatt ->
                    descriptor.value = payload
                    gatt.writeDescriptor(descriptor)
                } ?: error("Not connected to a BLE device!")
            } catch (e: SecurityException) {
                Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
            }
        }

        fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
            val cccdUuid =CCCD_UUID
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
                    if (bluetoothGatt?.setCharacteristicNotification(characteristic, true) == false) {
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
                    if (bluetoothGatt?.setCharacteristicNotification(characteristic, false) == false) {
                        Log.e("ConnectionManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
                        return
                    }
                } catch (e: SecurityException) {
                    Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
                }
                writeDescriptor(cccDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
            } ?: Log.e("ConnectionManager", "${characteristic.uuid} doesn't contain the CCC descriptor!")
        }

        fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

        fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

        fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
            properties and property != 0

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Log.i("BluetoothGattCallback", "Characteristic read callback")
            //val stringValue = characteristic?.value?.let { String(it, StandardCharsets.UTF_8) }
            val time = characteristic?.getStringValue(0)

            // ex 2022-05-28T12:50:59.000Z
            //var utcTime = OffsetDateTime.parse(time, formatter)
            //utcTime = utcTime.withOffsetSameInstant(ZoneOffset.UTC)

            //Log.d("IDS_wallet", time.toString())
            //Log.d("IDS_wallet time", utcTime.toString())

            val dateTime = LocalDateTime.parse(characteristic?.getStringValue(0)!!.dropLast(1)).atZone(ZoneId.of("UTC"))
            val localDateTime = dateTime.withZoneSameInstant(TimeZone.getDefault().toZoneId())

            whenWalletOutArray.add(localDateTime)
            //Log.i("BluetoothGattCallback", "whenWalletOut date=$stringValue")
            //val dateTime = LocalDateTime.parse(stringValue!!.dropLast(1)).atZone(ZoneId.of("UTC"))
            //val localDateTime = dateTime.withZoneSameInstant(TimeZone.getDefault().toZoneId())
            //activity.runOnUiThread { Toast.makeText(activity, "Wallet out at $localDateTime", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
    }

    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }
}