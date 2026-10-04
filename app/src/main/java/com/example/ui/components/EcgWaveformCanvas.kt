package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PulseCyan

@Composable
fun EcgWaveformCanvas(
    wavePoints: List<Float>,
    modifier: Modifier = Modifier,
    waveColor: Color = PulseCyan,
    isConnected: Boolean = true
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Background subtle grid lines
        val gridColor = DarkSurfaceVariant.copy(alpha = 0.5f)
        val gridSpacing = 24.dp.toPx()

        var x = 0f
        while (x < width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += gridSpacing
        }

        var y = 0f
        while (y < height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += gridSpacing
        }

        // Draw centerline baseline
        drawLine(
            color = gridColor.copy(alpha = 0.8f),
            start = Offset(0f, height / 2f),
            end = Offset(width, height / 2f),
            strokeWidth = 1.5f
        )

        if (wavePoints.size < 2) return@Canvas

        val stepX = width / (wavePoints.size - 1)
        val path = Path()

        wavePoints.forEachIndexed { index, normalizedY ->
            // Invert so higher value = higher on screen
            val clampedY = (1f - normalizedY.coerceIn(0f, 1f)) * height
            val currentX = index * stepX
            if (index == 0) {
                path.moveTo(currentX, clampedY)
            } else {
                path.lineTo(currentX, clampedY)
            }
        }

        // Draw subtle glow shadow
        drawPath(
            path = path,
            color = waveColor.copy(alpha = 0.3f),
            style = Stroke(
                width = 6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw crisp foreground wave
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    waveColor.copy(alpha = 0.4f),
                    waveColor,
                    waveColor
                )
            ),
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw active scanning head dot at the right edge
        if (isConnected && wavePoints.isNotEmpty()) {
            val lastY = (1f - wavePoints.last().coerceIn(0f, 1f)) * height
            val lastX = (wavePoints.size - 1) * stepX

            drawCircle(
                color = waveColor.copy(alpha = 0.4f),
                radius = 7.dp.toPx(),
                center = Offset(lastX, lastY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(lastX, lastY)
            )
        }
    }
}
