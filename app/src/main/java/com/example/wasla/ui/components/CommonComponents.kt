package com.example.wasla.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wasla.ui.theme.WaslaAccent
import com.example.wasla.ui.theme.WaslaBackground
import com.example.wasla.ui.theme.WaslaCardBorder
import com.example.wasla.ui.theme.WaslaOnPrimary
import com.example.wasla.ui.theme.WaslaOnline
import com.example.wasla.ui.theme.WaslaPrimary
import com.example.wasla.ui.theme.WaslaSurface
import com.example.wasla.ui.theme.WaslaSurfaceVariant
import com.example.wasla.ui.theme.WaslaTextPrimary
import com.example.wasla.ui.theme.WaslaTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val timeFormat = SimpleDateFormat("h:mm a", Locale("ar"))
    private val dateFormat = SimpleDateFormat("d MMMM", Locale("ar"))

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))
    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
}

@Composable
fun WaslaAvatar(
    name: String,
    isOnline: Boolean = false,
    size: Dp = 48.dp,
    isGroup: Boolean = false
) {
    val initial = name.trim().take(2).ifEmpty { "و" }

    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size / 3))
                .background(
                    if (isGroup) Brush.linearGradient(listOf(Color(0xFF352E63), Color(0xFF22365A)))
                    else Brush.linearGradient(listOf(WaslaAccent.copy(alpha = 0.8f), Color(0xFF4A3FA0)))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isGroup) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = WaslaAccent,
                    modifier = Modifier.size(size * 0.55f)
                )
            } else {
                Text(
                    text = initial,
                    color = WaslaTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp
                )
            }
        }

        if (isOnline && !isGroup) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .clip(CircleShape)
                    .background(WaslaOnline)
                    .border(2.dp, WaslaBackground, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun WaslaCodeCard(
    code: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("device_code_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WaslaSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = WaslaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "رمزك الخاص في وصلة",
                        color = WaslaTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onCopy() }
                        .testTag("copy_code_button"),
                    color = WaslaSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ الرمز",
                            tint = WaslaPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "نسخ",
                            color = WaslaPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = code,
                color = WaslaTextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "شاركه مع من تثق فقط لبدء محادثات خاصة",
                color = WaslaTextSecondary.copy(alpha = 0.8f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WaslaToast(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("toast_notice"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF162E3D),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF32556A)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(WaslaPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = WaslaPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = message,
                    color = Color(0xFFD8FFF7),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
