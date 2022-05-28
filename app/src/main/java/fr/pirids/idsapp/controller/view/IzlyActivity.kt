package fr.pirids.idsapp.controller.view


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import fr.pirids.idsapp.R
import fr.pirids.idsapp.controller.api.IzlyApi

class IzlyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_izly)

        findViewById<Button>(R.id.izly_connect_button).setOnClickListener() {
            val resultIntent = Intent()
            resultIntent.putExtra("phone_number", findViewById<EditText>(R.id.izly_phone_input).text.toString())
            resultIntent.putExtra("password", findViewById<EditText>(R.id.izly_password_input).text.toString())
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        /*
        findViewById<Button>(R.id.izly_connect_button).setOnClickListener {
            val value = IzlyApi()
            Thread {
                Log.d("IZLY",
                    value.getTransactionList(
                        findViewById<EditText>(R.id.izly_phone_input).text.toString(),
                        findViewById<EditText>(R.id.izly_password_input).text.toString()
                    ).toString()
                )
            }.start()
        }
        */

    }
}