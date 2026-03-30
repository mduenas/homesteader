package com.markduenas.homesteader.core.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePickerLauncher(onImageSelected: (uri: String?) -> Unit): () -> Unit {
    // iOS implementation placeholder — full PHPicker support coming in a future release
    return {}
}
