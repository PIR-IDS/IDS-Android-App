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
import fr.pirids.idsapp.extensions.isIndicatable
import fr.pirids.idsapp.extensions.isNotifiable
import fr.pirids.idsapp.extensions.isWritable
import fr.pirids.idsapp.extensions.printGattTable
import fr.pirids.idsapp.model.items.bluetooth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

import java.time.*
import java.util.*

class BluetoothConnection(private val mContext: Context) {

    private val defaultScope = CoroutineScope(Dispatchers.IO)
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothGatt: BluetoothGatt

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

    fun searchForKnownDevices(devicesList: MutableList<BluetoothDeviceIDS>, resultLauncher: ActivityResultLauncher<Intent>, scope: CoroutineScope = defaultScope) {
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

    fun handleSearchBluetoothIntent(result: ActivityResult, devicesList: MutableList<BluetoothDeviceIDS>, scope: CoroutineScope = defaultScope) {
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
        scanLE(deviceFlow(bluetoothAdapter.bluetoothLeScanner))
    }

    private suspend fun initSearch(devicesList: MutableList<BluetoothDeviceIDS>) {
        searchLE(devicesList, deviceFlow(bluetoothAdapter.bluetoothLeScanner, true))
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

    private suspend fun scanLE(flow: Flow<ScanResult>) {
        flow.collect {
            try {
                Log.d("BluetoothGattDiscoverServices", "Scan result: ${it.device.name}")
                if (it.device.name.startsWith("PIR-IDS")) {
                    Device.foundDevices.add(BluetoothDeviceIDS(it.device.name, it.device.address, it.device))
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
            }
        }
    }

    private suspend fun searchLE(devicesList: MutableList<BluetoothDeviceIDS>, flow: Flow<ScanResult>) {
        flow.collect {
            try {
                Log.d("BluetoothGattDiscoverServices", "Search result: ${it.device.name}")
                devicesList.find { found -> found.address == it.device.address }?.let { _ ->
                    connect(it.device)
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothGattDiscoverServices", "Error while collecting search result", e)
            }
        }
    }

    fun connect(idsDevice: BluetoothDevice) {
        try {
            bluetoothGatt = idsDevice.connectGatt(mContext, false, gattCallback)
        } catch (e: SecurityException) {
            Log.e("BluetoothGattDiscoverServices", "Error while connecting", e)
        }
    }

    private fun initiateDeviceClock(characteristic: BluetoothGattCharacteristic) {
        val dateTime = LocalDateTime.parse(characteristic.getStringValue(0)!!.dropLast(1)).atZone(ZoneId.of("UTC"))
        val localDateTime = dateTime.withZoneSameInstant(TimeZone.getDefault().toZoneId())

        whenWalletOutArray.add(localDateTime) // TODO: create an abstract wrapper for modularity for all devices
    }

    // TODO: handle many connections
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                        // TODO: Store a reference to BluetoothGatt
                        bluetoothGatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                        bluetoothGatt.close()
                    }
                } else {
                    Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                    bluetoothGatt.close()
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothGattDiscoverServices", "Error while connecting to GATT", e)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                try {
                    Log.w("BluetoothGattCallback", "Discovered ${services.size} services for ${device.address}")
                    printGattTable()

                    val timeService = bluetoothGatt.getService(TIME_SERVICE_UUID)
                    Log.i("time", "Time service found $timeService")

                    val dateServiceUUID = DATE_SERVICE_UUID
                    val timeCharacteristic = timeService.getCharacteristic(dateServiceUUID)
                    if (!timeCharacteristic.isWritable()) {
                        Log.i("BluetoothGattCallback", "not writable")
                    }
                    timeCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    timeCharacteristic.value = System.currentTimeMillis().toString().toByteArray()
                    val result = writeCharacteristic(timeCharacteristic)

                    Log.i("BluetoothGattCallback", "write result=$result")
                } catch (e: SecurityException) {
                    Log.e("BluetoothGattDiscoverServices", "Error while discovering services", e)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            with(characteristic) {
                try {
                    gatt?.readCharacteristic(whenWalletOutCharacteristic)
                } catch (e: SecurityException) {
                    Log.e("BluetoothGattDiscoverServices", "Error while reading characteristic", e)
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
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

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.i("BluetoothGattCallback", "Characteristic read callback")
            characteristic?.let { initiateDeviceClock(it) }
        }
    }
}