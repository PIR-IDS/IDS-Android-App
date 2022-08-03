package fr.pirids.idsapp.controller.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.companion.AssociationRequest
import android.companion.BluetoothLeDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.*
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.detection.NotificationHandler
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
import java.util.regex.Pattern
import fr.pirids.idsapp.data.items.Device as DeviceItem
import fr.pirids.idsapp.data.model.entity.device.DeviceData as DeviceDataEntity
import fr.pirids.idsapp.data.model.entity.device.WalletCardData as WalletCardDataEntity

class BluetoothConnection(private val mContext: Context) {
    var permissionsGranted = false
    private val defaultScope = CoroutineScope(Dispatchers.IO)
    private var bluetoothAdapter: BluetoothAdapter? = null

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                when (intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> {
                        Device.connectedDevices.value = setOf()
                        defaultScope.launch {
                            NotificationHandler.triggerNotification(
                                context = mContext,
                                title = mContext.resources.getString(R.string.device_connection_failed),
                                message = mContext.resources.getString(R.string.device_connection_failed),
                            )
                        }
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> {}
                    BluetoothAdapter.STATE_ON -> {
                        //TODO: do something better than this...
                        if(permissionsGranted) {
                            defaultScope.launch {
                                this@BluetoothConnection.initSearch()
                            }
                        }
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> {}
                }
            }
        }
    }

    fun initiateAssociation(managedActivity: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>, macAddress: String, onConnected: (Boolean) -> Unit = {}) {
        val deviceFilter: BluetoothLeDeviceFilter = BluetoothLeDeviceFilter.Builder()
            // Match only Bluetooth devices whose name matches the pattern.
            .setNamePattern(Pattern.compile(DeviceItem.idsPrefix + " *"))
            .setScanFilter(
                ScanFilter.Builder()
                    // Be sure to match the device selected by the user
                    .setDeviceAddress(macAddress)
                    .build()
            )
            .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            // Find only devices that match this request filter.
            .addDeviceFilter(deviceFilter)
            // We should only have the one selected
            .setSingleDevice(true)
            .build()

        val deviceManager = mContext.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

        fun launchChooser(chooserLauncher: IntentSender) {
            //TODO: override the chooser launcher to use the custom chooser
            // Note: it seems to be impossible at the moment as it uses another process to launch the chooser
            // The workaround for now is to still use the old scan to find the device and then launch the chooser
            // with the one selected by the user, only to confirm the pairing
            managedActivity.launch(IntentSenderRequest.Builder(chooserLauncher).build())
        }

        deviceManager.associate(pairingRequest,
            if(Build.VERSION.SDK_INT >= 33) {
                object : CompanionDeviceManager.Callback() {
                    // Called when a device is found. Launch the IntentSender so the user
                    // can select the device they want to pair with.
                    override fun onAssociationPending(chooserLauncher: IntentSender) = launchChooser(chooserLauncher)

                    override fun onFailure(error: CharSequence?) {
                        Log.e("BluetoothConnection", "onFailure: $error")
                        onConnected(false)
                    }
                }
            } else {
                object : CompanionDeviceManager.Callback() {
                    // Called when a device is found. Launch the IntentSender so the user
                    // can select the device they want to pair with.
                    @Deprecated("Deprecated in API 33", ReplaceWith(
                        "managedActivity.launch(IntentSenderRequest.Builder(chooserLauncher).build())",
                        "androidx.activity.result.IntentSenderRequest"
                    ))
                    override fun onDeviceFound(chooserLauncher: IntentSender) = launchChooser(chooserLauncher)

                    override fun onFailure(error: CharSequence?) {
                        Log.e("BluetoothConnection", "onFailure: $error")
                        onConnected(false)
                    }
                }
            }, null)
    }

    fun pair(result: ActivityResult, onConnected: (Boolean) -> Unit = {}) {
        try {
            // The user chose to pair the app with a Bluetooth device.
            val deviceToPair: BluetoothDevice? = if (Build.VERSION.SDK_INT >= 33) {
                (result.data?.getParcelableExtra(CompanionDeviceManager.EXTRA_ASSOCIATION, BluetoothDevice::class.java) as ScanResult?)?.device
            } else {
                (result.data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE) as ScanResult?)?.device
            }

            deviceToPair?.let { device ->
                bond(device, onConnected)
            } ?: onConnected(false)
        } catch (e: Exception) {
            Log.e("BluetoothConnection", "Unable to pair with device", e)
            onConnected(false)
        }
    }

    fun unpair(address: String) {
        val deviceManager = mContext.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
        if (Build.VERSION.SDK_INT >= 33) {
            deviceManager.myAssociations.find { it.deviceMacAddress.toString() == address }?.let {
                deviceManager.stopObservingDevicePresence(it.deviceMacAddress.toString())
                deviceManager.disassociate(it.id)
            }
        } else {
            deviceManager.associations.find { it == address }?.let {
                if (Build.VERSION.SDK_INT >= 31) {
                    deviceManager.stopObservingDevicePresence(it)
                }
                deviceManager.disassociate(it)
            }
        }
    }

    private fun bond(device: BluetoothDevice, onConnected: (Boolean) -> Unit = {}) {
        try {
            val deviceManager = mContext.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
            val bleDevice = BluetoothDeviceIDS(device.name, device.address, getDeviceData(device), device)

            // Pair with the device.
            device.createBond()

            // We observe the device presence in order to trigger the connection even if the app is not in the foreground
            if (Build.VERSION.SDK_INT >= 31) {
                deviceManager.startObservingDevicePresence(bleDevice.address)
            } else {
                //TODO: observe with another way
            }

            // Connect
            connect(bleDevice, onConnected)
        } catch (e: SecurityException) {
            Log.e("BluetoothConnection", "Unable to bond with device", e)
            onConnected(false)
        } catch (e: Exception) {
            Log.e("BluetoothConnection", "Unable to bond with device", e)
            onConnected(false)
        }
    }

    fun registerBroadCast() {
        mContext.registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    fun unregisterBroadCast() {
        mContext.unregisterReceiver(bluetoothReceiver)
    }

    fun getNecessaryPermissions() : List<String> {
        val permissions: MutableList<String> = mutableListOf()
        permissions.add(Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND)
        permissions.add(Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND)
        if (Build.VERSION.SDK_INT >= 31) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.REQUEST_OBSERVE_COMPANION_DEVICE_PRESENCE)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            permissions.add(Manifest.permission.BLUETOOTH)
        }
        return permissions.toList()
    }

    fun onPermissionsResult(result: Map<String, Boolean>, daemonMode: Boolean, resultLauncher: ActivityResultLauncher<Intent>, scope: CoroutineScope = defaultScope) {
        if(result.all { it.value }) {
            permissionsGranted = true
            scope.launch {
                if(daemonMode)
                    this@BluetoothConnection.searchForKnownDevices(resultLauncher, scope)
                else
                    this@BluetoothConnection.launchScan(resultLauncher, scope)
            }
            Log.i("BluetoothGattDiscoverServices", "Bluetooth permission granted")
        } else {
            permissionsGranted = false
            Log.i("BluetoothGattDiscoverServices", "Bluetooth permission not granted")
        }
    }

    private fun searchForKnownDevices(resultLauncher: ActivityResultLauncher<Intent>, scope: CoroutineScope = defaultScope) {
        val context = Context.BLUETOOTH_SERVICE
        val bluetoothManager = mContext.getSystemService(context) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultLauncher.launch(enableBtIntent)
        } else {
            scope.launch { initSearch() }
        }
    }

    fun handleSearchBluetoothIntent(result: ActivityResult, scope: CoroutineScope = defaultScope) {
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch { initSearch() }
        }
    }

    private fun launchScan(resultLauncher: ActivityResultLauncher<Intent>, scope: CoroutineScope = defaultScope) {
        val context = Context.BLUETOOTH_SERVICE
        val bluetoothManager = mContext.getSystemService(context) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter!!.isEnabled) {
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
        //TODO: this is the old way to scan for companion devices, but we still need it because of the Companion ChooserLauncher
        // which is really bad, not customizable and not user friendly...
        try {
            scanLE(deviceFlow((bluetoothAdapter ?: (mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter).bluetoothLeScanner))
        } catch (e: Exception) {
            Log.e("BluetoothConnection", "Error while scanning for devices", e)
        }
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

        val filters = listOf<ScanFilter>(
            ScanFilter.Builder()
                //TODO: maybe set a global IDS service in order to filter the devices
                // here we use the IDS service from the Wallet Card for convenience

                // Match only Bluetooth devices whose service UUID matches this pattern.
                .setServiceUuid(ParcelUuid(BluetoothService.get(ServiceId.CUSTOM_IDS_IMU).uuid))
                .build()
        )

        try {
            scanner.startScan(filters, settings, scanCallback)
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
                if (it.device.name?.startsWith(DeviceItem.idsPrefix) == true) {
                    Device.addToFoundDevices(BluetoothDeviceIDS(it.device.name, it.device.address, getDeviceData(it.device), it.device))
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
            }
        }
    }

    fun initSearch() {
        try {
            searchLE()
        } catch (e: Exception) {
            Log.e("BluetoothConnection", "Error while searching for devices", e)
        }
    }

    fun connectFromAddress(macAddress: String) {
        try {
            val deviceFound = Device.getBluetoothDeviceFromAddress(macAddress)
            deviceFound!!.device = (bluetoothAdapter ?: (mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter).getRemoteDevice(deviceFound.address)
            connect(deviceFound)
        } catch (e: Exception) {
            Log.e("BluetoothGattDiscoverServices", "Error while connecting to device", e)
        }
    }

    private fun searchLE() {
        try {
            val deviceManager = mContext.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
            val macList = if (Build.VERSION.SDK_INT >= 33) {
                deviceManager.myAssociations.map { it.deviceMacAddress.toString() }
            } else {
                deviceManager.associations
            }

            macList.filter { it !in Device.connectedDevices.value.map { d -> d.address } }.forEach { macAddress ->
                connectFromAddress(macAddress)
            }

            // If we lost the bond with a device, we need to reconnect to it
            Device.knownDevices.value.filter { it.address !in macList }.map { bleDev ->
                bleDev.device
                    ?.let { bond(it) }
                    ?: run {
                        val dev = (bluetoothAdapter ?: (mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter).getRemoteDevice(bleDev.address)
                        bond(dev)
                    }
            }
        } catch (e: SecurityException) {
            Log.e("BluetoothGattDiscoverServices", "Error while collecting search result", e)
        }
    }

    private fun getDeviceData(device: BluetoothDevice) : DeviceData =
        try {
            Device.getDeviceDataByName(device.name)
        } catch (e: SecurityException) {
            Log.e("BluetoothGattDiscoverServices", "Error while collecting scan result", e)
            throw e
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
                    onConnected(false)
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
                                if(Device.removeFromConnectedDevices(idsDevice)) {
                                    defaultScope.launch {
                                        NotificationHandler.triggerNotification(
                                            context = mContext,
                                            title = mContext.resources.getString(R.string.device_connection_failed),
                                            message = idsDevice.name + " [${idsDevice.address}] | " + mContext.resources.getString(R.string.device_connection_failed),
                                            icon = Device.getDeviceItemFromBluetoothDevice(idsDevice)?.logo
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }
                    } else {
                        Log.e("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                        bluetoothGatt.close()
                        if(Device.removeFromConnectedDevices(idsDevice)) {
                            defaultScope.launch {
                                NotificationHandler.triggerNotification(
                                    context = mContext,
                                    title = mContext.resources.getString(R.string.device_connection_failed),
                                    message = idsDevice.name + " [${idsDevice.address}] | " + mContext.resources.getString(R.string.device_connection_failed),
                                    icon = Device.getDeviceItemFromBluetoothDevice(idsDevice)?.logo
                                )
                            }
                        }
                        onConnected(false)
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

            @Deprecated("Deprecated in API 33")
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
                    Device.foundDevices.value = setOf()
                    Device.addToKnownDevices(idsDevice)
                    if(Device.addToConnectedDevices(idsDevice)) {
                        defaultScope.launch {
                            NotificationHandler.triggerNotification(
                                context = mContext,
                                title = mContext.resources.getString(R.string.device_connected),
                                message = idsDevice.name + " [${idsDevice.address}] | " + mContext.resources.getString(R.string.device_connected),
                                icon = Device.getDeviceItemFromBluetoothDevice(idsDevice)?.logo
                            )
                        }
                    }

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
            @Deprecated("Deprecated in API 33")
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
                                                    .getFromAddress(idsDevice.address)!!.id,
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
            bluetoothGatt = idsDevice.device?.connectGatt(mContext, false, gattCallback) ?: run { onConnected(false) ; throw Exception("Device null") }
        } catch (e: SecurityException) {
            Log.e("BluetoothGattDiscoverServices", "Error while connecting", e)
            onConnected(false)
        }
    }
}