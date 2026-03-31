package com.markduenas.homesteader.core.designsystem.components

import androidx.compose.runtime.Composable

/**
 * Full-screen crop dialog. Shows the picked image with pinch-zoom and pan;
 * user positions the circular crop area then taps "Use Photo".
 * [onCropComplete] is called with the URI of the saved cropped image.
 */
@Composable
expect fun ImageCropDialog(
    sourceUri: String,
    onCropComplete: (croppedUri: String) -> Unit,
    onDismiss: () -> Unit
)
