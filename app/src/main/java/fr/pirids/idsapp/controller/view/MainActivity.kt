package fr.pirids.idsapp.controller.view

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val REQUEST_CODE_ENABLE_BT: Int = 1

    lateinit var currentSelectedService : String

    lateinit var buttonIDS : Button
    lateinit var buttonRemoteService : Button
    lateinit var serviceSpinner : Spinner

    //val text = findViewById<TextView>(R.id.textView)

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val bluetoothConnection = BluetoothConnection(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonRemoteService = findViewById(R.id.remote_service_button)

        serviceSpinner = findViewById(R.id.remote_service_spinner)

        ArrayAdapter.createFromResource(
            this,
            R.array.remote_services,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            serviceSpinner.adapter = adapter
        }

        currentSelectedService = resources.getStringArray(R.array.remote_services).get(0)

        serviceSpinner.onItemSelectedListener = this

        buttonIDS = findViewById(R.id.ids_button)
        buttonIDS.setOnClickListener {
            /*
            val intent = Intent(this, IDSActivity::class.java)
            startActivity(intent)
            */

            //val bluetoothConnection = BluetoothConnection(this, this)
            bluetoothConnection.setUpBT()
        }
    }

    fun startServiceActivity(view: View) {
        if (currentSelectedService.equals("IZLY")) {
            val intent = Intent(this@MainActivity, IzlyActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        currentSelectedService = p0?.getItemAtPosition(p2) as String
        buttonRemoteService.isEnabled = true
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        buttonRemoteService.isEnabled = false
    }
}