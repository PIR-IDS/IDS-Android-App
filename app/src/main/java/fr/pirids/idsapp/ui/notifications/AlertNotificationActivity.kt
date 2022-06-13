package fr.pirids.idsapp.ui.notifications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import fr.pirids.idsapp.R
import fr.pirids.idsapp.ui.main.TopBar
import fr.pirids.idsapp.ui.theme.IDSAppTheme

class AlertNotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IDSAppTheme {
                Surface(
                    color = Color.Red
                ) {
                    Text(
                        text = stringResource(id = R.string.alert_notify),
                        color = Color.White,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertNotificationPreview() {
    Surface(
        color = Color.Red
    ) {
        Text(
            text = stringResource(id = R.string.alert_notify),
            color = Color.White,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}