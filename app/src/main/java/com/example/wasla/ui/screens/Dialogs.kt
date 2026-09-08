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
