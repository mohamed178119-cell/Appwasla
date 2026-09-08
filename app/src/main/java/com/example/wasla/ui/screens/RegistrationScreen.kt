package com.example.wasla.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wasla.ui.theme.WaslaAccent
import com.example.wasla.ui.theme.WaslaBackground
import com.example.wasla.ui.theme.WaslaCardBorder
import com.example.wasla.ui.theme.WaslaOnPrimary
import com.example.wasla.ui.theme.WaslaPrimary
import com.example.wasla.ui.theme.WaslaSurface
import com.example.wasla.ui.theme.WaslaSurfaceVariant
import com.example.wasla.ui.theme.WaslaTextPrimary
import com.example.wasla.ui.theme.WaslaTextSecondary
import com.example.wasla.ui.theme.WaslaTextTertiary

@Composable
fun RegistrationScreen(
    onRegister: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaslaBackground)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo & Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(WaslaPrimary, WaslaAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CellTower,
                    contentDescription = "وصلة",
                    tint = WaslaOnPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "وصلة",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WaslaTextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "محادثات خاصة تبدأ من كود ثابت",
                fontSize = 15.sp,
                color = WaslaPrimary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "وصلة تمنح كل تثبيت رمزاً فريداً وثابتاً. شاركه مع من تثق، وابدأ محادثة مباشرة بدون حسابات طويلة أو تعقيد.",
                fontSize = 13.sp,
                color = WaslaTextSecondary,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Registration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WaslaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "الخطوة الأولى",
                        color = WaslaAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "عرّف نفسك لوصلة",
                        color = WaslaTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "الاسم الظاهر",
                        color = WaslaTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 40) name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("display_name_input"),
                        placeholder = {
                            Text("مثال: محمد", color = WaslaTextTertiary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WaslaTextPrimary,
                            unfocusedTextColor = WaslaTextPrimary,
                            focusedBorderColor = WaslaPrimary,
                            unfocusedBorderColor = WaslaCardBorder,
                            focusedContainerColor = WaslaSurfaceVariant,
                            unfocusedContainerColor = WaslaSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onRegister(name.trim().ifEmpty { "نسخة وصلة" }) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("register_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WaslaPrimary,
                            contentColor = WaslaOnPrimary
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "أنشئ وصلة وابدأ الآن",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = WaslaSurfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = WaslaPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "خاص بك دائماً",
                            color = WaslaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = WaslaSurfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = WaslaAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "بسيط وبلا كلمات مرور",
                            color = WaslaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "وصلة — من تطوير Mohamed Saad",
                color = WaslaTextTertiary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
