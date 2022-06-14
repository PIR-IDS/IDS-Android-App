package fr.pirids.idsapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import fr.pirids.idsapp.controller.navigation.IDSApp
import fr.pirids.idsapp.ui.theme.IDSAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IDSAppTheme {
                IDSApp()
            }
        }
    }
}