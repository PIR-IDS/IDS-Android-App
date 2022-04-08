package fr.pirids.idsapp.controller.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection

class IDSActivity : AppCompatActivity() {

    lateinit var buttonSetupBt : Button
    lateinit var buttonScan : Button
    lateinit var buttonConnect : Button

    lateinit var bluetoothConnection : BluetoothConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_idsactivity)

        buttonSetupBt = findViewById(R.id.setup_bt_button)
        buttonSetupBt.setOnClickListener { bluetoothConnection.setUpBT() }

        buttonScan = findViewById(R.id.scan_device_button)
        buttonScan.setOnClickListener { bluetoothConnection.scanLE() }

        buttonScan.isEnabled = false

        buttonConnect = findViewById(R.id.connect_device_button)
        buttonConnect.setOnClickListener { bluetoothConnection.connect() }

        buttonConnect.isEnabled = false

        bluetoothConnection = BluetoothConnection(this, this, buttonScan, buttonConnect)
    }
}