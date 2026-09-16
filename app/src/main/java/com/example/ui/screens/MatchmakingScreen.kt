package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.ui.components.RankChip
import com.example.ui.theme.*

@Composable
fun MatchmakingScreen(
    statusText: String,
    progress: Float,
    players: List<Player>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "اتاق مچمکینگ هوشمند",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "پروژه منتالیست • لابی رندوم بله",
                color = TextSecondaryDark,
                fontSize = 12.sp
            )
        }

        // Radar Visualizer
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .testTag("radar_visualizer")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Static outer rings
                drawCircle(
                    color = SurfaceBorder,
                    radius = size.minDimension / 2,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = SurfaceBorder.copy(alpha = 0.6f),
                    radius = size.minDimension / 3,
                    style = Stroke(width = 1.dp.toPx())
                )
                drawCircle(
                    color = SurfaceBorder.copy(alpha = 0.4f),
                    radius = size.minDimension / 4,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Expanding pulse ring
                drawCircle(
                    color = CrimsonPrimary.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // Center Radar Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CrimsonPrimary, CrimsonDark)
                        )
                    )
                    .border(2.dp, GoldLight, CircleShape)
            ) {
                Text(text = "👁️", fontSize = 34.sp)
            }
        }

        // Status & Detected Players
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = statusText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GoldAccent,
                trackColor = SurfaceElevated
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Players Found Display
            if (players.isNotEmpty()) {
                Text(
                    text = "بازیکنان متصل شده (${players.size}/4):",
                    color = TextSecondaryDark,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    players.forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(p.avatarEmoji, fontSize = 22.sp)
                                Text(p.name, color = TextPrimaryDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                RankChip(rankTier = p.rankTier, level = p.level)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
