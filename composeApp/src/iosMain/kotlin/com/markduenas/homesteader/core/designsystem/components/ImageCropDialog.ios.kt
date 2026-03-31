package com.markduenas.homesteader.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun ImageCropDialog(
    sourceUri: String,
    onCropComplete: (croppedUri: String) -> Unit,
    onDismiss: () -> Unit
) {
    // iOS: no crop UI — pass URI through immediately
    LaunchedEffect(sourceUri) {
        onCropComplete(sourceUri)
    }
}
