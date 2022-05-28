package fr.pirids.idsapp.controller.view

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.postDelayed
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import java.time.OffsetDateTime
import java.util.*


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    lateinit var currentSelectedService : String

    lateinit var buttonIDS : Button
    lateinit var buttonRemoteService : Button
    lateinit var buttonStartDetection : Button
    lateinit var buttonStopDetection : Button
    lateinit var serviceSpinner : Spinner

    val currentTime = System.currentTimeMillis()

    var intrusion : Boolean = false

    var isDebug : Boolean = true

    var serviceTransactionsTime = mutableListOf<Long>()
    var idsWalletOutTimeArray = mutableListOf<OffsetDateTime>()

    val izly = IzlyApi()

    //val text = findViewById<TextView>(R.id.textView)

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val bluetoothConnection = BluetoothConnection(this, this)

    var serviceCredentials = mutableListOf<String>()

    var isServiceConnected = false
    var isIDSConnected = false

    val CHECKING_DELAY_MILLIS = 10000L

    val TIME_TOL = 10000.0

    val CHANNEL_ID = "ids"

    var notificationId = 0

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
            bluetoothConnection.setUpBT()
            isIDSConnected = true
            if (isServiceConnected) {
                findViewById<Button>(R.id.start_detection_button).isEnabled = true
            }
        }

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "IDS"
            val descriptionText = "Intrusion detected"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val handler = Handler(Looper.getMainLooper())

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // notification push
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("IDS detected an intrusion")
            .setContentText("")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)

        lateinit var loopingThd : Runnable
        loopingThd = Runnable {
            //Log.d("DETECTION", "walletOut times: ${bluetoothConnection.whenWalletOutArray}")

            // detection
            // non opti ?
            idsWalletOutTimeArray = bluetoothConnection.whenWalletOutArray
            Log.d("DETECTION", "walletout times: $idsWalletOutTimeArray")

            //serviceTransactionsTime = izly.getTransactionList(serviceCredentials.get(0), serviceCredentials.get(1))
            Thread {
                serviceTransactionsTime = izly.getTransactionList(serviceCredentials[0], serviceCredentials[1])
                Log.d("DETECTION","IZLY transactions: $serviceTransactionsTime")
            }.start()

            // checking
            serviceTransactionsTime.forEach { serviceTime ->
                if (serviceTime > currentTime || isDebug) {
                    intrusion = true
                    isDebug = false
                    // see below
                    idsWalletOutTimeArray.forEach { idsTime ->
                        var idsDate = idsTime.toInstant().toEpochMilli()
                        val diff = Math.abs(serviceTime - idsDate)
                        if (diff < TIME_TOL) {
                            intrusion = false
                        }
                    }

                    if (intrusion) {
                        with(NotificationManagerCompat.from(this)) {
                            // notificationId is a unique int for each notification that you must define
                            notify(notificationId, builder.setContentText("at $serviceTime").build())
                            notificationId++
                        }
                    }
                }
            }

            handler.postDelayed(loopingThd, CHECKING_DELAY_MILLIS)
        }

        buttonStartDetection = findViewById<Button>(R.id.start_detection_button)
        buttonStopDetection = findViewById<Button>(R.id.stop_detection_button)

        buttonStartDetection.setOnClickListener {

            if (currentSelectedService.equals("IZLY")) {
                buttonStopDetection.isEnabled = true
                buttonStartDetection.isEnabled = false

                loopingThd.run()
            }
        }

        buttonStopDetection.setOnClickListener {
            handler.removeCallbacks(loopingThd)
            buttonStartDetection.isEnabled = true
            buttonStopDetection.isEnabled = false
        }
    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            serviceCredentials.add(result.data?.getStringExtra("phone_number").toString())
            serviceCredentials.add(result.data?.getStringExtra("password").toString())
            //findViewById<Button>(R.id.start_detection_button).isEnabled = true
            isServiceConnected = true
            if (isIDSConnected) {
                findViewById<Button>(R.id.start_detection_button).isEnabled = true
            }
        }
    }

    fun startServiceActivity(view: View) {
        if (currentSelectedService.equals("IZLY")) {
            val intent = Intent(this@MainActivity, IzlyActivity::class.java)
            //startActivity(intent)
            resultLauncher.launch(intent)
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        currentSelectedService = p0?.getItemAtPosition(p2) as String
        buttonRemoteService.isEnabled = true
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        buttonRemoteService.isEnabled = false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        bluetoothConnection.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}