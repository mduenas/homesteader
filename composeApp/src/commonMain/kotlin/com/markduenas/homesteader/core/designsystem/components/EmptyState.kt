package com.markduenas.homesteader.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Empty state component shown when lists have no data.
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    icon: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.size(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }

        if (secondaryActionLabel != null && onSecondaryAction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onSecondaryAction) {
                Text(secondaryActionLabel)
            }
        }
    }
}

/**
 * Predefined empty states for common scenarios.
 */
object EmptyStates {
    @Composable
    fun NoAnimals(
        onAddAnimal: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        EmptyState(
            title = "No Animals Yet",
            message = "Start building your herd by adding your first animal.",
            actionLabel = "Add Animal",
            onAction = onAddAnimal,
            modifier = modifier
        )
    }

    @Composable
    fun NoEvents(
        animalName: String,
        onAddEvent: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        EmptyState(
            title = "No Events",
            message = "No events recorded for $animalName yet. Track health, breeding, and more.",
            actionLabel = "Add Event",
            onAction = onAddEvent,
            modifier = modifier
        )
    }

    @Composable
    fun NoTasks(
        onRefresh: (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        EmptyState(
            title = "All Caught Up!",
            message = "No tasks or reminders for today. Great job staying on top of things!",
            actionLabel = if (onRefresh != null) "Refresh" else null,
            onAction = onRefresh,
            modifier = modifier
        )
    }

    @Composable
    fun NoSearchResults(
        searchQuery: String,
        onClearSearch: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        EmptyState(
            title = "No Results",
            message = "No animals match \"$searchQuery\". Try a different search term.",
            actionLabel = "Clear Search",
            onAction = onClearSearch,
            modifier = modifier
        )
    }

    @Composable
    fun NoReports(
        modifier: Modifier = Modifier
    ) {
        EmptyState(
            title = "No Data for Report",
            message = "There isn't enough data to generate this report yet. Add more animals and events first.",
            modifier = modifier
        )
    }

    @Composable
    fun Error(
        message: String,
        onRetry: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        EmptyState(
            title = "Something Went Wrong",
            message = message,
            actionLabel = "Try Again",
            onAction = onRetry,
            modifier = modifier
        )
    }
}
