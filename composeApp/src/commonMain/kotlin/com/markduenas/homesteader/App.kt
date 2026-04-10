package com.markduenas.homesteader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.markduenas.homesteader.core.designsystem.HomesteaderTheme
import com.markduenas.homesteader.core.designsystem.components.LoadingIndicator
import com.markduenas.homesteader.core.util.IncomingContactStore
import com.markduenas.homesteader.core.util.VCardParser
import com.markduenas.homesteader.data.repository.SpeciesConfigRepository
import com.markduenas.homesteader.feature.customers.ContactImportDialog
import com.markduenas.homesteader.feature.main.MainScreen
import com.markduenas.homesteader.feature.setup.SpeciesSetupScreen
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun App() {
    HomesteaderTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    val speciesConfigRepository: SpeciesConfigRepository = koinInject()
    var startScreen by remember { mutableStateOf<Screen?>(null) }

    // Observe incoming shared contacts
    val pendingVCard by IncomingContactStore.pendingVCard.collectAsState()

    LaunchedEffect(Unit) {
        // Initialize default configs if needed
        speciesConfigRepository.initializeDefaultConfigs()

        // Check if any species are enabled
        val enabledConfigs = speciesConfigRepository.getEnabledConfigs().first()

        startScreen = if (enabledConfigs.isEmpty()) {
            // First launch - show setup wizard
            SpeciesSetupScreen()
        } else {
            // Already setup - go to main screen with bottom nav
            MainScreen()
        }
    }

    if (startScreen != null) {
        Navigator(startScreen!!) { navigator ->
            SlideTransition(navigator)
        }
    } else {
        // Show loading while determining start screen
        LoadingIndicator()
    }

    // Show contact import dialog when a vCard is shared into the app
    pendingVCard?.let { vcard ->
        val contact = remember(vcard) { VCardParser.parse(vcard) }
        if (contact != null) {
            ContactImportDialog(contact = contact)
        } else {
            // Couldn't parse the vCard — clear it silently
            IncomingContactStore.clear()
        }
    }
}
