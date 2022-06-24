package fr.pirids.idsapp.model.device.data

import java.time.ZonedDateTime

class WalletCardData(val whenWalletOutArray: MutableSet<ZonedDateTime> = mutableSetOf<ZonedDateTime>()) : DeviceData()