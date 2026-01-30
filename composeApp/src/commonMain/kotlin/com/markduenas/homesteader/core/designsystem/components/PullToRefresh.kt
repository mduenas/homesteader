package com.markduenas.homesteader.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * State for pull-to-refresh functionality.
 */
data class PullToRefreshState(
    val isRefreshing: Boolean = false,
    val pullProgress: Float = 0f // 0 to 1, where 1 means threshold reached
)

/**
 * A simple pull-to-refresh indicator.
 * Note: Full pull-to-refresh gesture handling requires platform-specific implementation.
 * This provides the visual indicator component.
 */
@Composable
fun RefreshIndicator(
    state: PullToRefreshState,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (state.isRefreshing || state.pullProgress > 0.2f) 1f else 0f,
        label = "refresh_alpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (state.isRefreshing) 56f else (state.pullProgress * 56f),
        label = "refresh_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, offsetY.toInt()) }
            .alpha(alpha),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp
            )
        }
    }
}

/**
 * Container that shows refresh indicator when refreshing.
 */
@Composable
fun RefreshableContent(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}
