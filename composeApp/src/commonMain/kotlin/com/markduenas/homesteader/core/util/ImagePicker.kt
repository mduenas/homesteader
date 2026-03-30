package com.markduenas.homesteader.core.util

import androidx.compose.runtime.Composable

/**
 * Returns a lambda that, when invoked, launches the platform image picker.
 * [onImageSelected] is called with the URI string of the chosen image, or null if cancelled.
 */
@Composable
expect fun rememberImagePickerLauncher(onImageSelected: (uri: String?) -> Unit): () -> Unit
