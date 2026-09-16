package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyQuest
import com.example.model.GameBadge
import com.example.ui.theme.*

@Composable
fun QuestsAndBadgesDialog(
    quests: List<DailyQuest>,
    badges: List<GameBadge>,
    onClaimQuest: (DailyQuest) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Quests, 1: Badges

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedTab == 0) "ماموریت‌های روزانه" else "مدال‌های افتخار",
                    color = GoldAccent,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    style = LocalTextStyle.current.copy(shadow = GamingTextShadow)
                )

                Row {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("ماموریت‌ها", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("مدال‌ها", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldAccent,
                            selectedLabelColor = DeepObsidian
                        )
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) {
                    items(quests) { quest ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (quest.isComplete && !quest.isClaimed) NeonCyberGreen.copy(alpha = 0.6f) else SurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = quest.title,
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = GoldAccent.copy(alpha = 0.15f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "+${quest.xpReward} XP",
                                                color = GoldLight,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = NeonCyberGreen.copy(alpha = 0.15f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyberGreen.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "+۱۰۰ 🪙",
                                                color = NeonCyberGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = quest.description,
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    LinearProgressIndicator(
                                        progress = { (quest.currentProgress.toFloat() / quest.targetProgress.toFloat()).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (quest.isComplete) NeonCyberGreen else GoldAccent,
                                        trackColor = DeepObsidian
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${quest.currentProgress}/${quest.targetProgress}",
                                        color = if (quest.isComplete) NeonCyberGreen else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (quest.isClaimed) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Black.copy(alpha = 0.3f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "پاداش دریافت شد ✓",
                                            color = SlateGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                } else if (quest.isComplete) {
                                    Button(
                                        onClick = { onClaimQuest(quest) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyberGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .testTag("btn_claim_quest_${quest.id}")
                                    ) {
                                        Text(
                                            text = "دریافت پاداش (+XP و سکه) 🎁",
                                            color = DeepObsidian,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "پس از انجام مسابقه و تکمیل، پاداش را از اینجا تحویل بگیرید.",
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(badges) { badge ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (badge.isUnlocked) SurfaceElevated else DeepObsidian,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (badge.isUnlocked) GoldAccent else SurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(badge.iconEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = badge.title,
                                        color = if (badge.isUnlocked) GoldLight else TextMuted,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = badge.description,
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                }
                                if (badge.isUnlocked) {
                                    Text("✓", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
            ) {
                Text("بستن", color = TextPrimaryDark)
            }
        },
        containerColor = SurfaceDark
    )
}

