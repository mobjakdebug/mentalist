package com.example.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class WalkthroughStep(
    val stepIndex: Int,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    val badge: String,
    val title: String,
    val description: String,
    val highlightHint: String
)

object WalkthroughManager {
    private const val PREFS_NAME = "siahbazi_walkthrough_prefs"
    private const val KEY_COMPLETED = "walkthrough_completed_v1"

    fun isCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_COMPLETED, false)
    }

    fun setCompleted(context: Context, completed: Boolean = true) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_COMPLETED, completed).apply()
    }
}

val walkthroughSteps = listOf(
    WalkthroughStep(
        stepIndex = 1,
        iconVector = Icons.Default.Person,
        badge = "پرونده محرمانه کارآگاه",
        title = "شناسنامه و درجه روانشناسی",
        description = "اینجا پرونده محرمانه شما در بازی است. رتبه، درصد موفقیت، سکه‌ها و آمار بلوف‌های پیروزمندانه شما در این بخش به صورت زنده ثبت می‌شوند.",
        highlightHint = "کارت پروفایل مأمور در بالای صفحه"
    ),
    WalkthroughStep(
        stepIndex = 2,
        iconVector = Icons.Default.Lock,
        badge = "میزبانی مسابقه",
        title = "ساخت اتاق و لابی اختصاصی",
        description = "می‌توانید با انتخاب این بخش، اتاق خصوصی با کد ۵ رقمی بسازید و لینک دعوت را برای دوستانتان بفرستید تا رقابت بلافاصله آغاز شود.",
        highlightHint = "دکمه قرمز ایجاد اتاق جدید"
    ),
    WalkthroughStep(
        stepIndex = 3,
        iconVector = Icons.AutoMirrored.Filled.ArrowForward,
        badge = "ورود مستقیم",
        title = "پیوستن با کد اتاق",
        description = "اگر دوستانتان اتاقی ساخته‌اند، کافیست کد ۵ رقمی آنها را اینجا وارد کنید و بدون معطلی به لابی مسابقه ملحق شوید.",
        highlightHint = "دکمه پیوستن به لابی"
    ),
    WalkthroughStep(
        stepIndex = 4,
        iconVector = Icons.Default.Search,
        badge = "رادار آنلاین لابی‌ها",
        title = "جستجوی مسابقات عمومی",
        description = "سیستم رادار شبکه، به طور پیوسته لابی‌های باز را شناسایی می‌کند. هر زمان اتاقی آنلاین شود، با یک لمس می‌توانید وارد مسابقه شوید.",
        highlightHint = "بخش رادار و لابی‌های آنلاین"
    ),
    WalkthroughStep(
        stepIndex = 5,
        iconVector = Icons.Default.Star,
        badge = "سیستم جوایز دستی (Claim)",
        title = "ماموریت‌های روزانه و پاداش‌ها",
        description = "جوایز و سکه‌ها به صورت خودکار داده نمی‌شوند! بعد از هر مسابقه به بخش ماموریت‌ها بروید و با زدن دکمه 'دریافت پاداش'، سکه و XP خود را تحویل بگیرید.",
        highlightHint = "بخش ماموریت‌های روزانه"
    ),
    WalkthroughStep(
        stepIndex = 6,
        iconVector = Icons.Default.Face,
        badge = "قانون طلایی بازی",
        title = "مچ‌گیری روانشناختی منتالیست",
        description = "در هر راند، یک بازیکن در نقش منتالیست قرار می‌گیرد و بقیه به سوال محرمانه پاسخ می‌دهند. آیا می‌توانید ذهن منتالیست را فریب دهید و بلوف بزنید؟",
        highlightHint = "قوانین و گیم‌پلی اصلی"
    )
)

@Composable
fun InteractiveWalkthroughOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = walkthroughSteps[currentStepIndex]
    val totalSteps = walkthroughSteps.size

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(enabled = false) {}
            .testTag("walkthrough_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Top Actions (Skip & Close)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Button
                TextButton(
                    onClick = {
                        WalkthroughManager.setCompleted(context, true)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("btn_skip_walkthrough")
                ) {
                    Text(
                        text = "رد کردن آموزش (Skip)",
                        color = GoldAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Step Counter Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Text(
                        text = "${currentStepIndex + 1} از $totalSteps",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Main Glassmorphic Step Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = GlassBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("walkthrough_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing Glowing Step Icon
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        CrimsonPrimary.copy(alpha = 0.4f * haloPulse),
                                        Color(0xFF1B142B)
                                    )
                                )
                            )
                            .border(
                                2.dp,
                                Brush.linearGradient(
                                    listOf(GoldAccent, CrimsonPrimary)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.iconVector,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step Category Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CrimsonDark.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = step.badge,
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step Title
                    Text(
                        text = step.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        style = LocalTextStyle.current.copy(shadow = GamingTextShadow)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step Description
                    Text(
                        text = step.description,
                        color = TextSecondaryDark,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Highlight Hint Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎯", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = step.highlightHint,
                                color = NeonCyberGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Segmented Step Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until totalSteps) {
                            val isActive = i == currentStepIndex
                            Box(
                                modifier = Modifier
                                    .height(4.dp)
                                    .width(if (isActive) 24.dp else 8.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isActive) GoldAccent else Color.White.copy(alpha = 0.2f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Bottom Navigation Buttons (Prev & Next/Finish)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentStepIndex > 0) {
                            OutlinedButton(
                                onClick = { currentStepIndex-- },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_walkthrough_prev")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("قبلی", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (currentStepIndex < totalSteps - 1) {
                                    currentStepIndex++
                                } else {
                                    WalkthroughManager.setCompleted(context, true)
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentStepIndex == totalSteps - 1) SuccessGreen else CrimsonPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(if (currentStepIndex > 0) 1.5f else 1f)
                                .height(46.dp)
                                .testTag("btn_walkthrough_next")
                        ) {
                            Text(
                                text = if (currentStepIndex == totalSteps - 1) "شروع بازی 🚀" else "بخش بعدی",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
