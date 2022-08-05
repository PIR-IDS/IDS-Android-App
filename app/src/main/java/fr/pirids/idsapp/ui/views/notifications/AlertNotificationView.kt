package fr.pirids.idsapp.ui.views.notifications

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import fr.pirids.idsapp.R
import fr.pirids.idsapp.data.navigation.NavRoutes
import fr.pirids.idsapp.ui.MainActivity

@Composable
fun AlertNotificationView() {
    val context = LocalContext.current
    val activity = (context as? Activity)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.error
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.padding(top = 100.dp, start = 16.dp, end = 16.dp)
            ) {
                Image(
                    modifier = Modifier
                        .size(width = 100.dp, height = 100.dp),
                    painter = painterResource(id = R.drawable.ids_logo_flat),
                    contentDescription = stringResource(id = R.string.app_name),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onError),
                    alpha = 0.5f
                )
            }
            Text(
                modifier = Modifier.wrapContentHeight().padding(start = 16.dp, end = 16.dp),
                text = stringResource(id = R.string.alert_notify),
                color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(90.dp))
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        val notifIntent = Intent(
                            Intent.ACTION_MAIN,
                            //TODO: set a link to the exact notification linked to the detection
                            NavRoutes.Notification.deepLink.toUri(),
                            context,
                            MainActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        }
                        notifIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                        activity?.startActivity(notifIntent)
                        activity?.finish()
                    },
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.height(90.dp).widthIn(220.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        modifier = Modifier.wrapContentHeight(),
                        text = stringResource(id = R.string.show_details).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.padding(bottom = 100.dp)
            ) {
                Button(
                    onClick = { activity?.finish() },
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.height(70.dp).widthIn(120.dp).alpha(0.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                ) {
                    Text(
                        modifier = Modifier.wrapContentHeight(),
                        text = stringResource(id = R.string.dismiss),
                        style = MaterialTheme.typography.titleLarge,
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
    AlertNotificationView()
}