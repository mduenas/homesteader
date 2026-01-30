package com.markduenas.homesteader.core.util

import android.content.Intent
import android.net.Uri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.content.Context

actual fun openUrl(url: String) {
    UrlLauncherHelper.openUrl(url)
}

object UrlLauncherHelper : KoinComponent {
    private val context: Context by inject()

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
