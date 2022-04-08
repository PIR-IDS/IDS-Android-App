package fr.pirids.idsapp.controller.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import fr.pirids.idsapp.R
import java.util.*

private const val SCAN_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2
private const val CONNECT_BLUETOOTH_REQUEST_CODE = 3

class BluetoothConnection(var mContext: Context, var activity: AppCompatActivity, var buttonScanLE : Button, var buttonConnect : Button) {

    lateinit var bluetoothAdapter: BluetoothAdapter

    lateinit var bluetoothLeScanner : BluetoothLeScanner

    lateinit var idsDevice : BluetoothDevice

    lateinit var bluetoothGatt: BluetoothGatt

    lateinit var timeService: BluetoothGattService

    val SCAN_PERIOD : Long = 10000

    var resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
        }
    }

    private fun requestPermission(permissionName: String, permissionRequestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permissionName), permissionRequestCode)
    }

    private fun showExplanation(
        title: String,
        message: String,
        permission: String,
        permissionRequestCode: Int
    ) {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(mContext)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    requestPermission(
                        permission,
                        permissionRequestCode
                    )
                })
        builder.create().show()
    }

    public fun setUpBT() {
        val context = Context.BLUETOOTH_SERVICE;
        var bluetoothManager = mContext.getSystemService(context) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            afficherToast("err bluetooth adapter")
        }

        // enabling bt
        if (!bluetoothAdapter?.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultLauncher.launch(enableBtIntent)
        }

        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        buttonScanLE.isEnabled = true
    }

    fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    val isBluetoothScanGranted
        get() = mContext.hasPermission(Manifest.permission.BLUETOOTH_SCAN)

    val isBluetoothConnectGranted
        get() = mContext.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    val isLocationPermissionGranted
        get() = mContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestLocationPermission() {
        if (!isLocationPermissionGranted) {
            showExplanation("Location permission required", "Starting from Android M (6.0), the system requires apps to be granted " +
                    "location access in order to scan for BLE devices.", Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestBluetoothScan() {
        if (!isBluetoothScanGranted) {
            showExplanation("Bluetooth Scan permission required", "", Manifest.permission.BLUETOOTH_SCAN, SCAN_BLUETOOTH_REQUEST_CODE)
        }
    }

    private fun requestBluetoothConnect() {
        if (!isBluetoothConnectGranted) {
            showExplanation("Bluetooth connect permission", "", Manifest.permission.BLUETOOTH_CONNECT, CONNECT_BLUETOOTH_REQUEST_CODE)
        }
    }

    var scanning = false
    val handler = Handler()

    private val leScanCallback: ScanCallback = object : ScanCallback() {

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result != null) {
                with(result.device) {
                    //afficherToast("Found BLE device with name: ${name ?: "No name"}, address: $address")
                    if (name == "PIR-IDS") {
                        //afficherToast("Found BLE device with name: ${name ?: "No name"}, address: $address")
                        buttonConnect.isEnabled = true
                        scanning = false
                        idsDevice = this
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    public fun scanLE() {
        requestLocationPermission()
        requestBluetoothScan()
        requestBluetoothConnect()
        /*val filter = ScanFilter.Builder().setServiceUuid(
            ParcelUuid.fromString()
        )*/
        if (!scanning) {
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
            afficherToast("scanning for LE devices")
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
            afficherToast("stopping LE devices scan")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

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
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                Log.w("BluetoothGattCallback", "Discovered ${services.size} services for ${device.address}")
                printGattTable()

                timeService = bluetoothGatt.getService(UUID.fromString("00001805-0000-1000-8000-00805f9b34fb"))
                Log.i("time", "Time service found $timeService")

                val currentTimeMillis = System.currentTimeMillis()
                Log.i("time", currentTimeMillis.toString())

                val dateServiceUUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
                val timeCharacteristic = timeService.getCharacteristic(dateServiceUUID)
                writeCharacteristic(timeCharacteristic, currentTimeMillis.toString().toByteArray())
            }
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

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }

        bluetoothGatt.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            gatt.writeCharacteristic(characteristic)
        }

        Log.i("Characteristic", "Sent datetime")
    }

    // TODO
    @SuppressLint("MissingPermission")
    fun connect() {
        bluetoothGatt = idsDevice.connectGatt(mContext, false, gattCallback)

        //afficherToast("device connected" + bluetoothGatt.device)


        // connect to time service

        // send date before discovering service
        //val dateServiceUUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
        //val currentTimeMillis = System.currentTimeMillis()
        //val timeCharacteristic = timeService.getCharacteristic(dateServiceUUID)
        //writeCharacteristic(timeCharacteristic, currentTimeMillis.toString().toByteArray())

        //Log.i("BluetoothGattDiscoverServices", "Discovering gatt services")
    }

    fun afficherToast(message: String) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
    }
}