package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Live Cyber-Gothic background with dynamic radial darkness and drifting ambient particles
 */
@Composable
fun LiveGothicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles_drift")
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Stable random seed for particle initial positions
    val particles = remember {
        List(24) {
            val randomX = Random.nextFloat()
            val randomY = Random.nextFloat()
            val size = Random.nextFloat() * 2.5f + 1f
            val alpha = Random.nextFloat() * 0.4f + 0.15f
            val speed = Random.nextFloat() * 0.5f + 0.5f
            val isCrimson = Random.nextBoolean()
            Particle(randomX, randomY, size, alpha, speed, isCrimson)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        CyberGothicCenter,
                        CyberGothicMid,
                        CyberGothicEdge
                    ),
                    center = Offset(Float.POSITIVE_INFINITY / 2f, 0f),
                    radius = 2200f
                )
            )
    ) {
        // Floating ambient dust / cyber sparks
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            particles.forEach { p ->
                val currentY = ((p.initialY - (particlePhase * p.speed)) % 1f + 1f) % 1f
                val currentX = p.initialX + (sin((particlePhase * 6.28f * p.speed).toDouble()).toFloat() * 0.03f)
                val color = if (p.isCrimson) {
                    NeonCrimson.copy(alpha = p.alpha)
                } else {
                    GoldAccent.copy(alpha = p.alpha * 0.8f)
                }
                drawCircle(
                    color = color,
                    radius = p.size * density,
                    center = Offset(currentX * w, currentY * h)
                )
            }
        }

        content()
    }
}

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float,
    val isCrimson: Boolean
)

/**
 * Animated Forensic Radar Scanner replacing empty list texts
 */
@Composable
fun DetectiveRadarScanner(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GlassBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 22.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Concentric radar grid rings
                    drawCircle(
                        color = NeonCyberGreen.copy(alpha = 0.15f),
                        radius = radius * 0.35f,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = NeonCyberGreen.copy(alpha = 0.22f),
                        radius = radius * 0.7f,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = NeonCyberGreen.copy(alpha = 0.35f),
                        radius = radius * 0.95f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Expanding pulsating ring
                    drawCircle(
                        color = NeonCyberGreen.copy(alpha = pulseAlpha),
                        radius = radius * 0.95f * pulseScale,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Radar crosshairs
                    drawLine(
                        color = NeonCyberGreen.copy(alpha = 0.2f),
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y + radius),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = NeonCyberGreen.copy(alpha = 0.2f),
                        start = Offset(center.x - radius, center.y),
                        end = Offset(center.x + radius, center.y),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Rotating scanner beam
                    rotate(sweepAngle, center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    NeonCyberGreen.copy(alpha = 0.05f),
                                    NeonCyberGreen.copy(alpha = 0.45f)
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 70f,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )
                    }

                    // Glowing center dot
                    drawCircle(
                        color = NeonCyberGreen,
                        radius = 3.5.dp.toPx(),
                        center = center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonCyberGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "رادار در حال اسکن لابی‌های فعال...",
                    color = NeonCyberGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "هیچ اتاق عمومی بازی باز نیست. اتاق جدیدی بسازید یا با کد ۵ رقمی ملحق شوید.",
                color = SlateGray,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

/**
 * 3D Tactile Controller Button with scale bounce and haptic click
 */
@Composable
fun TactileGamingButton(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    gradientTop: Color,
    gradientBottom: Color,
    glowColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current

    // Trigger subtle haptic on press
    LaunchedEffect(isPressed) {
        if (isPressed) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btn_scale"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(gradientTop, gradientBottom)
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Text(
                            text = badgeText,
                            color = GoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.material3.LocalTextStyle.current.copy(shadow = GamingTextShadow)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }
    }
}
