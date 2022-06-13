package fr.pirids.idsapp.model.items

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import fr.pirids.idsapp.R

data class Service(val id: Int, val name: String, @StringRes val description: Int, @DrawableRes val logo: Int) {
    companion object {
        val list = listOf(
            Service(1, "IZLY", R.string.izly_description, R.drawable.izly_logo)
        )
    }
}
