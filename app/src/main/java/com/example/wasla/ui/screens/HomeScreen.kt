package com.example.wasla.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Sync
import com.example.wasla.data.cloud.CloudStatus
import com.example.wasla.data.model.Chat
import com.example.wasla.data.model.ChatRequest
import com.example.wasla.data.model.Device
import com.example.wasla.ui.components.FormatUtils
import com.example.wasla.ui.components.WaslaAvatar
import com.example.wasla.ui.components.WaslaCodeCard
import com.example.wasla.ui.theme.WaslaAccent
import com.example.wasla.ui.theme.WaslaBackground
import com.example.wasla.ui.theme.WaslaCardBorder
import com.example.wasla.ui.theme.WaslaError
import com.example.wasla.ui.theme.WaslaOffline
import com.example.wasla.ui.theme.WaslaOnPrimary
import com.example.wasla.ui.theme.WaslaOnline
import com.example.wasla.ui.theme.WaslaPrimary
import com.example.wasla.ui.theme.WaslaSurface
import com.example.wasla.ui.theme.WaslaSurfaceVariant
import com.example.wasla.ui.theme.WaslaTextPrimary
import com.example.wasla.ui.theme.WaslaTextSecondary
import com.example.wasla.ui.theme.WaslaTextTertiary
import com.example.wasla.ui.viewmodel.WaslaTab

@Composable
fun HomeScreen(
    device: Device,
    chats: List<Chat>,
    requests: List<ChatRequest>,
    currentTab: WaslaTab,
    cloudStatus: CloudStatus,
    onSelectTab: (WaslaTab) -> Unit,
    onTogglePresence: () -> Unit,
    onOpenChat: (String) -> Unit,
    onDeleteChat: (String) -> Unit,
    onNewRequest: () -> Unit,
    onNewGroup: () -> Unit,
    onRespondRequest: (String, Boolean) -> Unit,
    onSimulateIncomingRequest: () -> Unit,
    onOpenCloudSettings: () -> Unit = {},
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val pendingIncomingRequests = requests.filter { it.status == "pending" && it.toDeviceId == device.id }
    val outgoingRequests = requests.filter { it.fromDeviceId == device.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WaslaBackground)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(listOf(WaslaPrimary, WaslaAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = null,
                        tint = WaslaOnPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "وصلة",
                        color = WaslaTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "أهلاً، ${device.displayName}",
                        color = WaslaTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Clean Presence Toggle (Server details are hidden like WhatsApp)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onTogglePresence() }
                    .testTag("presence_toggle_button"),
                color = WaslaSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (device.online) WaslaOnline else WaslaOffline)
                    )
                    Text(
                        text = if (device.online) "متصل" else "مخفي",
                        color = if (device.online) WaslaOnline else WaslaTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = WaslaBackground,
            contentColor = WaslaPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                    color = WaslaPrimary,
                    height = 3.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(WaslaCardBorder)
                )
            }
        ) {
            Tab(
                selected = currentTab == WaslaTab.CHATS,
                onClick = { onSelectTab(WaslaTab.CHATS) },
                text = {
                    Text(
                        text = "المحادثات (${chats.size})",
                        fontWeight = if (currentTab == WaslaTab.CHATS) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.testTag("tab_chats")
            )

            Tab(
                selected = currentTab == WaslaTab.REQUESTS,
                onClick = { onSelectTab(WaslaTab.REQUESTS) },
                text = {
                    BadgedBox(
                        badge = {
                            if (pendingIncomingRequests.isNotEmpty()) {
                                Badge(
                                    containerColor = WaslaPrimary,
                                    contentColor = WaslaOnPrimary
                                ) {
                                    Text("${pendingIncomingRequests.size}")
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "الطلبات",
                            fontWeight = if (currentTab == WaslaTab.REQUESTS) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_requests")
            )

            Tab(
                selected = currentTab == WaslaTab.ABOUT,
                onClick = { onSelectTab(WaslaTab.ABOUT) },
                text = {
                    Text(
                        text = "عن وصلة",
                        fontWeight = if (currentTab == WaslaTab.ABOUT) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.testTag("tab_about")
            )
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                WaslaTab.CHATS -> ChatsTabContent(
                    device = device,
                    chats = chats,
                    onOpenChat = onOpenChat,
                    onDeleteChat = onDeleteChat,
                    onNewRequest = onNewRequest,
                    onNewGroup = onNewGroup,
                    onCopyCode = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Wasla Code", device.code))
                        onShowToast("تم نسخ رمز جهازك: ${device.code}")
                    }
                )

                WaslaTab.REQUESTS -> RequestsTabContent(
                    incomingRequests = pendingIncomingRequests,
                    outgoingRequests = outgoingRequests,
                    onRespond = onRespondRequest,
                    onNewRequest = onNewRequest,
                    onSimulateIncoming = onSimulateIncomingRequest
                )

                WaslaTab.ABOUT -> AboutTabContent(
                    device = device,
                    onOpenCloudSettings = onOpenCloudSettings,
                    onCopyCode = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Wasla Code", device.code))
                        onShowToast("تم نسخ رمز جهازك: ${device.code}")
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatsTabContent(
    device: Device,
    chats: List<Chat>,
    onOpenChat: (String) -> Unit,
    onDeleteChat: (String) -> Unit,
    onNewRequest: () -> Unit,
    onNewGroup: () -> Unit,
    onCopyCode: () -> Unit
) {
    var chatPendingDeletion by remember { mutableStateOf<Chat?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Device Code Card Banner
        item {
            WaslaCodeCard(code = device.code, onCopy = onCopyCode)
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNewRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_new_request"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WaslaPrimary,
                        contentColor = WaslaOnPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("محادثة جديدة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                OutlinedButton(
                    onClick = onNewGroup,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_new_group"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WaslaAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaAccent.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("مجموعة جديدة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "المحادثات النشطة",
                color = WaslaTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        if (chats.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = WaslaSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(WaslaSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mail,
                                contentDescription = null,
                                tint = WaslaPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "لا توجد محادثات بعد",
                            color = WaslaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "ابدأ بطلب محادثة جديدة عبر كود الطرف الآخر، أو شارك كودك معه.",
                            color = WaslaTextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(chats, key = { it.id }) { chat ->
                val otherMember = chat.members.firstOrNull { it.deviceId != device.id }
                val isOnline = otherMember?.online ?: false
                val isGroup = chat.type == "group"
                val lastMessage = chat.messages.lastOrNull()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onOpenChat(chat.id) }
                        .testTag("chat_item_${chat.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WaslaSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WaslaAvatar(
                            name = chat.name,
                            isOnline = isOnline,
                            size = 48.dp,
                            isGroup = isGroup
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = chat.name,
                                    color = WaslaTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = FormatUtils.formatTime(chat.updatedAt),
                                    color = WaslaTextTertiary,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val previewText = when {
                                    lastMessage == null -> "لا توجد رسائل بعد"
                                    !lastMessage.imageUri.isNullOrBlank() && lastMessage.text.isNotBlank() -> "📷 ${lastMessage.text}"
                                    !lastMessage.imageUri.isNullOrBlank() -> "📷 صورة"
                                    else -> lastMessage.text
                                }

                                Text(
                                    text = previewText,
                                    color = WaslaTextSecondary,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                if (chat.status == "pending_outgoing") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = WaslaSurfaceVariant
                                    ) {
                                        Text(
                                            text = "بانتظار الموافقة",
                                            color = WaslaAccent,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (chat.status == "pending_incoming") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = WaslaPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "طلب وارد",
                                            color = WaslaPrimary,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { chatPendingDeletion = chat },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("delete_chat_${chat.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "حذف المحادثة",
                                        tint = WaslaTextTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for deleting chat from Home Screen
    if (chatPendingDeletion != null) {
        val targetChat = chatPendingDeletion!!
        AlertDialog(
            onDismissRequest = { chatPendingDeletion = null },
            title = {
                Text(
                    text = "حذف المحادثة؟",
                    color = WaslaTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "هل تريد حذف المحادثة مع «${targetChat.name}»؟ سيتم مسح كافة الرسائل والصور نهائياً.",
                    color = WaslaTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val chatId = targetChat.id
                        chatPendingDeletion = null
                        onDeleteChat(chatId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WaslaError,
                        contentColor = Color.White
                    )
                ) {
                    Text("حذف", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatPendingDeletion = null }) {
                    Text("إلغاء", color = WaslaTextSecondary)
                }
            },
            containerColor = WaslaSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun RequestsTabContent(
    incomingRequests: List<ChatRequest>,
    outgoingRequests: List<ChatRequest>,
    onRespond: (String, Boolean) -> Unit,
    onNewRequest: () -> Unit,
    onSimulateIncoming: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Simulator Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WaslaSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اختبار المحادثات",
                            color = WaslaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "يمكنك محاكاة طلب وارد فوري لتجربة قبول الطلبات والرد.",
                            color = WaslaTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onSimulateIncoming,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WaslaPrimary,
                            contentColor = WaslaOnPrimary
                        ),
                        modifier = Modifier.testTag("simulate_request_button")
                    ) {
                        Text("محاكاة طلب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Incoming Requests
        item {
            Text(
                text = "الطلبات الواردة (${incomingRequests.size})",
                color = WaslaTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (incomingRequests.isEmpty()) {
            item {
                Text(
                    text = "لا توجد طلبات واردة بانتظارك حالياً.",
                    color = WaslaTextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(incomingRequests, key = { it.id }) { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("incoming_request_${req.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WaslaSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val senderTitle = if (req.fromDisplayName.isNotBlank()) req.fromDisplayName else "وصلة ${req.fromCode}"
                                WaslaAvatar(name = senderTitle, size = 42.dp)
                                Column {
                                    Text(
                                        text = senderTitle,
                                        color = WaslaTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "كود: ${req.fromCode} • ${FormatUtils.formatTime(req.createdAt)}",
                                        color = WaslaTextTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onRespond(req.id, true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("accept_request_${req.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WaslaPrimary,
                                    contentColor = WaslaOnPrimary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("قبول", fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { onRespond(req.id, false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reject_request_${req.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = WaslaTextSecondary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("تجاهل")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Outgoing Requests
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "الطلبات الصادرة (${outgoingRequests.size})",
                color = WaslaTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (outgoingRequests.isEmpty()) {
            item {
                Text(
                    text = "لم ترسل أي طلبات محادثة بعد.",
                    color = WaslaTextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(outgoingRequests, key = { it.id }) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = WaslaSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val recipientLabel = if (req.toDisplayName.isNotBlank()) req.toDisplayName else req.toCode
                            Text(
                                text = "إلى: $recipientLabel",
                                color = WaslaTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "كود: ${req.toCode} • ${FormatUtils.formatDate(req.createdAt)}",
                                color = WaslaTextTertiary,
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (req.status == "accepted") WaslaPrimary.copy(alpha = 0.2f) else WaslaSurfaceVariant
                        ) {
                            Text(
                                text = when (req.status) {
                                    "accepted" -> "تم القبول"
                                    "rejected" -> "تم الرفض"
                                    else -> "بانتظار الرد"
                                },
                                color = if (req.status == "accepted") WaslaPrimary else WaslaAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutTabContent(
    device: Device,
    onOpenCloudSettings: () -> Unit,
    onCopyCode: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WaslaCodeCard(code = device.code, onCopy = onCopyCode)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WaslaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = WaslaPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "المزامنة السحابية وتوصيل عدة أجهزة",
                                color = WaslaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Text(
                        text = "يتيح هذا التطبيق الآن الاتصال بخادم سحابي (Firebase) لتبادل الرسائل والطلبات بين أجهزة وهواتف مختلفة حقيقية في أي مكان عبر الإنترنت.",
                        color = WaslaTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    Button(
                        onClick = onOpenCloudSettings,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WaslaSurfaceVariant,
                            contentColor = WaslaTextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = WaslaAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إعدادات السحابة وتجربة جهازين",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WaslaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = WaslaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "فلسفة وصلة",
                            color = WaslaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "صممها محمد سعد لتكون طريقة أقرب للتواصل: كود واحد ثابت، وأبواب لا تُفتح إلا بإذنك.",
                        color = WaslaTextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )

                    Text(
                        text = "لا توجد أرقام هواتف مطلوبة، لا إعلانات، ولا خوارزميات تزعجك. فقط محادثة نقية بينك وبين من تثق بهم.",
                        color = WaslaTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WaslaSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "حالة الاتصال بالخادم",
                            color = WaslaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(WaslaOnline)
                            )
                            Text(
                                text = "كل شيء يعمل",
                                color = WaslaOnline,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المطور",
                            color = WaslaTextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Mohamed Saad",
                            color = WaslaTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إصدار التطبيق",
                            color = WaslaTextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "1.0.0 (Native Android)",
                            color = WaslaTextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
