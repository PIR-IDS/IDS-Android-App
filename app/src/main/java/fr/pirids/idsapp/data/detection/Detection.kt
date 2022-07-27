package fr.pirids.idsapp.data.detection

import fr.pirids.idsapp.data.device.bluetooth.BluetoothDeviceIDS
import fr.pirids.idsapp.data.items.Service

data class Detection(val timestamp: Long, val service: Service, val connectedDevicesDuringDetection: List<BluetoothDeviceIDS>)