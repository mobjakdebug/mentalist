package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.model.RankTier
import com.example.ui.theme.*

@Composable
fun GameHeaderBar(
    roundText: String,
    secondsRemaining: Int,
    maxSeconds: Int,
    isMentalist: Boolean,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (secondsRemaining.toFloat() / maxSeconds.toFloat()).coerceIn(0f, 1f)
    val timerColor = when {
        secondsRemaining <= 10 -> DangerRed
        secondsRemaining <= 25 -> GoldAccent
        else -> ElectricCyan
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Role Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isMentalist) CrimsonDark else SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMentalist) CrimsonPrimary else MysticPurpleLight
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMentalist) "🕵️ نقش شما: بازجو" else "🎭 نقش شما: مظنون",
                        color = if (isMentalist) GoldLight else TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Round indicator
            Text(
                text = roundText,
                color = TextSecondaryDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Info / Rules Icon Button
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(36.dp).testTag("header_info_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "قوانین بازی",
                    tint = GoldAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Timer Row & Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = timerColor,
                trackColor = SurfaceElevated
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${secondsRemaining}s",
                color = timerColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun PlayerAvatarBadge(
    player: Player,
    isSelected: Boolean = false,
    showScore: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        player.isMentalist -> CrimsonPrimary
        isSelected -> GoldAccent
        else -> SurfaceBorder
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(SurfaceElevated)
                .border(2.dp, borderColor, CircleShape)
        ) {
            Text(
                text = player.avatarEmoji,
                fontSize = 26.sp
            )
            if (player.isMentalist) {
                Surface(
                    color = CrimsonPrimary,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🔍", fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = player.name,
            color = if (player.isLocalUser) GoldAccent else TextPrimaryDark,
            fontWeight = if (player.isLocalUser) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        RankChip(rankTier = player.rankTier, level = player.level)

        if (showScore) {
            Text(
                text = "${player.matchScore} XP",
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun RankChip(
    rankTier: RankTier,
    level: Int,
    modifier: Modifier = Modifier
) {
    val chipColor = when (rankTier) {
        RankTier.PROFILER -> Color(0xFF4A6572)
        RankTier.TACTICIAN -> Color(0xFF2E7D32)
        RankTier.SHADOW -> Color(0xFF512DA8)
        RankTier.ILLUSIONIST -> Color(0xFFC2185B)
        RankTier.MENTALIST -> Color(0xFFD84315)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = chipColor.copy(alpha = 0.25f),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, chipColor),
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rankTier.badgeIcon,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "${rankTier.titleFa} L$level",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ReportViolationDialog(
    reportedName: String,
    messageText: String,
    onDismiss: () -> Unit,
    onSubmitReport: (reason: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("فحاشی و الفاظ رکیک") }
    val reasons = listOf(
        "فحاشی و الفاظ رکیک",
        "پیام نامربوط و ترولینگ",
        "توهین مستقیم به سایر بازیکنان",
        "تلاش برای دور زدن قوانین بازی"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    tint = CrimsonPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "گزارش تخلف کاربر",
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "کاربر گزارش شده: $reportedName",
                    color = GoldAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "\"$messageText\"",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "علت گزارش را انتخاب کنید:",
                    color = TextPrimaryDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = CrimsonPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = reason,
                            color = TextPrimaryDark,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitReport(selectedReason) },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text("ارسال گزارش به ادمین")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = TextSecondaryDark)
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
fun GameRulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "قوانین و نظام امتیازدهی «سیاه بازی»",
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🎭 روند بازی:\n" +
                            "۱. سوال روانی: همه مخفیانه پاسخ می‌دهند.\n" +
                            "۲. بر زدن: کارت‌ها بدون نام پخش می‌شوند.\n" +
                            "۳. بازجویی و بلوف: منتالیست بازجویی می‌کند، بقیه مجاز به بلوف و تقلید لحن هستند.\n" +
                            "۴. حدس‌زنی: منتالیست کارت‌ها را به افراد وصل می‌کند.",
                    color = TextPrimaryDark,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = SurfaceBorder)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "⚖️ جدول امتیازات و جریمه‌ها:\n" +
                            "• منتالیست حدس درست: ۱۰۰+ XP\n" +
                            "• منتالیست حدس اشتباه: ۵۰- XP جریمه\n" +
                            "• فریب موفق (خوانده نشدن دست): ۱۵۰+ XP\n" +
                            "• لو رفتن دست بازیکن: ۴۰- XP جریمه\n" +
                            "• سکوت و عدم پاسخ (AFK): ۱۰۰- XP جریمه سنگین",
                    color = GoldLight,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
            ) {
                Text("متوجه شدم", color = GoldAccent)
            }
        },
        containerColor = SurfaceDark
    )
}
