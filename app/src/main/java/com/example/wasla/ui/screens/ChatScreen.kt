package com.example.wasla.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wasla.data.model.Chat
import com.example.wasla.data.model.Device
import com.example.wasla.data.model.Message
import com.example.wasla.data.util.WaslaImageUtils
import com.example.wasla.ui.components.FormatUtils
import com.example.wasla.ui.components.WaslaAvatar
import com.example.wasla.ui.theme.WaslaAccent
import com.example.wasla.ui.theme.WaslaBackground
import com.example.wasla.ui.theme.WaslaBubbleMe
import com.example.wasla.ui.theme.WaslaBubbleOther
import com.example.wasla.ui.theme.WaslaCardBorder
import com.example.wasla.ui.theme.WaslaError
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
    onSendImage: (String, String) -> Unit,
    onDeleteChat: () -> Unit,
    onAcceptChat: () -> Unit,
    onRejectChat: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    var selectedImagePreviewUri by remember { mutableStateOf<Uri?>(null) }
    var viewingFullImageUri by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val processed = WaslaImageUtils.processPickedImage(context, uri)
            if (processed != null) {
                selectedImagePath = processed.first
                selectedImagePreviewUri = uri
            }
        }
    }

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

                // Delete Chat Action Button
                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.testTag("chat_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف المحادثة",
                        tint = WaslaError.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
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
                            text = "بداية المحادثة • اتصال مباشر وسريع",
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
                            text = "قل أول كلمة أو أرسل صورة — الرسائل تصل فوراً!",
                            color = WaslaTextTertiary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(chat.messages, key = { it.id }) { msg ->
                    val isMe = msg.senderId == device.id
                    ChatMessageBubble(
                        message = msg,
                        isMe = isMe,
                        showSender = isGroup && !isMe,
                        onImageClick = { imgUri ->
                            viewingFullImageUri = imgUri
                        }
                    )
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
                            text = "وصلك طلب لبدء هذه المحادثة من ${chat.name}",
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WaslaSurface)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder))
                ) {
                    // Selected Image Preview Strip
                    if (selectedImagePath != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(selectedImagePath)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "معاينة الصورة المرفقة",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, WaslaPrimary, RoundedCornerShape(8.dp))
                                )
                                Text(
                                    text = "تم اختيار صورة للإرسال",
                                    color = WaslaTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            IconButton(
                                onClick = {
                                    selectedImagePath = null
                                    selectedImagePreviewUri = null
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إلغاء الصورة",
                                    tint = WaslaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Input row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Image attachment button
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(WaslaSurfaceVariant)
                                .testTag("attach_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "إرسال صورة",
                                tint = if (selectedImagePath != null) WaslaPrimary else WaslaTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field"),
                            placeholder = {
                                Text(
                                    if (selectedImagePath != null) "أضف تعليقاً على الصورة..." else "اكتب رسالتك...",
                                    color = WaslaTextTertiary
                                )
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

                        val canSend = inputText.trim().isNotEmpty() || selectedImagePath != null

                        IconButton(
                            onClick = {
                                if (selectedImagePath != null) {
                                    onSendImage(selectedImagePath!!, inputText.trim())
                                    selectedImagePath = null
                                    selectedImagePreviewUri = null
                                    inputText = ""
                                } else if (inputText.trim().isNotEmpty()) {
                                    onSendMessage(inputText.trim())
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (canSend) WaslaPrimary else WaslaSurfaceVariant)
                                .testTag("send_message_button"),
                            enabled = canSend
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال",
                                tint = if (canSend) WaslaOnPrimary else WaslaTextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Chat Deletion
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "حذف المحادثة؟",
                    color = WaslaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف هذه المحادثة مع «${chat.name}» وجميع الرسائل والصور؟ لن تتمكن من التراجع عن هذه الخطوة.",
                    color = WaslaTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteChat()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WaslaError,
                        contentColor = Color.White
                    )
                ) {
                    Text("حذف نهائي", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("إلغاء", color = WaslaTextSecondary)
                }
            },
            containerColor = WaslaSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Full-screen Image Viewer Modal
    if (viewingFullImageUri != null) {
        Dialog(
            onDismissRequest = { viewingFullImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable { viewingFullImageUri = null }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(viewingFullImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "عرض الصورة بالحجم الكامل",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )

                IconButton(
                    onClick = { viewingFullImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: Message,
    isMe: Boolean,
    showSender: Boolean,
    onImageClick: (String) -> Unit
) {
    val context = LocalContext.current
    val hasImage = !message.imageUri.isNullOrBlank()

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
                Column(
                    modifier = Modifier.padding(
                        horizontal = if (hasImage) 6.dp else 14.dp,
                        vertical = if (hasImage) 6.dp else 10.dp
                    )
                ) {
                    // Display Image if message has image
                    if (hasImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageClick(message.imageUri!!) }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(message.imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "صورة مرسلة",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 140.dp, max = 240.dp)
                            )
                        }

                        if (message.text.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            color = if (isMe) WaslaTextPrimary else WaslaOnBubbleOther,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = if (hasImage) Modifier.padding(horizontal = 8.dp, vertical = 4.dp) else Modifier
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(horizontal = if (hasImage) 6.dp else 0.dp),
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
