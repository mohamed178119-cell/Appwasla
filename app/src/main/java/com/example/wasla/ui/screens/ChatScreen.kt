package com.example.wasla.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wasla.data.model.Chat
import com.example.wasla.data.model.Device
import com.example.wasla.data.model.Message
import com.example.wasla.ui.components.FormatUtils
import com.example.wasla.ui.components.WaslaAvatar
import com.example.wasla.ui.theme.WaslaAccent
import com.example.wasla.ui.theme.WaslaBackground
import com.example.wasla.ui.theme.WaslaBubbleMe
import com.example.wasla.ui.theme.WaslaBubbleOther
import com.example.wasla.ui.theme.WaslaCardBorder
import com.example.wasla.ui.theme.WaslaOnBubbleOther
import com.example.wasla.ui.theme.WaslaOnPrimary
import com.example.wasla.ui.theme.WaslaOnline
import com.example.wasla.ui.theme.WaslaPrimary
import com.example.wasla.ui.theme.WaslaSurface
import com.example.wasla.ui.theme.WaslaSurfaceVariant
import com.example.wasla.ui.theme.WaslaTextPrimary
import com.example.wasla.ui.theme.WaslaTextSecondary
import com.example.wasla.ui.theme.WaslaTextTertiary

@Composable
fun ChatScreen(
    chat: Chat,
    device: Device,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onAcceptChat: () -> Unit,
    onRejectChat: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val otherMember = chat.members.firstOrNull { it.deviceId != device.id }
    val isOnline = otherMember?.online ?: false
    val isGroup = chat.type == "group"

    LaunchedEffect(chat.messages.size) {
        if (chat.messages.isNotEmpty()) {
            listState.animateScrollToItem(chat.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WaslaBackground)
            .imePadding()
    ) {
        // Chat Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = WaslaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "الرجوع",
                        tint = WaslaTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                WaslaAvatar(
                    name = chat.name,
                    isOnline = isOnline,
                    size = 42.dp,
                    isGroup = isGroup
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chat.name,
                        color = WaslaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Text(
                        text = if (isGroup) "${chat.members.size} أعضاء"
                        else if (isOnline) "متصل الآن"
                        else "غير متصل",
                        color = if (isOnline && !isGroup) WaslaOnline else WaslaTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Intro badge
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = WaslaSurfaceVariant.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                    ) {
                        Text(
                            text = "بداية هذه الوصلة • لا أطراف ثالثة",
                            color = WaslaTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (chat.messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "قل أول كلمة — لا تحتاج وصلة جيدة إلى مقدمة طويلة.",
                            color = WaslaTextTertiary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(chat.messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId == device.id
                    ChatMessageBubble(message = msg, isMe = isMe, showSender = isGroup && !isMe)
                }
            }
        }

        // Bottom Controls / Composer / Status
        when (chat.status) {
            "pending_outgoing" -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = WaslaSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = WaslaAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "بانتظار قبول الطرف الآخر — ستتمكن من المراسلة فور الموافقة.",
                            color = WaslaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            "pending_incoming" -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = WaslaSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "وصلك طلب لبدء هذه المحادثة الخاصة.",
                            color = WaslaTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onAcceptChat,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_accept_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WaslaPrimary,
                                    contentColor = WaslaOnPrimary
                                )
                            ) {
                                Text("قبول المحادثة", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onRejectChat,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = WaslaTextSecondary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                            ) {
                                Text("تجاهل")
                            }
                        }
                    }
                }
            }

            else -> {
                // Active Message Composer
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = WaslaSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field"),
                            placeholder = {
                                Text("اكتب رسالتك...", color = WaslaTextTertiary)
                            },
                            maxLines = 4,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WaslaTextPrimary,
                                unfocusedTextColor = WaslaTextPrimary,
                                focusedBorderColor = WaslaPrimary,
                                unfocusedBorderColor = WaslaCardBorder,
                                focusedContainerColor = WaslaSurfaceVariant,
                                unfocusedContainerColor = WaslaSurfaceVariant
                            )
                        )

                        IconButton(
                            onClick = {
                                if (inputText.trim().isNotEmpty()) {
                                    onSendMessage(inputText.trim())
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (inputText.trim().isNotEmpty()) WaslaPrimary else WaslaSurfaceVariant)
                                .testTag("send_message_button"),
                            enabled = inputText.trim().isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال",
                                tint = if (inputText.trim().isNotEmpty()) WaslaOnPrimary else WaslaTextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: Message,
    isMe: Boolean,
    showSender: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            if (showSender && message.senderCode != null) {
                Text(
                    text = message.senderCode,
                    color = WaslaAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                color = if (isMe) WaslaBubbleMe else WaslaBubbleOther,
                border = if (isMe) androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder) else null
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        color = if (isMe) WaslaTextPrimary else WaslaOnBubbleOther,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = FormatUtils.formatTime(message.createdAt),
                            color = if (isMe) WaslaTextTertiary else WaslaOnBubbleOther.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )

                        if (isMe) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = WaslaPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
