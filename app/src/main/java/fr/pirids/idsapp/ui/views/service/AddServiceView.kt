package fr.pirids.idsapp.ui.views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun AddServiceView(navController: NavHostController) {
    Text("Izly")

    /*findViewById<Button>(R.id.izly_connect_button).setOnClickListener() {
        val resultIntent = Intent()
        resultIntent.putExtra("phone_number", findViewById<EditText>(R.id.izly_phone_input).text.toString())
        resultIntent.putExtra("password", findViewById<EditText>(R.id.izly_password_input).text.toString())
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }*/
}