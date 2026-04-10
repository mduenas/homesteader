package com.markduenas.homesteader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.markduenas.homesteader.core.util.IncomingContactStore
import com.markduenas.homesteader.domain.monetization.BillingService
import com.markduenas.homesteader.domain.monetization.GooglePlayBillingService
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val billingService: BillingService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleShareIntent(intent)
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val type = intent.type ?: return
        if (!type.contains("vcard", ignoreCase = true)) return

        // Try stream URI first, fall back to inline text
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        if (uri != null) {
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val vcard = stream.bufferedReader().readText()
                    if (vcard.isNotBlank()) IncomingContactStore.setPending(vcard)
                }
            } catch (_: Exception) {}
        } else {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) IncomingContactStore.setPending(text)
        }
    }

    override fun onResume() {
        super.onResume()
        // Set activity reference for billing flow
        (billingService as? GooglePlayBillingService)?.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        // Clear activity reference
        (billingService as? GooglePlayBillingService)?.setActivity(null)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}