package com.markduenas.homesteader.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.markduenas.homesteader.feature.import.ImportScreen

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        SettingsContent(
            onNavigateBack = { navigator.pop() },
            onNavigateToImport = { navigator.push(ImportScreen()) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    onNavigateBack: () -> Unit,
    onNavigateToImport: () -> Unit
) {
    var useMetricUnits by remember { mutableStateOf(false) }
    var showDeceasedAnimals by remember { mutableStateOf(true) }
    var showSoldAnimals by remember { mutableStateOf(true) }
    var enableNotifications by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsSection(title = "Display")
            }

            item {
                SettingsSwitchItem(
                    title = "Use Metric Units",
                    subtitle = "Display weights in kg instead of lbs",
                    checked = useMetricUnits,
                    onCheckedChange = { useMetricUnits = it }
                )
            }

            item {
                SettingsSwitchItem(
                    title = "Show Deceased Animals",
                    subtitle = "Include deceased animals in lists",
                    checked = showDeceasedAnimals,
                    onCheckedChange = { showDeceasedAnimals = it }
                )
            }

            item {
                SettingsSwitchItem(
                    title = "Show Sold Animals",
                    subtitle = "Include sold animals in lists",
                    checked = showSoldAnimals,
                    onCheckedChange = { showSoldAnimals = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Notifications")
            }

            item {
                SettingsSwitchItem(
                    title = "Enable Notifications",
                    subtitle = "Receive reminders for tasks and events",
                    checked = enableNotifications,
                    onCheckedChange = { enableNotifications = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Species Configuration")
            }

            item {
                SettingsNavigationItem(
                    title = "Manage Species",
                    subtitle = "Configure tracked species and their settings",
                    onClick = { /* TODO: Navigate to species setup */ }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Data")
            }

            item {
                SettingsNavigationItem(
                    title = "Import Data",
                    subtitle = "Import animals from CSV file",
                    onClick = onNavigateToImport
                )
            }

            item {
                SettingsNavigationItem(
                    title = "Export All Data",
                    subtitle = "Export all data to CSV files",
                    onClick = { /* TODO: Implement full export */ }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun SettingsNavigationItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = ">",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
