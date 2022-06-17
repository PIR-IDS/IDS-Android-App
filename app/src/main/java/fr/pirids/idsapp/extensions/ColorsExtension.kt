package fr.pirids.idsapp.extensions

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color
import fr.pirids.idsapp.ui.theme.*

val Colors.custom_success: Color
    get() = if (isLight) custom_light_success else custom_dark_success

val Colors.custom_onSuccess: Color
    get() = if (isLight) custom_light_onSuccess else custom_dark_onSuccess