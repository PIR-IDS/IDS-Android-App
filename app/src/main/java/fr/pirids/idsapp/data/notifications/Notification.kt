package fr.pirids.idsapp.data.notifications

import androidx.annotation.StringRes
import fr.pirids.idsapp.data.items.Service

data class Notification(@StringRes val title: Int, @StringRes val message: Int, val timestamp: Long, val service: Service)