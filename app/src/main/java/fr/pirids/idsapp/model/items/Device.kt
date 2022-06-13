package fr.pirids.idsapp.model.items

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Device(val id: Int, val name: String, @StringRes val description: Int, @DrawableRes val logo: Int)
