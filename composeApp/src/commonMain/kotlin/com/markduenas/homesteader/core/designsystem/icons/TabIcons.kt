package com.markduenas.homesteader.core.designsystem.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DashboardIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val padding = size.width * 0.15f
        val gap = size.width * 0.08f
        val cellSize = (size.width - padding * 2 - gap) / 2
        val cornerRadius = CornerRadius(size.width * 0.08f)

        // Top-left
        drawRoundRect(
            color = tint,
            topLeft = Offset(padding, padding),
            size = Size(cellSize, cellSize),
            cornerRadius = cornerRadius
        )
        // Top-right
        drawRoundRect(
            color = tint,
            topLeft = Offset(padding + cellSize + gap, padding),
            size = Size(cellSize, cellSize),
            cornerRadius = cornerRadius
        )
        // Bottom-left
        drawRoundRect(
            color = tint,
            topLeft = Offset(padding, padding + cellSize + gap),
            size = Size(cellSize, cellSize),
            cornerRadius = cornerRadius
        )
        // Bottom-right
        drawRoundRect(
            color = tint,
            topLeft = Offset(padding + cellSize + gap, padding + cellSize + gap),
            size = Size(cellSize, cellSize),
            cornerRadius = cornerRadius
        )
    }
}

@Composable
fun AnimalsIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val scale = size.width / 24f

        // Draw a simple cow/animal head silhouette
        val path = Path().apply {
            // Head outline
            moveTo(centerX, centerY + 6 * scale)
            // Left side
            cubicTo(
                centerX - 5 * scale, centerY + 6 * scale,
                centerX - 7 * scale, centerY + 2 * scale,
                centerX - 7 * scale, centerY - 1 * scale
            )
            // Left ear
            cubicTo(
                centerX - 8 * scale, centerY - 4 * scale,
                centerX - 6 * scale, centerY - 7 * scale,
                centerX - 4 * scale, centerY - 6 * scale
            )
            // Top of head
            cubicTo(
                centerX - 2 * scale, centerY - 8 * scale,
                centerX + 2 * scale, centerY - 8 * scale,
                centerX + 4 * scale, centerY - 6 * scale
            )
            // Right ear
            cubicTo(
                centerX + 6 * scale, centerY - 7 * scale,
                centerX + 8 * scale, centerY - 4 * scale,
                centerX + 7 * scale, centerY - 1 * scale
            )
            // Right side
            cubicTo(
                centerX + 7 * scale, centerY + 2 * scale,
                centerX + 5 * scale, centerY + 6 * scale,
                centerX, centerY + 6 * scale
            )
            close()
        }

        drawPath(
            path = path,
            color = tint
        )

        // Eyes
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = 1.2f * scale,
            center = Offset(centerX - 2.5f * scale, centerY - 1 * scale)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = 1.2f * scale,
            center = Offset(centerX + 2.5f * scale, centerY - 1 * scale)
        )
    }
}

@Composable
fun CalendarIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val padding = size.width * 0.12f
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val strokeWidth = size.width * 0.08f
        val cornerRadius = CornerRadius(size.width * 0.1f)

        // Calendar body
        drawRoundRect(
            color = tint,
            topLeft = Offset(padding, padding + height * 0.15f),
            size = Size(width, height * 0.85f),
            cornerRadius = cornerRadius,
            style = Stroke(width = strokeWidth)
        )

        // Top binding hooks
        val hookWidth = strokeWidth * 1.2f
        val hook1X = padding + width * 0.28f
        val hook2X = padding + width * 0.72f

        drawLine(
            color = tint,
            start = Offset(hook1X, padding),
            end = Offset(hook1X, padding + height * 0.25f),
            strokeWidth = hookWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(hook2X, padding),
            end = Offset(hook2X, padding + height * 0.25f),
            strokeWidth = hookWidth,
            cap = StrokeCap.Round
        )

        // Horizontal line below hooks
        drawLine(
            color = tint,
            start = Offset(padding, padding + height * 0.35f),
            end = Offset(padding + width, padding + height * 0.35f),
            strokeWidth = strokeWidth * 0.7f
        )

        // Grid dots for dates
        val dotRadius = size.width * 0.04f
        val gridStartY = padding + height * 0.5f
        val gridSpacingX = width / 4
        val gridSpacingY = height * 0.2f

        for (row in 0..1) {
            for (col in 0..2) {
                drawCircle(
                    color = tint,
                    radius = dotRadius,
                    center = Offset(
                        padding + gridSpacingX * (col + 1),
                        gridStartY + gridSpacingY * row
                    )
                )
            }
        }
    }
}

@Composable
fun MoreIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val dotRadius = size.width * 0.09f
        val spacing = size.height * 0.22f

        // Three vertical dots
        drawCircle(
            color = tint,
            radius = dotRadius,
            center = Offset(centerX, centerY - spacing)
        )
        drawCircle(
            color = tint,
            radius = dotRadius,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = tint,
            radius = dotRadius,
            center = Offset(centerX, centerY + spacing)
        )
    }
}
