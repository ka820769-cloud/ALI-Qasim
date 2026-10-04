package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailySnapshotEntity
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PulseAmber
import com.example.ui.theme.PulseCoral
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CalorieProgressCard(
    dailySnapshot: DailySnapshotEntity?,
    dailyGoal: Int,
    onLogWorkoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalBurned = dailySnapshot?.totalCaloriesBurned ?: 380
    val loggedWorkoutBurned = dailySnapshot?.loggedWorkoutCalories ?: 0
    val targetGoal = dailyGoal.coerceAtLeast(1)
    val progress = (totalBurned.toFloat() / targetGoal.toFloat()).coerceIn(0f, 1.5f)
    val animatedProgress by animateFloatAsState(
        targetValue = (progress.coerceAtMost(1f)),
        animationSpec = tween(durationMillis = 800),
        label = "calorie_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(PulseAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Calorie Burn",
                            tint = PulseAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DAILY CALORIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$totalBurned",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "/ $targetGoal kcal",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Breakdown: Workouts vs Passive
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(text = "Logged Workouts", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "+$loggedWorkoutBurned kcal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PulseAmber
                        )
                    }
                    Column {
                        Text(text = "Remaining", fontSize = 11.sp, color = TextSecondary)
                        val remaining = (targetGoal - totalBurned).coerceAtLeast(0)
                        Text(
                            text = "$remaining kcal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (remaining == 0) PulseCyan else TextPrimary
                        )
                    }
                }
            }

            // Circular Progress Gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                Canvas(modifier = Modifier.size(90.dp)) {
                    val strokeWidth = 8.dp.toPx()

                    // Background track
                    drawCircle(
                        color = DarkSurfaceElevated,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active progress arc
                    val sweepAngle = animatedProgress * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(PulseAmber, PulseCoral, PulseAmber)
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "GOAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = PulseAmber
                    )
                }
            }
        }
    }
}
