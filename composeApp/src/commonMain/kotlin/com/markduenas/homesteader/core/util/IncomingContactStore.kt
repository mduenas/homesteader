package com.markduenas.homesteader.core.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton that bridges platform-level share intents/file opens to the Compose UI.
 * Platform code (MainActivity, iOSApp.swift) sets a raw vCard string here.
 * App.kt observes it and shows the ContactImportDialog when non-null.
 */
object IncomingContactStore {
    private val _pendingVCard = MutableStateFlow<String?>(null)
    val pendingVCard: StateFlow<String?> = _pendingVCard.asStateFlow()

    fun setPending(vcard: String) {
        _pendingVCard.value = vcard
    }

    fun clear() {
        _pendingVCard.value = null
    }
}
