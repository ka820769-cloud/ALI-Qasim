package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayTrendItem
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseCoral
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.PulseMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

enum class TrendMetricMode(val label: String, val iconEmoji: String) {
    COMBINED("Combined", "⚡"),
    CALORIES("Calories", "🔥"),
    DURATION("Duration", "⏱️")
}

@Composable
fun WeeklyTrendsChart(
    dayTrends: List<DayTrendItem>,
    selectedDayIndex: Int,
    onSelectDay: (Int) -> Unit,
    metricMode: TrendMetricMode,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(metricMode, dayTrends) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val maxCalorie = remember(dayTrends) {
        (dayTrends.maxOfOrNull { it.totalCalories } ?: 500).coerceAtLeast(400)
    }
    val maxDuration = remember(dayTrends) {
        (dayTrends.maxOfOrNull { it.totalDurationMinutes } ?: 60).coerceAtLeast(60)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("weekly_trends_chart")
    ) {
        Column {
            // Chart Legend (Recharts style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7-DAY ACTIVITY TREND",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (metricMode == TrendMetricMode.COMBINED || metricMode == TrendMetricMode.CALORIES) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PulseCoral)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = "Calories (kcal)", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                    if (metricMode == TrendMetricMode.COMBINED || metricMode == TrendMetricMode.DURATION) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PulseCyan)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = "Duration (m)", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Canvas Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dayTrends) {
                            detectTapGestures { offset ->
                                val count = dayTrends.size
                                if (count > 0) {
                                    val slotWidth = size.width / count
                                    val tappedIndex = (offset.x / slotWidth).toInt().coerceIn(0, count - 1)
                                    onSelectDay(tappedIndex)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 28.dp.toPx()
                    val chartHeight = height - bottomPadding
                    val count = dayTrends.size
                    if (count == 0) return@Canvas

                    val slotWidth = width / count

                    // Draw Recharts-style dashed Cartesian grid lines (4 horizontal tiers)
                    val gridColor = DarkSurfaceElevated.copy(alpha = 0.8f)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                    for (tier in 1..3) {
                        val y = chartHeight * (tier / 4f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = dashEffect
                        )
                    }

                    // Draw baseline
                    drawLine(
                        color = DarkSurfaceVariant,
                        start = Offset(0f, chartHeight),
                        end = Offset(width, chartHeight),
                        strokeWidth = 1.5f
                    )

                    // Draw bars for each day
                    dayTrends.forEachIndexed { index, item ->
                        val isSelected = index == selectedDayIndex
                        val slotCenterX = index * slotWidth + (slotWidth / 2f)

                        // Highlight background pill for selected or today
                        if (isSelected) {
                            drawRoundRect(
                                color = DarkSurfaceElevated.copy(alpha = 0.6f),
                                topLeft = Offset(index * slotWidth + 4.dp.toPx(), 0f),
                                size = Size(slotWidth - 8.dp.toPx(), chartHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }

                        when (metricMode) {
                            TrendMetricMode.CALORIES -> {
                                val calRatio = (item.totalCalories.toFloat() / maxCalorie.toFloat()).coerceIn(0f, 1f)
                                val barHeight = (calRatio * (chartHeight - 16.dp.toPx()) * animProgress.value)
                                    .coerceAtLeast(if (item.totalCalories > 0) 6.dp.toPx() else 0f)
                                val barWidth = 18.dp.toPx()
                                val barLeft = slotCenterX - (barWidth / 2f)
                                val barTop = chartHeight - barHeight

                                if (barHeight > 0) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(PulseAmber, PulseCoral),
                                            startY = barTop,
                                            endY = chartHeight
                                        ),
                                        topLeft = Offset(barLeft, barTop),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                }
                            }

                            TrendMetricMode.DURATION -> {
                                val durRatio = (item.totalDurationMinutes.toFloat() / maxDuration.toFloat()).coerceIn(0f, 1f)
                                val barHeight = (durRatio * (chartHeight - 16.dp.toPx()) * animProgress.value)
                                    .coerceAtLeast(if (item.totalDurationMinutes > 0) 6.dp.toPx() else 0f)
                                val barWidth = 18.dp.toPx()
                                val barLeft = slotCenterX - (barWidth / 2f)
                                val barTop = chartHeight - barHeight

                                if (barHeight > 0) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(PulseMint, PulseCyan),
                                            startY = barTop,
                                            endY = chartHeight
                                        ),
                                        topLeft = Offset(barLeft, barTop),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                }
                            }

                            TrendMetricMode.COMBINED -> {
                                // Side-by-side twin bars (Coral for Calorie, Cyan for Duration)
                                val barWidth = 10.dp.toPx()
                                val gap = 3.dp.toPx()

                                // Calorie bar (left)
                                val calRatio = (item.totalCalories.toFloat() / maxCalorie.toFloat()).coerceIn(0f, 1f)
                                val calHeight = (calRatio * (chartHeight - 16.dp.toPx()) * animProgress.value)
                                    .coerceAtLeast(if (item.totalCalories > 0) 6.dp.toPx() else 0f)
                                val calLeft = slotCenterX - barWidth - (gap / 2f)
                                val calTop = chartHeight - calHeight

                                if (calHeight > 0) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(PulseAmber, PulseCoral),
                                            startY = calTop,
                                            endY = chartHeight
                                        ),
                                        topLeft = Offset(calLeft, calTop),
                                        size = Size(barWidth, calHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }

                                // Duration bar (right)
                                val durRatio = (item.totalDurationMinutes.toFloat() / maxDuration.toFloat()).coerceIn(0f, 1f)
                                val durHeight = (durRatio * (chartHeight - 16.dp.toPx()) * animProgress.value)
                                    .coerceAtLeast(if (item.totalDurationMinutes > 0) 6.dp.toPx() else 0f)
                                val durLeft = slotCenterX + (gap / 2f)
                                val durTop = chartHeight - durHeight

                                if (durHeight > 0) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(PulseMint, PulseCyan),
                                            startY = durTop,
                                            endY = chartHeight
                                        ),
                                        topLeft = Offset(durLeft, durTop),
                                        size = Size(barWidth, durHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }

                // X-Axis Day Labels Row below the chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayTrends.forEachIndexed { index, item ->
                        val isSelected = index == selectedDayIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) PulseCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { onSelectDay(index) }
                                .padding(vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = item.dayLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected || item.isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isSelected -> PulseCyan
                                        item.isToday -> PulseAmber
                                        else -> TextSecondary
                                    }
                                )
                                if (item.isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(PulseAmber)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
