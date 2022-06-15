package fr.pirids.idsapp.controller.view

import android.bluetooth.BluetoothAdapter
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavHostController
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import fr.pirids.idsapp.model.items.Service
import fr.pirids.idsapp.model.navigation.NavRoutes
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

object HomeViewController /*: ActivityCompat.OnRequestPermissionsResultCallback, AdapterView.OnItemSelectedListener*/ {

    lateinit var currentSelectedService : String

    /*lateinit var buttonIDS : Button
    lateinit var buttonRemoteService : Button
    lateinit var buttonStartDetection : Button
    lateinit var buttonTest : Button
    lateinit var buttonStopDetection : Button
    lateinit var serviceSpinner : Spinner*/

    val currentTime = System.currentTimeMillis()

    //lateinit var notifBuilder : NotificationCompat.Builder

    var intrusion : Boolean = false

    var isDebug : Boolean = false

    var serviceTransactionsTime = mutableListOf<Long>()
    var idsWalletOutTimeArray = mutableListOf<ZonedDateTime>()

    val izly = IzlyApi()

    private lateinit var bluetoothAdapter: BluetoothAdapter

    //private val bluetoothConnection = BluetoothConnection(this, this)

    var serviceCredentials = mutableListOf<String>()

    var isServiceConnected = false
    var isIDSConnected = false

    val CHECKING_DELAY_MILLIS = 10000L

    val TIME_TOL = 10000.0

    val CHANNEL_ID = "ids"

    var notificationId = 0

    /*
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

            buttonStartDetection = findViewById<Button>(R.id.start_detection_button)
            buttonStopDetection = findViewById<Button>(R.id.stop_detection_button)
            buttonTest = findViewById<Button>(R.id.test_button)

            buttonIDS = findViewById(R.id.ids_button)
            buttonIDS.setOnClickListener {
                bluetoothConnection.setUpBT()
                isIDSConnected = true
                if (isServiceConnected) {
                    buttonStartDetection.isEnabled = true
                }
            }

            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
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

            val mainHandler = Handler(Looper.getMainLooper())

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            intent.setAction(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val fullScreenIntent = Intent(this, AlertNotificationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(this, 1, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            // notification push
            notifBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(resources.getString(R.string.alert_notify))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)

            val loopingThd : Runnable
            loopingThd = object : Runnable {
                override fun run() {
                    //Log.d("DETECTION", "walletOut times: ${bluetoothConnection.whenWalletOutArray}")

                    //triggerNotification(System.currentTimeMillis())
                    // detection
                    // non opti ?
                    idsWalletOutTimeArray = bluetoothConnection.whenWalletOutArray
                    Log.d("DETECTION", "walletout times: $idsWalletOutTimeArray")

                    //serviceTransactionsTime = izly.getTransactionList(serviceCredentials.get(0), serviceCredentials.get(1))
                    Thread {
                        serviceTransactionsTime.addAll(izly.getTransactionList(serviceCredentials[0], serviceCredentials[1]))
                        Log.d("DETECTION", "IZLY transactions: $serviceTransactionsTime")
                    }.start()

                    // checking
                    serviceTransactionsTime.forEach { serviceTime ->
                        if (serviceTime > currentTime || isDebug) {
                            intrusion = true
                            isDebug = false
                            idsWalletOutTimeArray.forEach { idsTime ->
                                val idsDate = idsTime.toInstant().toEpochMilli()
                                Log.d("DETECTION", "ids time $idsDate")
                                val diff = Math.abs(serviceTime - idsDate)
                                if (diff < TIME_TOL) {
                                    intrusion = false
                                }
                            }

                            if (intrusion) {
                                triggerNotification(serviceTime)
                            }
                        }
                    }
                    mainHandler.postDelayed(this, CHECKING_DELAY_MILLIS)
                }
            }

            buttonStartDetection.setOnClickListener {
                if (currentSelectedService.equals("IZLY")) {
                    buttonStopDetection.isEnabled = true
                    buttonStartDetection.isEnabled = false
                    mainHandler.post(loopingThd)
                }
            }

            buttonStopDetection.setOnClickListener {
                mainHandler.removeCallbacks(loopingThd)
                buttonStartDetection.isEnabled = true
                buttonStopDetection.isEnabled = false
            }
            buttonTest.setOnClickListener {
                serviceTransactionsTime.add(System.currentTimeMillis())
            }*/
    /*
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
    */
        /*private fun triggerNotification(serviceTime: Long) {
        val time = Instant.ofEpochMilli(serviceTime).atZone(ZoneId.of("UTC")).withZoneSameInstant(
            TimeZone.getDefault().toZoneId())
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(
                notificationId,
                //notifBuilder.setContentText("at $time").build()
            )
            notificationId++
        }
    }
    /*
        fun startServiceActivity(view: View) {
            if (currentSelectedService.equals("IZLY")) {
                val intent = Intent(this@MainActivity, IzlyActivity::class.java)
                //startActivity(intent)
                resultLauncher.launch(intent)
            }
        }
    */
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        currentSelectedService = p0?.getItemAtPosition(p2) as String
        //buttonRemoteService.isEnabled = true
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        //buttonRemoteService.isEnabled = false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //bluetoothConnection.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
*/
    fun addService(navController: NavHostController) {
        navController.navigate(NavRoutes.AddService.route)
    }

    fun showService(navController: NavHostController, service: Service) {

        navController.navigate(NavRoutes.Service.route)
    }
}