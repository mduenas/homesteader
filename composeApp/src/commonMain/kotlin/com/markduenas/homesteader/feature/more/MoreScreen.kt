package com.markduenas.homesteader.feature.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.markduenas.homesteader.core.util.AppUrls
import com.markduenas.homesteader.core.util.openUrl
import com.markduenas.homesteader.feature.about.AboutScreen
import com.markduenas.homesteader.feature.backup.BackupScreen
import com.markduenas.homesteader.feature.customers.CustomerListScreen
import com.markduenas.homesteader.feature.premium.PremiumScreen
import com.markduenas.homesteader.feature.reports.ReportsScreen
import com.markduenas.homesteader.feature.settings.SettingsScreen

class MoreScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        MoreContent(
            onNavigateToReports = { navigator.push(ReportsScreen()) },
            onNavigateToCustomers = { navigator.push(CustomerListScreen()) },
            onNavigateToSettings = { navigator.push(SettingsScreen()) },
            onNavigateToBackup = { navigator.push(BackupScreen()) },
            onNavigateToAbout = { navigator.push(AboutScreen()) },
            onNavigateToPremium = { navigator.push(PremiumScreen()) },
            onOpenPrivacyPolicy = { openUrl(AppUrls.PRIVACY_POLICY) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreContent(
    onNavigateToReports: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Data & Reports",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                MoreMenuItem(
                    icon = "R",
                    title = "Reports",
                    subtitle = "Generate inventory, breeding, health, and production reports",
                    onClick = onNavigateToReports
                )
            }

            item {
                MoreMenuItem(
                    icon = "👤",
                    title = "Customers",
                    subtitle = "Manage buyer profiles and purchase history",
                    onClick = onNavigateToCustomers
                )
            }

            item {
                MoreMenuItem(
                    icon = "B",
                    title = "Backup & Restore",
                    subtitle = "Create backups and restore your data",
                    onClick = onNavigateToBackup
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                MoreMenuItem(
                    icon = "S",
                    title = "Settings",
                    subtitle = "Configure species, units, and app preferences",
                    onClick = onNavigateToSettings
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "About",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                MoreMenuItem(
                    icon = "i",
                    title = "About Steady Hand",
                    subtitle = "Version info and support",
                    onClick = onNavigateToAbout
                )
            }

            item {
                MoreMenuItem(
                    icon = "P",
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    onClick = onOpenPrivacyPolicy
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))

                // Premium upgrade card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToPremium),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Remove Ads",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Support development and enjoy an ad-free experience with a one-time purchase.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$7.99 - One-time purchase",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

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
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
