package com.markduenas.homesteader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.markduenas.homesteader.domain.monetization.BillingService
import com.markduenas.homesteader.domain.monetization.GooglePlayBillingService
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val billingService: BillingService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
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