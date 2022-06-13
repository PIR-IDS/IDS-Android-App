package fr.pirids.idsapp.ui.main


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import fr.pirids.idsapp.R

class IzlyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {

        }

        /*findViewById<Button>(R.id.izly_connect_button).setOnClickListener() {
            val resultIntent = Intent()
            resultIntent.putExtra("phone_number", findViewById<EditText>(R.id.izly_phone_input).text.toString())
            resultIntent.putExtra("password", findViewById<EditText>(R.id.izly_password_input).text.toString())
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }*/
    }
}