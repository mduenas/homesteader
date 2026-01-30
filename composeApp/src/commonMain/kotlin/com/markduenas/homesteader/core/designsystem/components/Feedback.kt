package com.markduenas.homesteader.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Types of feedback messages.
 */
enum class FeedbackType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

/**
 * Data class for feedback messages.
 */
data class FeedbackMessage(
    val message: String,
    val type: FeedbackType = FeedbackType.INFO,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val durationMillis: Long = 3000
)

/**
 * A toast-like feedback component that slides in from the bottom.
 */
@Composable
fun FeedbackToast(
    message: FeedbackMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        if (message != null) {
            isVisible = true
            delay(message.durationMillis)
            isVisible = false
            delay(300) // Wait for animation
            onDismiss()
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = isVisible && message != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            message?.let { msg ->
                FeedbackContent(
                    message = msg,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FeedbackContent(
    message: FeedbackMessage,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (message.type) {
        FeedbackType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        FeedbackType.ERROR -> MaterialTheme.colorScheme.errorContainer
        FeedbackType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        FeedbackType.INFO -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when (message.type) {
        FeedbackType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        FeedbackType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        FeedbackType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        FeedbackType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )

            if (message.actionLabel != null && message.onAction != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = message.onAction) {
                    Text(
                        text = message.actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}

/**
 * A banner-style feedback for persistent messages.
 */
@Composable
fun FeedbackBanner(
    message: String,
    type: FeedbackType,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        FeedbackType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        FeedbackType.ERROR -> MaterialTheme.colorScheme.errorContainer
        FeedbackType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        FeedbackType.INFO -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when (type) {
        FeedbackType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        FeedbackType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        FeedbackType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        FeedbackType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )

            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Dismiss",
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor
                    )
                }
            }
        }
    }
}

/**
 * Offline indicator banner.
 */
@Composable
fun OfflineBanner(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Text(
                text = "You're offline. Changes will sync when connected.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
