package com.example.wasla.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.wasla.data.cloud.CloudConfig
import com.example.wasla.data.cloud.CloudStatus
import com.example.wasla.data.cloud.WaslaCloudSyncManager
import com.example.wasla.data.model.Device
import com.example.wasla.ui.theme.WaslaAccent
import com.example.wasla.ui.theme.WaslaOnline
import com.example.wasla.ui.theme.WaslaCardBorder
import com.example.wasla.ui.theme.WaslaError
import com.example.wasla.ui.theme.WaslaOnPrimary
import com.example.wasla.ui.theme.WaslaPrimary
import com.example.wasla.ui.theme.WaslaSurface
import com.example.wasla.ui.theme.WaslaSurfaceVariant
import com.example.wasla.ui.theme.WaslaTextPrimary
import com.example.wasla.ui.theme.WaslaTextSecondary
import com.example.wasla.ui.theme.WaslaTextTertiary

@Composable
fun NewRequestDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_new_request"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WaslaSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = WaslaPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "طلب محادثة جديدة",
                            color = WaslaTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_request_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = WaslaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "أدخل رمز الطرف الآخر لبدء المحادثة. لن تفتح المحادثة حتى يوافق على الطلب.",
                    color = WaslaTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "رمز الطرف الآخر",
                    color = WaslaTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase().filter { c -> c.isLetterOrDigit() || c == '-' }.take(10)
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_code_input"),
                    placeholder = {
                        Text("مثال: WS8-4KQ", color = WaslaTextTertiary, fontFamily = FontFamily.Monospace)
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

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = WaslaError,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WaslaTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (code.trim().length >= 4) {
                                onSubmit(code.trim())
                            } else {
                                errorMessage = "يرجى إدخال رمز صحيح مكون من 4 خانات على الأقل"
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_request_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WaslaPrimary,
                            contentColor = WaslaOnPrimary
                        )
                    ) {
                        Text("إرسال الطلب", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NewGroupDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var membersText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_new_group"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WaslaSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GroupAdd,
                            contentDescription = null,
                            tint = WaslaPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "إنشاء مجموعة جديدة",
                            color = WaslaTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_group_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = WaslaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "اسم المجموعة",
                    color = WaslaTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it; errorMessage = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_name_input"),
                    placeholder = {
                        Text("مثال: أصدقاء العمل", color = WaslaTextTertiary)
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "رموز الأعضاء (مفصولة بمسافة أو فاصلة)",
                    color = WaslaTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = membersText,
                    onValueChange = { membersText = it; errorMessage = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_codes_input"),
                    placeholder = {
                        Text("مثال: SAAD-77, NOOR-39", color = WaslaTextTertiary, fontFamily = FontFamily.Monospace)
                    },
                    maxLines = 2,
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

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = WaslaError,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WaslaTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            val trimmedName = groupName.trim()
                            val codes = membersText.split(Regex("[,\\s]+")).map { it.trim() }.filter { it.isNotEmpty() }
                            if (trimmedName.isEmpty()) {
                                errorMessage = "يرجى تحديد اسم للمجموعة"
                            } else if (codes.isEmpty()) {
                                errorMessage = "يرجى إضافة رمز عضو واحد على الأقل"
                            } else {
                                onSubmit(trimmedName, codes)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_group_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WaslaPrimary,
                            contentColor = WaslaOnPrimary
                        )
                    ) {
                        Text("إنشاء المجموعة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CloudSettingsDialog(
    config: com.example.wasla.data.cloud.CloudConfig,
    status: com.example.wasla.data.cloud.CloudStatus,
    currentDevice: com.example.wasla.data.model.Device,
    allProfiles: List<com.example.wasla.data.model.Device>,
    onDismiss: () -> Unit,
    onSaveUrl: (String) -> Unit,
    onToggleSync: () -> Unit,
    onManualSync: () -> Unit,
    onSwitchProfile: (String) -> Unit,
    onCreateTestProfile: (String) -> Unit
) {
    var urlText by remember { mutableStateOf(config.serverUrl) }
    var newProfileName by remember { mutableStateOf("") }
    var showAddProfile by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WaslaSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (status) {
                                CloudStatus.CONNECTED -> Icons.Default.CloudDone
                                CloudStatus.SYNCING -> Icons.Default.Sync
                                CloudStatus.OFFLINE_LOCAL -> Icons.Default.CloudOff
                                CloudStatus.ERROR -> Icons.Default.CloudOff
                            },
                            contentDescription = null,
                            tint = when (status) {
                                CloudStatus.CONNECTED -> WaslaOnline
                                CloudStatus.SYNCING -> WaslaAccent
                                CloudStatus.OFFLINE_LOCAL -> WaslaTextTertiary
                                CloudStatus.ERROR -> WaslaError
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "إعدادات الربط السحابي",
                            color = WaslaTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إإغلاق",
                            tint = WaslaTextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = WaslaSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val statusText = when (status) {
                            com.example.wasla.data.cloud.CloudStatus.CONNECTED -> "🟢 متصل بالسحابة (مزامنة حية)"
                            com.example.wasla.data.cloud.CloudStatus.SYNCING -> "🟡 جارِ المزامنة الآن..."
                            com.example.wasla.data.cloud.CloudStatus.OFFLINE_LOCAL -> "⚪ وضع محلي فقط"
                            com.example.wasla.data.cloud.CloudStatus.ERROR -> "🔴 تعذر الاتصال بالسحابة"
                        }
                        Text(
                            text = statusText,
                            color = WaslaTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = config.statusMessage,
                            color = WaslaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Sync
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "المزامنة بين عدة أجهزة",
                            color = WaslaTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "إرسال واستقبال الرسائل والطلبات عبر الإنترنت",
                            color = WaslaTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = config.isEnabled,
                        onCheckedChange = { onToggleSync() },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = WaslaPrimary,
                            checkedTrackColor = WaslaPrimary.copy(alpha = 0.3f),
                            uncheckedThumbColor = WaslaTextTertiary,
                            uncheckedTrackColor = WaslaSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Server URL input
                Text(
                    text = "رابط الخادم السحابي / Firebase Realtime DB:",
                    color = WaslaTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WaslaTextPrimary,
                        unfocusedTextColor = WaslaTextPrimary,
                        focusedBorderColor = WaslaPrimary,
                        unfocusedBorderColor = WaslaCardBorder,
                        focusedContainerColor = WaslaSurfaceVariant,
                        unfocusedContainerColor = WaslaSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            urlText = com.example.wasla.data.cloud.WaslaCloudSyncManager.DEFAULT_FIREBASE_URL
                            onSaveUrl(urlText)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WaslaTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                    ) {
                        Text("الافتراضي", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onSaveUrl(urlText) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WaslaPrimary,
                            contentColor = WaslaOnPrimary
                        )
                    ) {
                        Text("حفظ ومزامنة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Multi-device testing section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = WaslaAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "اختبار تواصل جهازين (التبديل بين الأجهزة):",
                        color = WaslaTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "يمكنك التبديل بين ملفات الأجهزة أو إنشاء جهاز جديد لاختبار إرسال الطلب واستقباله فورياً!",
                    color = WaslaTextTertiary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                // Existing profiles
                allProfiles.forEach { profile ->
                    val isCurrent = profile.id == currentDevice.id
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) WaslaPrimary.copy(alpha = 0.12f) else WaslaSurfaceVariant
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) WaslaPrimary else WaslaCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = profile.displayName,
                                        color = WaslaTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (isCurrent) {
                                        Text(
                                            text = "(نشط الآن)",
                                            color = WaslaPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "الرمز: ${profile.code}",
                                    color = com.example.wasla.ui.theme.WaslaAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!isCurrent) {
                                OutlinedButton(
                                    onClick = {
                                        onSwitchProfile(profile.id)
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WaslaPrimary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WaslaPrimary)
                                ) {
                                    Text("تبديل لهذا الجهاز", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Create second test device
                if (showAddProfile) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newProfileName,
                            onValueChange = { newProfileName = it },
                            placeholder = { Text("اسم الجهاز الثاني (مثال: هاتف صديقي)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WaslaTextPrimary,
                                unfocusedTextColor = WaslaTextPrimary,
                                focusedBorderColor = com.example.wasla.ui.theme.WaslaAccent,
                                unfocusedBorderColor = WaslaCardBorder,
                                focusedContainerColor = WaslaSurfaceVariant,
                                unfocusedContainerColor = WaslaSurfaceVariant
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddProfile = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = WaslaTextSecondary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WaslaCardBorder)
                            ) {
                                Text("إلغاء", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val name = newProfileName.trim().ifEmpty { "هاتف تجريبي ثاني" }
                                    onCreateTestProfile(name)
                                    showAddProfile = false
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WaslaAccent,
                                    contentColor = WaslaSurface
                                )
                            ) {
                                Text("إنشاء والتبديل إليه", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showAddProfile = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WaslaAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WaslaAccent)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة جهاز جديد لاختبار الربط", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Manual sync button
                Button(
                    onClick = {
                        onManualSync()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WaslaSurfaceVariant,
                        contentColor = WaslaTextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = WaslaAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("فحص السحابة والمزامنة الآن", fontSize = 13.sp)
                }
            }
        }
    }
}
