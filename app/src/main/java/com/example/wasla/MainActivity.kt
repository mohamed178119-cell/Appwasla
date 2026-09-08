package com.example.wasla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wasla.ui.components.WaslaToast
import com.example.wasla.ui.screens.ChatScreen
import com.example.wasla.ui.screens.CloudSettingsDialog
import com.example.wasla.ui.screens.HomeScreen
import com.example.wasla.ui.screens.NewGroupDialog
import com.example.wasla.ui.screens.NewRequestDialog
import com.example.wasla.ui.screens.RegistrationScreen
import com.example.wasla.ui.theme.WaslaBackground
import com.example.wasla.ui.theme.WaslaTheme
import com.example.wasla.ui.viewmodel.WaslaViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: WaslaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WaslaTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    WaslaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun WaslaApp(viewModel: WaslaViewModel) {
    val device by viewModel.device.collectAsStateWithLifecycle()
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val requests by viewModel.requests.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeChatId by viewModel.activeChatId.collectAsStateWithLifecycle()
    val activeChat by viewModel.activeChat.collectAsStateWithLifecycle()
    val showNewRequestDialog by viewModel.showNewRequestDialog.collectAsStateWithLifecycle()
    val showNewGroupDialog by viewModel.showNewGroupDialog.collectAsStateWithLifecycle()
    val showCloudSettingsDialog by viewModel.showCloudSettingsDialog.collectAsStateWithLifecycle()
    val cloudStatus by viewModel.cloudStatus.collectAsStateWithLifecycle()
    val cloudConfig by viewModel.cloudConfig.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaslaBackground)
    ) {
        val currentDevice = device
        if (currentDevice == null) {
            // First time onboarding / registration
            RegistrationScreen(
                onRegister = { name ->
                    viewModel.register(name)
                }
            )
        } else {
            // Main application flow
            val selectedChat = activeChat
            if (activeChatId != null && selectedChat != null) {
                BackHandler {
                    viewModel.closeChat()
                }

                Box(modifier = Modifier.systemBarsPadding()) {
                    ChatScreen(
                        chat = selectedChat,
                        device = currentDevice,
                        onBack = { viewModel.closeChat() },
                        onSendMessage = { text -> viewModel.sendMessage(selectedChat.id, text) },
                        onAcceptChat = {
                            val req = requests.find { it.chatId == selectedChat.id && it.toDeviceId == currentDevice.id }
                            if (req != null) {
                                viewModel.respondToRequest(req.id, true)
                            }
                        },
                        onRejectChat = {
                            val req = requests.find { it.chatId == selectedChat.id && it.toDeviceId == currentDevice.id }
                            if (req != null) {
                                viewModel.respondToRequest(req.id, false)
                            }
                            viewModel.closeChat()
                        }
                    )
                }
            } else {
                Box(modifier = Modifier.systemBarsPadding()) {
                    HomeScreen(
                        device = currentDevice,
                        chats = chats,
                        requests = requests,
                        currentTab = currentTab,
                        cloudStatus = cloudStatus,
                        onSelectTab = { tab -> viewModel.selectTab(tab) },
                        onTogglePresence = { viewModel.togglePresence() },
                        onOpenChat = { id -> viewModel.openChat(id) },
                        onNewRequest = { viewModel.showNewRequest(true) },
                        onNewGroup = { viewModel.showNewGroup(true) },
                        onRespondRequest = { id, accept -> viewModel.respondToRequest(id, accept) },
                        onSimulateIncomingRequest = { viewModel.simulateIncomingRequest() },
                        onOpenCloudSettings = { viewModel.showCloudSettings(true) },
                        onShowToast = { msg -> viewModel.showToast(msg) }
                    )
                }
            }
        }

        // Dialogs
        if (showNewRequestDialog) {
            NewRequestDialog(
                onDismiss = { viewModel.showNewRequest(false) },
                onSubmit = { targetCode ->
                    viewModel.sendChatRequest(
                        targetCode = targetCode,
                        onSuccess = {},
                        onError = { error -> viewModel.showToast(error) }
                    )
                }
            )
        }

        if (showNewGroupDialog) {
            NewGroupDialog(
                onDismiss = { viewModel.showNewGroup(false) },
                onSubmit = { name, codes ->
                    viewModel.createGroup(
                        name = name,
                        memberCodes = codes,
                        onSuccess = {},
                        onError = { error -> viewModel.showToast(error) }
                    )
                }
            )
        }

        if (showCloudSettingsDialog && currentDevice != null) {
            CloudSettingsDialog(
                config = cloudConfig,
                status = cloudStatus,
                currentDevice = currentDevice,
                allProfiles = allProfiles,
                onDismiss = { viewModel.showCloudSettings(false) },
                onSaveUrl = { url -> viewModel.updateCloudServerUrl(url) },
                onToggleSync = { viewModel.toggleCloudSync() },
                onManualSync = { viewModel.triggerManualSync() },
                onSwitchProfile = { id -> viewModel.switchProfile(id) },
                onCreateTestProfile = { name -> viewModel.createSecondTestDevice(name) }
            )
        }

        // Toast Notification Banner
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .systemBarsPadding()
                .padding(top = 8.dp)
        ) {
            toastMessage?.let { msg ->
                WaslaToast(
                    message = msg,
                    onDismiss = { viewModel.clearToast() }
                )
            }
        }
    }
}
