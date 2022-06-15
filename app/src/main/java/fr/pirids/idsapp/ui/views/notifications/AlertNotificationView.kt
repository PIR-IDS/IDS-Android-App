package fr.pirids.idsapp.ui.views.notifications

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import fr.pirids.idsapp.R

@Composable
fun AlertNotificationView() {
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

@Preview(showBackground = true)
@Composable
fun AlertNotificationPreview() {
    AlertNotificationView()
}