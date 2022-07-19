package fr.pirids.idsapp.data.device.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

abstract class DeviceData {
    @get:StringRes abstract val intrusionTitle: Int
    @get:StringRes abstract val intrusionMessage: Int

    @get:StringRes abstract val dataTitle: Int
    @get:StringRes abstract val dataMessage: Int
    abstract val eventIcon: ImageVector
}
