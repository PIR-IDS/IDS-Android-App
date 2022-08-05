package fr.pirids.idsapp.ui.views.notifications

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import fr.pirids.idsapp.ui.theme.IDSAlertTheme

/**
 * This activity is only used to display a fullscreen notification on the lock screen.
 */
class AlertNotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We display the activity on the lock screen.
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
            requestDismissKeyguard(this@AlertNotificationActivity, null)
        }

        setContent {
            IDSAlertTheme {
                AlertNotificationView()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setShowWhenLocked(false)
        setTurnScreenOn(false)
    }
}