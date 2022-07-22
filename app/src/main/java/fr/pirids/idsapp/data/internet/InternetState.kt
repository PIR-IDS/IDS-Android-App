package fr.pirids.idsapp.data.internet

sealed interface InternetState {
    object Available : InternetState
    object Unavailable : InternetState
}