package com.markduenas.homesteader.core.designsystem.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

@Composable
actual fun ImageCropDialog(
    sourceUri: String,
    onCropComplete: (croppedUri: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var userScale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imgSize by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val cropSizeDp = 280.dp
    val cropSizePx = with(density) { cropSizeDp.toPx() }

    // Load original image dimensions off the main thread
    LaunchedEffect(sourceUri) {
        withContext(Dispatchers.IO) {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(Uri.parse(sourceUri))?.use {
                BitmapFactory.decodeStream(it, null, opts)
            }
            if (opts.outWidth > 0 && opts.outHeight > 0) {
                imgSize = opts.outWidth to opts.outHeight
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val size = imgSize
            if (size == null) {
                CircularProgressIndicator(color = Color.White)
            } else {
                val (imgW, imgH) = size

                // Scale so image fills the crop circle at userScale = 1
                val baseScale = max(cropSizePx / imgW, cropSizePx / imgH)
                val totalScale = baseScale * userScale

                // Constrain pan so the image always covers the crop circle
                val maxOffX = ((imgW * totalScale / 2f) - cropSizePx / 2f).coerceAtLeast(0f)
                val maxOffY = ((imgH * totalScale / 2f) - cropSizePx / 2f).coerceAtLeast(0f)
                val constrainedOffset = Offset(
                    offset.x.coerceIn(-maxOffX, maxOffX),
                    offset.y.coerceIn(-maxOffY, maxOffY)
                )
                if (constrainedOffset != offset) offset = constrainedOffset

                // Gesture detector covering the full screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                userScale = (userScale * zoom).coerceIn(1f, 8f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Image: pre-sized to fill the crop area, extra zoom/pan via graphicsLayer
                    val scaledW = with(density) { (imgW * baseScale).toDp() }
                    val scaledH = with(density) { (imgH * baseScale).toDp() }
                    AsyncImage(
                        model = sourceUri,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .size(scaledW, scaledH)
                            .graphicsLayer {
                                scaleX = userScale
                                scaleY = userScale
                                translationX = constrainedOffset.x
                                translationY = constrainedOffset.y
                            }
                    )

                    // Dark overlay with circular window — clipPath is reliable on all devices
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val circlePath = Path().apply {
                            addOval(
                                Rect(
                                    center = center,
                                    radius = cropSizePx / 2f
                                )
                            )
                        }
                        // Draw dark overlay only OUTSIDE the circle
                        clipPath(circlePath, clipOp = ClipOp.Difference) {
                            drawRect(color = Color.Black.copy(alpha = 0.6f))
                        }
                        // Draw circle guide ring
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = cropSizePx / 2f,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Buttons anchored to bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Button(onClick = {
                            scope.launch {
                                isSaving = true
                                val result = cropAndSave(
                                    context, sourceUri, imgW, imgH,
                                    totalScale, constrainedOffset, cropSizePx
                                )
                                isSaving = false
                                if (result != null) onCropComplete(result) else onDismiss()
                            }
                        }) { Text("Use Photo") }
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun cropAndSave(
    context: Context,
    sourceUri: String,
    imgW: Int,
    imgH: Int,
    totalScale: Float,
    offset: Offset,
    cropSizePx: Float
): String? = withContext(Dispatchers.IO) {
    try {
        val uri = Uri.parse(sourceUri)

        // Downsample to ~1200px on the short side to avoid OOM
        val rawMin = min(imgW, imgH)
        val sampleSize = Integer.highestOneBit((rawMin / 1200).coerceAtLeast(1))
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return@withContext null

        val bmpW = bitmap.width.toFloat()
        val bmpH = bitmap.height.toFloat()

        // Adjust for the downsampling applied during load
        val adjustedScale = totalScale * (bmpW / imgW)

        // Find crop center and size in bitmap coordinates
        val cx = bmpW / 2f - offset.x / adjustedScale
        val cy = bmpH / 2f - offset.y / adjustedScale
        val halfSize = (cropSizePx / 2f) / adjustedScale

        val left = (cx - halfSize).coerceIn(0f, bmpW - 1f).toInt()
        val top = (cy - halfSize).coerceIn(0f, bmpH - 1f).toInt()
        val cropSize = (halfSize * 2f).toInt()
            .coerceAtMost(bitmap.width - left)
            .coerceAtMost(bitmap.height - top)
            .coerceAtLeast(1)

        val cropped = Bitmap.createBitmap(bitmap, left, top, cropSize, cropSize)
        bitmap.recycle()

        val file = File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { cropped.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        cropped.recycle()

        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        null
    }
}
