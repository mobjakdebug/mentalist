package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Send
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
import com.example.model.*
import com.example.ui.components.PlayerAvatarBadge
import com.example.ui.components.RankChip
import com.example.ui.theme.*

// --- LOBBY PHASE SCREEN ---
@Composable
fun LobbyPhaseScreen(
    roomCode: String,
    players: List<Player>,
    currentRound: Int,
    totalRounds: Int,
    isHost: Boolean,
    onStartMatch: () -> Unit,
    onLeaveRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mentalist = players.find { it.isMentalist }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header & Room Code
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onLeaveRoom,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("خروج از اتاق", fontSize = 12.sp)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "راند $currentRound از $totalRounds",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Room Code Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(CrimsonPrimary, MysticPurple))
                ),
                modifier = Modifier.fillMaxWidth().testTag("lobby_code_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "کد دعوت به اتاق Cloudflare D1",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = roomCode.ifBlank { "MNTL7" },
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent,
                            letterSpacing = 6.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(roomCode))
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .background(SurfaceElevated, CircleShape)
                        ) {
                            Text("📋", fontSize = 18.sp)
                        }
                    }
                    Text(
                        text = "این کد را به دوستان خود بدهید تا از صفحه اصلی وارد شوند",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Players Grid & Online Status
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "بازیکنان حاضر در اتاق (${players.size}/6):",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1B3B2B)
                ) {
                    Text(
                        text = "🟢 همگام با D1",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(players) { player ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (player.isHost) GoldAccent else SurfaceBorder
                        ),
                        modifier = Modifier.width(110.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceElevated)
                                ) {
                                    Text(player.avatarEmoji, fontSize = 24.sp)
                                }
                                if (player.isHost) {
                                    Text("👑", fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = player.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RankChip(rankTier = player.rankTier, level = player.level)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (player.isOnline) "🟢 آنلاین" else "🟡 در انتظار",
                                color = if (player.isOnline) SuccessGreen else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isHost) {
                Button(
                    onClick = onStartMatch,
                    enabled = players.size >= 2,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_match_button")
                ) {
                    Text(
                        text = if (players.size >= 2) "شروع مسابقه در دیتابیس D1" else "در انتظار اتصال حداقل ۲ بازیکن...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = GoldAccent,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "در انتظار شروع بازی توسط میزبان...",
                            color = TextSecondaryDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Text(
            text = "پس از شروع، سوال روانشناسی از جدول D1 دریافت خواهد شد",
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

// --- ANSWERING PHASE SCREEN (45s) ---
@Composable
fun AnsweringPhaseScreen(
    question: QuestionItem,
    players: List<Player>,
    userAnswerText: String,
    hasSubmitted: Boolean,
    onAnswerChange: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Question Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(CrimsonPrimary, MysticPurple))
                ),
                modifier = Modifier.fillMaxWidth().testTag("question_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CrimsonDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "دسته‌بندی: ${question.category}",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = question.questionText,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "نکته روانشناسی: همه پاسخ‌ها بدون نام و با فونت یکسان پخش می‌شوند. طوری بنویسید که لحنتان لو نرود یا سبک دیگران را تقلید کنید!",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Players answering status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                players.forEach { p ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (p.hasAnswered) Color(0xFF1B3B2B) else SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (p.hasAnswered) SuccessGreen else SurfaceBorder
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(p.avatarEmoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = p.name,
                                color = if (p.hasAnswered) SuccessGreen else TextSecondaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (p.hasAnswered) "✓" else "...",
                                color = if (p.hasAnswered) SuccessGreen else GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Text Input & Submission
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = userAnswerText,
                    onValueChange = { if (!hasSubmitted) onAnswerChange(it) },
                    readOnly = hasSubmitted,
                    placeholder = {
                        Text(
                            text = "پاسخ محرمانه خود را اینجا تایپ کنید...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("secret_answer_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (hasSubmitted) "پاسخ ثبت شد و در گاوصندوق رمزگذاری گردید." else "حداکثر ۱۲۰ کاراکتر",
                        color = if (hasSubmitted) SuccessGreen else TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${userAnswerText.length}/120",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSubmitAnswer,
                    enabled = !hasSubmitted && userAnswerText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonPrimary,
                        disabledContainerColor = SurfaceElevated
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_answer_button")
                ) {
                    Text(
                        text = if (hasSubmitted) "پاسخ شما با موفقیت ثبت شد ✓" else "ثبت محرمانه و آماده شدن",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// --- SHUFFLING PHASE SCREEN (4s) ---
@Composable
fun ShufflingPhaseScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shuffle_cards")
    val cardOffset1 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
            // Stack of animated cards
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardAnonBg,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent),
                modifier = Modifier
                    .size(110.dp, 140.dp)
                    .offset(x = (-cardOffset1).dp, y = (cardOffset1 / 2).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("کارت ج", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardAnonBg,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CrimsonPrimary),
                modifier = Modifier
                    .size(110.dp, 140.dp)
                    .offset(x = (cardOffset1).dp, y = (-cardOffset1 / 2).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("کارت ب", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardAnonBg,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MysticPurpleLight),
                modifier = Modifier.size(110.dp, 140.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("کارت الف", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "در حال بر زدن کارت‌ها...",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "تمام نام‌ها حذف شدند؛ چیدمان و فونت برای همه کاملاً یکسان شد.",
            color = TextSecondaryDark,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// --- INTERROGATION PHASE SCREEN (150s) ---
@Composable
fun InterrogationPhaseScreen(
    players: List<Player>,
    cards: List<CardAnswer>,
    chatMessages: List<ChatMessage>,
    userChatInput: String,
    isLocalUserMentalist: Boolean,
    afkWarningActive: Boolean,
    onUserChatInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onQuickProbe: (String) -> Unit,
    onProceedToGuessing: () -> Unit,
    onReportMessage: (senderName: String, text: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var inspectingCard by remember { mutableStateOf<CardAnswer?>(null) }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        // Sticky Anonymous Cards Strip on top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "کارت‌های پاسخ (کلیک جهت مطالعه):",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isLocalUserMentalist) {
                    TextButton(
                        onClick = onProceedToGuessing,
                        modifier = Modifier.testTag("finish_interrogation_button")
                    ) {
                        Text("اتمام بازجویی و حدس‌زنی ❯", color = CrimsonLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cards) { card ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CardAnonBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardAnonBorder),
                        modifier = Modifier
                            .width(130.dp)
                            .clickable { inspectingCard = card }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = card.label,
                                    color = GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Flag,
                                    contentDescription = "گزارش",
                                    tint = TextMuted,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onReportMessage("صاحب ${card.label}", card.text) }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = card.text,
                                color = TextPrimaryDark,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // AFK warning indicator
        if (afkWarningActive) {
            Surface(
                color = DangerRed.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ هشدار عدم فعالیت (AFK): سکوت طولانی باعث کسر ۱۰۰ XP جریمه خواهد شد!",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { msg ->
                ChatBubbleItem(
                    message = msg,
                    onReport = { onReportMessage(msg.senderName, msg.text) }
                )
            }
        }

        // Quick Probe Chips (Visible for Mentalist)
        if (isLocalUserMentalist) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val probes = listOf(
                    "کارت الف تابلوعه کار توئه!",
                    "چرا انقدر دستپاچه شدی؟",
                    "علامت تعجب‌های کارت ج دستتو رو کرد!",
                    "به نظرم کارت ب دقیقاً سبک توئه."
                )
                items(probes) { probe ->
                    SuggestionChip(
                        onClick = { onQuickProbe(probe) },
                        label = { Text(probe, fontSize = 11.sp, color = TextPrimaryDark) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceElevated),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = SurfaceBorder
                        )
                    )
                }
            }
        }

        // Chat input bar
        Surface(
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userChatInput,
                    onValueChange = onUserChatInputChange,
                    placeholder = {
                        Text(
                            text = if (isLocalUserMentalist) "سوال بازجویی از مظنونین..." else "بلوف بزنید یا لحن دیگران را تقلید کنید...",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedBorderColor = if (isLocalUserMentalist) CrimsonPrimary else MysticPurpleLight,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).testTag("chat_input_field"),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSendMessage,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isLocalUserMentalist) CrimsonPrimary else MysticPurple)
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "ارسال پیام",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Modal inspecting card
    inspectingCard?.let { card ->
        AlertDialog(
            onDismissRequest = { inspectingCard = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(card.label, color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { onReportMessage("صاحب ${card.label}", card.text) }) {
                        Icon(imageVector = Icons.Outlined.Flag, contentDescription = "گزارش", tint = CrimsonPrimary)
                    }
                }
            },
            text = {
                Surface(
                    color = CardAnonBg,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardAnonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = card.text,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { inspectingCard = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
                ) {
                    Text("بستن", color = TextPrimaryDark)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun ChatBubbleItem(message: ChatMessage, onReport: () -> Unit) {
    if (message.isSystem) {
        Surface(
            color = SurfaceElevated.copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(
                text = message.text,
                color = GoldLight,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(6.dp)
            )
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromMentalist) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (message.isFromMentalist) Arrangement.End else Arrangement.Start
        ) {
            Text(
                text = if (message.isFromMentalist) "🕵️ ${message.senderName} (بازجو)" else message.senderName,
                color = if (message.isFromMentalist) CrimsonLight else TextSecondaryDark,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = "گزارش",
                tint = TextMuted,
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onReport() }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (message.isFromMentalist) 14.dp else 2.dp,
                bottomEnd = if (message.isFromMentalist) 2.dp else 14.dp
            ),
            color = if (message.isFromMentalist) CrimsonDark.copy(alpha = 0.85f) else SurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (message.isFromMentalist) CrimsonPrimary else SurfaceBorder
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

// --- GUESSING PHASE SCREEN (30s) ---
@Composable
fun GuessingPhaseScreen(
    players: List<Player>,
    cards: List<CardAnswer>,
    selectedCard: CardAnswer?,
    isLocalUserMentalist: Boolean,
    onSelectCard: (CardAnswer) -> Unit,
    onAssignPlayer: (String) -> Unit,
    onConfirmDeductions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nonMentalistPlayers = players // All players can be assigned (or suspects)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isLocalUserMentalist) CrimsonDark else SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLocalUserMentalist) "مرحله حدس‌زنی و اتصال کارت‌ها" else "منتالیست در حال حدس‌زنی است...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isLocalUserMentalist)
                            "روی یک کارت کلیک کنید، سپس بازیکن مظنون را انتخاب کنید تا به هم متصل شوند."
                        else
                            "اگر منتالیست دست شما را نخواند، ۱۵۰+ XP فریب موفق پاداش می‌گیرید!",
                        color = GoldLight,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        // Anonymous Cards List
        item {
            Text(
                text = "۱. انتخاب کارت پاسخ:",
                color = GoldAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(cards) { card ->
            val isSelected = selectedCard?.id == card.id
            val assignedPlayer = players.find { it.id == card.assignedPlayerId }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) SurfaceElevated else CardAnonBg,
                border = androidx.compose.foundation.BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) GoldAccent else if (assignedPlayer != null) ElectricCyan else CardAnonBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable(enabled = isLocalUserMentalist) { onSelectCard(card) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.label,
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = card.text,
                            color = TextPrimaryDark,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (assignedPlayer != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F3642),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(assignedPlayer.avatarEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = assignedPlayer.name,
                                    color = ElectricCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (isSelected) "انتخاب مظنون ❯" else "بدون اتصال",
                            color = if (isSelected) GoldAccent else TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        // Suspects selection row
        item {
            Text(
                text = "۲. انتخاب بازیکن مظنون:",
                color = GoldAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                nonMentalistPlayers.forEach { player ->
                    PlayerAvatarBadge(
                        player = player,
                        isSelected = false,
                        showScore = false,
                        onClick = if (isLocalUserMentalist && selectedCard != null) {
                            { onAssignPlayer(player.id) }
                        } else null
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        if (isLocalUserMentalist) {
            item {
                Button(
                    onClick = onConfirmDeductions,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_deductions_button")
                ) {
                    Text(
                        text = "تایید حدس‌ها و افشای کارت‌ها 🔥",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// --- REVEAL PHASE SCREEN ---
@Composable
fun RevealPhaseScreen(
    results: List<RoundRevealResult>,
    activeStepIndex: Int,
    isDone: Boolean,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        item {
            Text(
                text = "افشای کارت‌ها و شمارش امتیازات",
                color = GoldAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "کارت‌ها یک به یک رو می‌شوند...",
                color = TextSecondaryDark,
                fontSize = 12.sp
            )
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        items(results.take(activeStepIndex + 1)) { res ->
            val isSuccess = res.isCorrect
            val borderColor = if (isSuccess) SuccessGreen else CrimsonPrimary
            val bannerBg = if (isSuccess) Color(0xFF143021) else Color(0xFF38141F)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(res.card.label, color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bannerBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                        ) {
                            Text(
                                text = if (isSuccess) "✓ مچ‌گیری موفق!" else "🎭 فریب موفق بازجو!",
                                color = if (isSuccess) SuccessGreen else CrimsonLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "\"${res.card.text}\"",
                        color = Color.White,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SurfaceBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Authorship & Scoring breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("نویسنده واقعی:", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "${res.authorPlayer.avatarEmoji} ${res.authorPlayer.name}",
                                color = TextPrimaryDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text("حدس منتالیست:", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = res.guessedPlayer?.let { "${it.avatarEmoji} ${it.name}" } ?: "بدون حدس",
                                color = TextPrimaryDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("تغییرات امتیاز:", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = if (isSuccess) "بازجو: +۱۰۰ | نویسنده: -۴۰" else "بازجو: -۵۰ | نویسنده: +۱۵۰",
                                color = if (isSuccess) SuccessGreen else GoldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (isDone) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onProceed,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("proceed_from_reveal_button")
                ) {
                    Text(
                        text = "مشاهده جدول راند و ادامه ❯",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- ROUND SUMMARY & GAME OVER SCREEN ---
@Composable
fun RoundSummaryScreen(
    players: List<Player>,
    currentRound: Int,
    totalRounds: Int,
    isFinalGameOver: Boolean,
    onNextRound: () -> Unit,
    onReturnHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedPlayers = players.sortedByDescending { it.matchScore }
    val winner = sortedPlayers.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            Text(
                text = if (isFinalGameOver) "🏆 پایان مسابقه سیاه بازی" else "📊 جدول رده‌بندی راند $currentRound",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isFinalGameOver) "برترین ذهن‌خوانان و فریب‌دهندگان مسابقه" else "راند بعدی نقش بازجو به بازیکن بعدی منتقل می‌شود",
                color = TextSecondaryDark,
                fontSize = 12.sp
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Leaderboard cards
        items(sortedPlayers) { player ->
            val isLeader = player.id == winner?.id
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isLeader) SurfaceElevated else SurfaceDark,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isLeader) GoldAccent else SurfaceBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLeader) "🥇" else "#${sortedPlayers.indexOf(player) + 1}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(player.avatarEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = player.name,
                            color = if (player.isLocalUser) GoldAccent else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        RankChip(rankTier = player.rankTier, level = player.level)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${player.matchScore} XP",
                            color = ElectricCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (player.roundDeltaXp != 0) {
                            Text(
                                text = if (player.roundDeltaXp > 0) "+${player.roundDeltaXp}" else "${player.roundDeltaXp}",
                                color = if (player.roundDeltaXp > 0) SuccessGreen else DangerRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            if (isFinalGameOver) {
                Button(
                    onClick = onReturnHome,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("return_home_button")
                ) {
                    Text(
                        text = "ثبت نتایج و بازگشت به صفحه اصلی",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onNextRound,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_round_button")
                ) {
                    Text(
                        text = "آغاز راند بعدی ❯",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
