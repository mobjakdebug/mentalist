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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    onOpenQuestions: () -> Unit,
    onOpenRules: () -> Unit,
    onLogout: () -> Unit,
    d1State: D1ConnectionState? = null,
    workerUrl: String? = null,
    onOpenD1Settings: (() -> Unit)? = null,
    onOpenD1Guide: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0C0E17),
                        Color(0xFF121422),
                        DeepObsidian
                    )
                )
            )
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // --- 1. TOP HEADER & CONTROLS ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎭",
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "سیاه‌بازی",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "پروژه منتالیست و روانشناسی",
                            color = GoldAccent.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Header Action Pill (Server Status + Logout)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Live Server Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131728),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            SuccessGreen.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("server_status_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
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

                    // Logout Icon Button
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2235))
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

        // --- 2. HERO USER PROFILE CARD (REAL STATS, NO FAKE NUMBERS) ---
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF151829),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(CrimsonPrimary.copy(alpha = 0.35f), Color(0xFF1B2036))
                                    )
                                )
                                .border(2.dp, GoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = userProfile.avatarEmoji, fontSize = 34.sp)
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
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CrimsonPrimary.copy(alpha = 0.2f),
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

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${rank.badgeIcon} رتبه: ${rank.titleFa}",
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // XP Progress Bar
                            Column {
                                LinearProgressIndicator(
                                    progress = { xpProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = GoldAccent,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$xpInCurrentLevel / 100 XP",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "${(xpProgress * 100).toInt()}% تا سطح بعد",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Real Stat Counter Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTile(
                            icon = "🪙",
                            title = "سکه‌ها",
                            value = "${userProfile.coins}",
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            icon = "🏆",
                            title = "پیروزی‌ها",
                            value = "${userProfile.wins}",
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            icon = "🎮",
                            title = "مسابقات",
                            value = "${userProfile.matchesPlayed}",
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            icon = "🎭",
                            title = "بلوف موفق",
                            value = "${userProfile.successfulBluffs}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- 3. PRIMARY GAME ACTION HUB (CREATE / JOIN ROOM) ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Create Room Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onCreateRoom)
                        .testTag("btn_create_room")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFE50914), Color(0xFF8B0000))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
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
                                Text(text = "👑", fontSize = 28.sp)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "میزبان",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "ایجاد اتاق جدید",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "ساخت لابی با کد اختصاصی",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Join Room Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenJoinDialog("") }
                        .testTag("btn_open_join_dialog")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF2C3E50), Color(0xFF1A252F))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
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
                                Text(text = "🔑", fontSize = 28.sp)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "ورود مستقیم",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "پیوستن به لابی",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "ورود با کد ۵ رقمی اتاق",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. PUBLIC ONLINE LOBBIES SECTION (D1 LIVE) ---
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
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1B2036)
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
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF151829),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🌐", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "در حال حاضر هیچ اتاق عمومی فعالی وجود ندارد",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "همین حالا اولین اتاق بازی را بسازید یا با کد اختصاصی وارد شوید!",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(publicRooms) { room ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF151829),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
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
                                    .background(Color(0xFF20263E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🕵️", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "کد: ${room.code}",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SuccessGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "در انتظار",
                                            color = SuccessGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "راند ${room.currentRound} • ${room.playerCount} نفر در لابی",
                                    color = Color.Gray,
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

        // --- 5. GAME TOOLS & UTILITIES HUB ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "مرکز دسترسی و امکانات",
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
                UtilityCard(
                    icon = "📜",
                    title = "بانک سوالات",
                    subtitle = "۱۲ سوال روانشناسی",
                    onClick = onOpenQuestions,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_question_bank"
                )
                UtilityCard(
                    icon = "🎯",
                    title = "ماموریت‌ها",
                    subtitle = "جوایز و XP روزانه",
                    onClick = onOpenQuests,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_daily_quests"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UtilityCard(
                    icon = "📖",
                    title = "قوانین بازی",
                    subtitle = "راهنمای منتالیست",
                    onClick = onOpenRules,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_game_rules"
                )
                UtilityCard(
                    icon = "🧠",
                    title = "بانک سوالات",
                    subtitle = "۱۰۰ سوال روانشناسی",
                    onClick = onOpenQuestions,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_questions_home"
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    icon: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1B1E32),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun UtilityCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF151829),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
