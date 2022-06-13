package fr.pirids.idsapp.ui.main

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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.api.IzlyApi
import fr.pirids.idsapp.controller.bluetooth.BluetoothConnection
import java.time.*
import java.util.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.TabRow
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.pager.*
import fr.pirids.idsapp.model.items.Device
import fr.pirids.idsapp.model.items.Service
import fr.pirids.idsapp.model.view.TabItem
import fr.pirids.idsapp.ui.theme.IDSAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), AdapterView.OnItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    lateinit var currentSelectedService : String

    lateinit var buttonIDS : Button
    lateinit var buttonRemoteService : Button
    lateinit var buttonStartDetection : Button
    lateinit var buttonTest : Button
    lateinit var buttonStopDetection : Button
    lateinit var serviceSpinner : Spinner

    val currentTime = System.currentTimeMillis()

    lateinit var notifBuilder : NotificationCompat.Builder

    var intrusion : Boolean = false

    var isDebug : Boolean = false

    var serviceTransactionsTime = mutableListOf<Long>()
    var idsWalletOutTimeArray = mutableListOf<ZonedDateTime>()

    val izly = IzlyApi()

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val bluetoothConnection = BluetoothConnection(this, this)

    var serviceCredentials = mutableListOf<String>()

    var isServiceConnected = false
    var isIDSConnected = false

    val CHECKING_DELAY_MILLIS = 10000L

    val TIME_TOL = 10000.0

    val CHANNEL_ID = "ids"

    var notificationId = 0

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IDSAppTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = { TopBar() }
                    ) {
                        TextTabs(
                            modifier = Modifier
                                .padding(top = it.calculateTopPadding())
                        )
                    }
                }
            }
        }
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
    }
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
    private fun triggerNotification(serviceTime: Long) {
        val time = Instant.ofEpochMilli(serviceTime).atZone(ZoneId.of("UTC")).withZoneSameInstant(TimeZone.getDefault().toZoneId())
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(
                notificationId,
                notifBuilder.setContentText("at $time").build()
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    IDSAppTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = { TopBar() }
            ) {
                TextTabs(
                    modifier = Modifier
                        .padding(top = it.calculateTopPadding())
                )
            }
        }
    }
}

@Composable
fun TopBar() {
    TopAppBar(
        title = { Image(painter = painterResource(R.drawable.ids_logo), contentDescription = "") },
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    )
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TextTabs(modifier: Modifier) {
    val tabs = listOf(
        TabItem.Services,
        TabItem.Devices,
        TabItem.Network
    )
    val pagerState = rememberPagerState()
    Column(modifier = modifier) {
        Tabs(tabs = tabs, pagerState = pagerState)
        TabsContent(tabs = tabs, pagerState = pagerState)
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                icon = { Icon(tab.icon, contentDescription = "") },
                text = { Text(stringResource(id = tab.title).uppercase()) },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabsContent(tabs: List<TabItem>, pagerState: PagerState) {
    HorizontalPager(state = pagerState, count = tabs.size) { page ->
        tabs[page].screen()
    }
}

@Composable
fun ServicesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.tab_text_services),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 35.sp
        )
        FlowRow(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Service.list.forEach {
                Box(
                    modifier = Modifier
                        .size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = it.logo),
                        contentDescription = it.name,
                        //contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = true,
                                onClickLabel = "Clickable image",
                                onClick = {
                                    Log.i("MainActivity", "Clicked on ${it.name}")
                                }
                            )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AddCircle,
                    contentDescription = stringResource(id = R.string.add_service),
                    //contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = true,
                            onClickLabel = "Clickable image",
                            onClick = {
                                Log.i("MainActivity", "Clicked on ADD SERVICE")
                            }
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServicesScreenPreview() {
    ServicesScreen()
}

@Composable
fun DevicesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.tab_text_devices),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 35.sp
        )
        FlowRow(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            listOf(
                Device(1, "PIR-IDS", R.string.app_name, R.drawable.ids_logo),
            ).forEach {
                Box(
                    modifier = Modifier
                        .size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = it.logo),
                        contentDescription = it.name,
                        //contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = true,
                                onClickLabel = "Clickable image",
                                onClick = {
                                    Log.i("MainActivity", "Clicked on ${it.name}")
                                }
                            )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AddCircle,
                    contentDescription = stringResource(id = R.string.add_device),
                    //contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = true,
                            onClickLabel = "Clickable image",
                            onClick = {
                                Log.i("MainActivity", "Clicked on ADD DEVICE")
                            }
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DevicesScreenPreview() {
    DevicesScreen()
}

@Composable
fun NetworkScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.tab_text_network),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
            fontSize = 35.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NetworkScreenPreview() {
    NetworkScreen()
}