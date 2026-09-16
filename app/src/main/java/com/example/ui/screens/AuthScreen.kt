package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.D1ConnectionState
import com.example.ui.theme.*

val AVAILABLE_AVATARS = listOf(
    "🕵️", "🎭", "🕶️", "👁️", "🎩", "♟️", "🐺", "🔮", "🃏", "🦉", "👑", "🛡️"
)

enum class AuthMode {
    REGISTER,
    LOGIN
}

@Composable
fun AuthScreen(
    d1State: D1ConnectionState,
    workerUrl: String,
    isLoading: Boolean,
    errorMessage: String?,
    onRegister: (username: String, email: String?, password: String, avatar: String) -> Unit,
    onLogin: (username: String, password: String) -> Unit,
    onOpenD1Settings: () -> Unit,
    onOpenD1Guide: () -> Unit,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.REGISTER) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("🕵️") }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0F18),
                        Color(0xFF131626),
                        DeepObsidian
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Hero Badge & Mystery Logo
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                CrimsonPrimary.copy(alpha = 0.35f),
                                Color(0xFF1B1F33)
                            )
                        )
                    )
                    .border(2.dp, Brush.linearGradient(listOf(CrimsonPrimary, GoldAccent)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedAvatar,
                    fontSize = 42.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "سیاه‌بازی",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "پروژه تحلیل ذهن و کشف حقیقت",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = GoldAccent.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Segmented Auth Toggle Tab
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF161928),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Register Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                authMode = AuthMode.REGISTER
                            }
                            .testTag("tab_register"),
                        color = if (authMode == AuthMode.REGISTER) CrimsonPrimary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "ساخت اکانت جدید",
                            color = Color.White,
                            fontWeight = if (authMode == AuthMode.REGISTER) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    // Login Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                authMode = AuthMode.LOGIN
                            }
                            .testTag("tab_login"),
                        color = if (authMode == AuthMode.LOGIN) CrimsonPrimary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "ورود به حساب",
                            color = Color.White,
                            fontWeight = if (authMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error Message Banner
            AnimatedVisibility(
                visible = !errorMessage.isNullOrBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CrimsonPrimary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFFB4BC),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Input Fields Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceElevated.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    if (authMode == AuthMode.REGISTER) {
                        // Avatar Carousel
                        Text(
                            text = "انتخاب نقاب کارآگاهی / آواتار:",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AVAILABLE_AVATARS.forEach { emoji ->
                                val isSelected = emoji == selectedAvatar
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) CrimsonPrimary.copy(alpha = 0.3f)
                                            else Color(0xFF1E2235)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        )
                                        .clickable { selectedAvatar = emoji }
                                        .testTag("avatar_$emoji"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_username"),
                        label = {
                            Text(if (authMode == AuthMode.REGISTER) "نام کاربری (حداقل ۳ حرف)" else "نام کاربری یا ایمیل")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = GoldAccent)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CrimsonPrimary,
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = if (authMode == AuthMode.REGISTER) ImeAction.Next else ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (authMode == AuthMode.REGISTER) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Email Field (Optional)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_email"),
                            label = { Text("ایمیل (اختیاری جهت بازیابی)") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = CrimsonPrimary,
                                unfocusedLabelColor = Color.Gray
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_password"),
                        label = { Text("کلمه عبور (حداقل ۴ کاراکتر)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.testTag("toggle_password_visibility")
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "نمایش کلمه عبور",
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = CrimsonPrimary,
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (authMode == AuthMode.REGISTER) {
                                    onRegister(username, email.ifBlank { null }, password, selectedAvatar)
                                } else {
                                    onLogin(username, password)
                                }
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (authMode == AuthMode.REGISTER) {
                                onRegister(username, email.ifBlank { null }, password, selectedAvatar)
                            } else {
                                onLogin(username, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_auth_submit"),
                        enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonPrimary,
                            disabledContainerColor = CrimsonPrimary.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (authMode == AuthMode.REGISTER) "ثبت‌نام و ورود به بازی" else "ورود به بازی",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // D1 Server Status and Config
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF141726),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (d1State) {
                                        is D1ConnectionState.Connected -> SuccessGreen
                                        is D1ConnectionState.Checking -> GoldAccent
                                        else -> CrimsonPrimary
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (d1State) {
                                is D1ConnectionState.Connected -> "سرور D1 متصل است (${d1State.latencyMs}ms)"
                                is D1ConnectionState.Checking -> "در حال بررسی دیتابیس D1..."
                                else -> "دیتابیس Cloudflare D1"
                            },
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onOpenD1Guide,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("btn_auth_d1_guide")
                        ) {
                            Icon(
                                Icons.Outlined.HelpOutline,
                                contentDescription = "راهنما",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onOpenD1Settings,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("btn_auth_d1_settings")
                        ) {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = "تنظیمات D1",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
