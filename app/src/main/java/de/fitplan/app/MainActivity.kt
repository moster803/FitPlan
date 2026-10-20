package de.fitplan.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.fitplan.app.ui.FitPlanRoot
import de.fitplan.app.ui.LaunchRequest
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val launchRequests = MutableStateFlow<LaunchRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleIntent(intent)
        setContent {
            FitPlanRoot(
                container = container,
                launchRequests = launchRequests,
                onLaunchHandled = { launchRequests.value = null },
                onDarkThemeChanged = ::applySystemBars,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val open = intent.getStringExtra(EXTRA_OPEN) ?: return
        launchRequests.value = LaunchRequest(open, intent.getStringExtra(EXTRA_PLAN_ID))
    }

    /** Statusleisten-Symbole passend zum in der App gewählten Design einfärben. */
    private fun applySystemBars(dark: Boolean) {
        val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    companion object {
        const val EXTRA_OPEN = "open"
        const val EXTRA_PLAN_ID = "plan_id"
        const val OPEN_PLAYER = "player"
        const val OPEN_SUPPLEMENTS = "supplements"
    }
}
