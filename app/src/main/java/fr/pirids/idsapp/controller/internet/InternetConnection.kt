package fr.pirids.idsapp.controller.internet

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import fr.pirids.idsapp.data.internet.InternetState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class InternetConnection(private val mContext: Context) {
    val connectivityState: InternetState = getCurrentConnectivityState(mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

    @Composable
    fun dynamicConnectivityState(): State<InternetState> {
        // Creates a State<ConnectionState> with current connectivity state as initial value
        return produceState(initialValue = connectivityState) {
            observeConnectivityAsFlow().collect { value = it }
        }
    }

    private fun observeConnectivityAsFlow() = callbackFlow {
        val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = networkCallback { connectionState -> trySend(connectionState) }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        val currentState = getCurrentConnectivityState(connectivityManager)
        trySend(currentState)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    private fun getCurrentConnectivityState(connectivityManager: ConnectivityManager): InternetState {
        val connected = connectivityManager.allNetworks.any { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                ?: false
        }

        return if (connected) InternetState.Available else InternetState.Unavailable
    }

    private fun networkCallback(callback: (InternetState) -> Unit): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                callback(InternetState.Available)
            }

            override fun onLost(network: Network) {
                callback(InternetState.Unavailable)
            }
        }
    }
}