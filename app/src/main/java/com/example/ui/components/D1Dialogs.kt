package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.remote.D1ConnectionState
import com.example.ui.theme.*

@Composable
fun D1SettingsDialog(
    initialUrl: String,
    connectionState: D1ConnectionState,
    onSaveUrl: (String) -> Unit,
    onTestConnection: () -> Unit,
    onOpenGuide: () -> Unit,
    onDismiss: () -> Unit
) {
    var urlText by remember(initialUrl) { mutableStateOf(initialUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("☁️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تنظیمات پایگاه داده Cloudflare D1",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "آدرس ورکر کلودفلر (Cloudflare Worker URL):",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    placeholder = {
                        Text("https://siahbazi-backend.workers.dev", color = TextMuted, fontSize = 12.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Connection Status Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (connectionState) {
                            is D1ConnectionState.Connected -> SuccessGreen
                            is D1ConnectionState.Error -> CrimsonPrimary
                            else -> SurfaceBorder
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        when (connectionState) {
                            is D1ConnectionState.Connected -> {
                                Text(
                                    text = "🟢 اتصال موفق به دیتابیس D1",
                                    color = SuccessGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تاخیر پاسخگویی: ${connectionState.latencyMs}ms\nتعداد سوالات در D1: ${connectionState.questionCount}\nاتاق‌های فعال: ${connectionState.activeRoomsCount}",
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            is D1ConnectionState.Checking -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = GoldAccent,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("در حال بررسی اتصال به ورکر کلودفلر...", color = GoldAccent, fontSize = 12.sp)
                                }
                            }
                            is D1ConnectionState.Error -> {
                                Text(
                                    text = "🔴 خطای اتصال به D1",
                                    color = CrimsonPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = connectionState.message,
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                            }
                            is D1ConnectionState.Idle -> {
                                Text(
                                    text = "وضعیت: در انتظار تست اتصال",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onTestConnection,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تست اتصال", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onSaveUrl(urlText)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ذخیره و اتصال", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onOpenGuide,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("مشاهده راهنمای ۳ مرحله‌ای ساخت دیتابیس D1", color = ElectricCyan, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun D1GuideDialog(
    onDismiss: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "راهنمای استقرار Cloudflare D1",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "فایل‌های کامل ورکر و جداول دیتابیس آماده شده‌اند:\n• پوشه /cloudflare/worker.js\n• ساختار جداول /cloudflare/schema.sql\n• فایل کانفیگ /cloudflare/wrangler.toml",
                    color = GoldLight,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                StepCard(
                    stepNum = "۱",
                    title = "ایجاد دیتابیس ابری D1",
                    cmd = "npx wrangler d1 create siahbazi-db",
                    clipboard = clipboard
                )

                Spacer(modifier = Modifier.height(10.dp))

                StepCard(
                    stepNum = "۲",
                    title = "اجرای اسکیما و سوالات پیش‌فرض",
                    cmd = "npx wrangler d1 execute siahbazi-db --file=schema.sql --remote",
                    clipboard = clipboard
                )

                Spacer(modifier = Modifier.height(10.dp))

                StepCard(
                    stepNum = "۳",
                    title = "انتشار ورکر",
                    cmd = "npx wrangler deploy",
                    clipboard = clipboard
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "پس از deploy، لینک خروجی ورکر را در بخش تنظیمات وارد کنید.",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("متوجه شدم", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNum: String,
    title: String,
    cmd: String,
    clipboard: androidx.compose.ui.platform.ClipboardManager
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "مرحله $stepNum: $title",
                color = TextPrimaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidian, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cmd,
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { clipboard.setText(AnnotatedString(cmd)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "کپی", tint = GoldAccent, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun JoinRoomDialog(
    initialCode: String,
    errorMessage: String?,
    isLoading: Boolean,
    onJoin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var codeInput by remember(initialCode) { mutableStateOf(initialCode) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, MysticPurpleLight),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ورود به اتاق بازی",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "کد ۵ رقمی ارسالی توسط میزبان را وارد کنید:",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it.uppercase().trim().take(6) },
                    placeholder = { Text("مثال: MNTL7", color = TextMuted, fontSize = 16.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = GoldLight,
                        unfocusedTextColor = GoldLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = CrimsonPrimary, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onJoin(codeInput) },
                    enabled = codeInput.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("اتصال و ورود به لابی", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
