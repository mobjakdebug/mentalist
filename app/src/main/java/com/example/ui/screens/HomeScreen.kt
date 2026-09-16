package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserProfileEntity
import com.example.data.remote.D1ConnectionState
import com.example.data.remote.D1RoomSummary
import com.example.model.GameBadge
import com.example.model.RankTier
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    userProfile: UserProfileEntity,
    badges: List<GameBadge>,
    publicRooms: List<D1RoomSummary>,
    isRefreshingRooms: Boolean,
    onCreateRoom: () -> Unit,
    onOpenJoinDialog: (String) -> Unit,
    onRefreshRooms: () -> Unit,
    onOpenQuests: () -> Unit,
    onLogout: () -> Unit,
    onOpenQuestions: (() -> Unit)? = null,
    onOpenRules: (() -> Unit)? = null,
    d1State: D1ConnectionState? = null,
    workerUrl: String? = null,
    onOpenD1Settings: (() -> Unit)? = null,
    onOpenD1Guide: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showWalkthrough by remember {
        mutableStateOf(!WalkthroughManager.isCompleted(context))
    }

    val rank = RankTier.fromLevel(userProfile.level)
    val xpInCurrentLevel = userProfile.xp % 100
    val xpProgress = (xpInCurrentLevel / 100f).coerceIn(0f, 1f)

    // Animated refresh icon rotation
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LiveGothicBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 16.dp, bottom = 36.dp)
            ) {
                // --- 1. TOP HEADER & CONTROLS ---
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand & Project Identity
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(CrimsonPrimary, GothicCrimsonBottom)
                                        )
                                    )
                                    .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = GoldLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "سیاه‌بازی",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    style = LocalTextStyle.current.copy(shadow = GamingTextShadow)
                                )
                                Text(
                                    text = "پروژه منتالیست و روانشناسی",
                                    color = GoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Status & Logout Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Server Status Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GlassBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    NeonCyberGreen.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("server_status_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyberGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "سرور آنلاین",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Logout Button
                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GlassBackground)
                                    .border(1.dp, GlassBorder, CircleShape)
                                    .testTag("btn_logout")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "خروج از حساب",
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // --- 2. HERO INVESTIGATOR DOSSIER (CYBER-GOTHIC PROFILE CARD) ---
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = GlassBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .testTag("investigator_profile_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            // Confidential Header Banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CrimsonDark.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "پرونده محرمانه مأمور",
                                        color = GoldLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "شناسه: #${userProfile.id.takeLast(6).uppercase()}",
                                    color = SlateGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Metallic Avatar Frame
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(CrimsonPrimary.copy(alpha = 0.35f), DeepObsidian)
                                            )
                                        )
                                        .border(
                                            2.5.dp,
                                            Brush.sweepGradient(
                                                listOf(MetallicChrome, GoldAccent, MetallicDark, GoldAccent)
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = GoldLight,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = userProfile.username,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            style = LocalTextStyle.current.copy(shadow = SoftGlowShadow)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = CrimsonPrimary.copy(alpha = 0.25f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary)
                                        ) {
                                            Text(
                                                text = "سطح ${userProfile.level}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = "${rank.badgeIcon} درجه: ${rank.titleFa}",
                                        color = GoldAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Neon XP Progress Bar
                                    Column {
                                        LinearProgressIndicator(
                                            progress = { xpProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = GoldAccent,
                                            trackColor = DeepObsidian
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "$xpInCurrentLevel / 100 XP",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = "${(xpProgress * 100).toInt()}% تا درجه بعدی",
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 4 3D Micro-Stat Cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatPill(
                                    iconVector = Icons.Default.Star,
                                    iconTint = GoldAccent,
                                    title = "سکه‌ها",
                                    value = "${userProfile.coins}",
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    iconVector = Icons.Default.ThumbUp,
                                    iconTint = NeonCyberGreen,
                                    title = "پیروزی‌ها",
                                    value = "${userProfile.wins}",
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    iconVector = Icons.Default.PlayArrow,
                                    iconTint = CrimsonLight,
                                    title = "مسابقات",
                                    value = "${userProfile.matchesPlayed}",
                                    modifier = Modifier.weight(1f)
                                )
                                StatPill(
                                    iconVector = Icons.Default.CheckCircle,
                                    iconTint = ElectricCyan,
                                    title = "بلوف موفق",
                                    value = "${userProfile.successfulBluffs}",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // --- 3. PRIMARY ACTION CONTROLLERS (TACTILE BUTTONS, NO EMOJIS) ---
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Create Room Switch
                        TactileGamingButton(
                            title = "ایجاد لابی جدید",
                            subtitle = "ساخت اتاق با کد امنیتی",
                            badgeText = "میزبان",
                            icon = Icons.Default.AddCircle,
                            gradientTop = GothicCrimsonTop,
                            gradientBottom = GothicCrimsonBottom,
                            glowColor = NeonCrimson,
                            onClick = onCreateRoom,
                            modifier = Modifier
                                .weight(1f)
                                .height(145.dp),
                            testTag = "btn_create_room"
                        )

                        // Direct Join Switch
                        TactileGamingButton(
                            title = "پیوستن به لابی",
                            subtitle = "ورود با کد ۵ رقمی اتاق",
                            badgeText = "ورود مستقیم",
                            icon = Icons.Default.Lock,
                            gradientTop = GothicIndigoTop,
                            gradientBottom = GothicIndigoBottom,
                            glowColor = ElectricCyan,
                            onClick = { onOpenJoinDialog("") },
                            modifier = Modifier
                                .weight(1f)
                                .height(145.dp),
                            testTag = "btn_open_join_dialog"
                        )
                    }
                }

                // --- 4. PUBLIC ONLINE LOBBIES / RADAR SECTION ---
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "لابی‌های عمومی فعال",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceElevated
                            ) {
                                Text(
                                    text = "${publicRooms.size} اتاق",
                                    color = GoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onRefreshRooms,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_refresh_rooms")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تازه‌سازی اتاق‌ها",
                                tint = GoldAccent,
                                modifier = Modifier
                                    .size(20.dp)
                                    .then(if (isRefreshingRooms) Modifier.rotate(angle) else Modifier)
                            )
                        }
                    }
                }

                if (publicRooms.isEmpty()) {
                    item {
                        DetectiveRadarScanner(modifier = Modifier.padding(bottom = 18.dp))
                    }
                } else {
                    items(publicRooms) { room ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GlassBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenJoinDialog(room.code) }
                                .testTag("room_item_${room.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "اتاق: ${room.code}",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = NeonCyberGreen.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "آماده بازی",
                                                    color = NeonCyberGreen,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "راند ${room.currentRound} • ${room.playerCount} شرکت‌کننده",
                                            color = SlateGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onOpenJoinDialog(room.code) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "ورود", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // --- 5. ACCESS HUB (CLEANED UP: NO QUESTION BANKS, NO RULES, WITH WALKTHROUGH & QUESTS) ---
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "مرکز دسترسی و ابزارها",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quests & Manual Claim Card
                        HubTileCard(
                            iconVector = Icons.Default.Star,
                            iconTint = GoldAccent,
                            title = "ماموریت‌ها و افتخارات",
                            subtitle = "دریافت دستی سکه و XP جوایز",
                            badgeText = "Claim",
                            onClick = onOpenQuests,
                            modifier = Modifier.weight(1f),
                            testTag = "btn_daily_quests"
                        )

                        // Interactive Walkthrough Launcher
                        HubTileCard(
                            iconVector = Icons.Default.PlayArrow,
                            iconTint = NeonCyberGreen,
                            title = "آموزش تعاملی بازی",
                            subtitle = "تور تصویری مراحل و قوانین",
                            badgeText = "راهنما",
                            onClick = { showWalkthrough = true },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_launch_walkthrough"
                        )
                    }
                }
            }
        }

        // Interactive Walkthrough Overlay (Only shown for first-timers, or on user tap)
        if (showWalkthrough) {
            InteractiveWalkthroughOverlay(
                onDismiss = { showWalkthrough = false }
            )
        }
    }
}

@Composable
private fun StatPill(
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = title,
                color = TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun HubTileCard(
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GlassBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Text(
                        text = badgeText,
                        color = iconTint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = SlateGray,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}
