package fr.pirids.idsapp


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val value = Javanese()
        Thread({ value.main() }).start()


      /*  var et_user_name = findViewById(R.id.et_user_name) as EditText
        var et_password = findViewById(R.id.et_password) as EditText
        var btn_reset = findViewById(R.id.btn_reset) as Button
        var btn_submit = findViewById(R.id.btn_submit) as Button

        btn_reset.setOnClickListener {
            et_user_name.setText("")
            et_password.setText("")
        }


        btn_submit.setOnClickListener {
            val user_name = et_user_name.text;
            //val password = et_password.text;
           //Toast.makeText(this@MainActivity, user_name, Toast.LENGTH_LONG).show()
            Log.d("username",user_name.toString())
        }*/
    }
}