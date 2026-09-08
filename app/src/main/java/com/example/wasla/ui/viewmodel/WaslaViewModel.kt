package com.example.wasla.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wasla.data.cloud.CloudConfig
import com.example.wasla.data.cloud.CloudStatus
import com.example.wasla.data.model.Chat
import com.example.wasla.data.model.ChatRequest
import com.example.wasla.data.model.Device
import com.example.wasla.data.repository.WaslaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WaslaTab {
    CHATS,
    REQUESTS,
    ABOUT
}

class WaslaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WaslaRepository(application)

    val device: StateFlow<Device?> = repository.device
    val chats: StateFlow<List<Chat>> = repository.chats
    val requests: StateFlow<List<ChatRequest>> = repository.requests
    val allProfiles: StateFlow<List<Device>> = repository.allProfiles
    val cloudStatus: StateFlow<CloudStatus> = repository.cloudStatus
    val cloudConfig: StateFlow<CloudConfig> = repository.cloudConfig

    private val _currentTab = MutableStateFlow(WaslaTab.CHATS)
    val currentTab: StateFlow<WaslaTab> = _currentTab.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    val activeChat: StateFlow<Chat?> = combine(chats, _activeChatId) { chatList, id ->
        chatList.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showNewRequestDialog = MutableStateFlow(false)
    val showNewRequestDialog: StateFlow<Boolean> = _showNewRequestDialog.asStateFlow()

    private val _showNewGroupDialog = MutableStateFlow(false)
    val showNewGroupDialog: StateFlow<Boolean> = _showNewGroupDialog.asStateFlow()

    private val _showCloudSettingsDialog = MutableStateFlow(false)
    val showCloudSettingsDialog: StateFlow<Boolean> = _showCloudSettingsDialog.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun selectTab(tab: WaslaTab) {
        _currentTab.value = tab
    }

    fun openChat(chatId: String) {
        _activeChatId.value = chatId
    }

    fun closeChat() {
        _activeChatId.value = null
    }

    fun showNewRequest(show: Boolean) {
        _showNewRequestDialog.value = show
    }

    fun showNewGroup(show: Boolean) {
        _showNewGroupDialog.value = show
    }

    fun showCloudSettings(show: Boolean) {
        _showCloudSettingsDialog.value = show
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun register(name: String) {
        viewModelScope.launch {
            repository.registerDevice(name)
            showToast("تم إنشاء وصلتك الخاصة وتفعيل المزامنة السحابية")
        }
    }

    fun togglePresence() {
        val current = device.value ?: return
        val newStatus = !current.online
        repository.updatePresence(newStatus)
        showToast(if (newStatus) "أنت متصل الآن على السحابة" else "ظهورك الآن مخفي")
    }

    fun sendChatRequest(targetCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val result = repository.createChatRequest(targetCode)
        if (result.isSuccess) {
            _showNewRequestDialog.value = false
            showToast("تم إرسال الطلب عبر السحابة — بانتظار الطرف الآخر")
            onSuccess()
        } else {
            onError(result.exceptionOrNull()?.message ?: "تعذر إرسال الطلب")
        }
    }

    fun createGroup(name: String, memberCodes: List<String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val result = repository.createGroup(name, memberCodes)
        if (result.isSuccess) {
            _showNewGroupDialog.value = false
            val newChat = result.getOrNull()
            if (newChat != null) {
                _activeChatId.value = newChat.id
            }
            showToast("تم إنشاء المجموعة ومزامنتها سحابياً")
            onSuccess()
        } else {
            onError(result.exceptionOrNull()?.message ?: "تعذر إنشاء المجموعة")
        }
    }

    fun respondToRequest(requestId: String, accept: Boolean) {
        repository.respondToRequest(requestId, accept)
        showToast(if (accept) "تم قبول الطلب، المحادثة جاهزة ومحدثة سحابياً" else "تم تجاهل الطلب")
    }

    fun sendMessage(chatId: String, text: String) {
        repository.sendMessage(chatId, text)
    }

    fun simulateIncomingRequest() {
        repository.simulateIncomingNewRequest()
        showToast("وصل طلب محادثة جديد!")
    }

    fun updateCloudServerUrl(url: String) {
        repository.updateCloudServerUrl(url)
        showToast("تم حفظ رابط الخادم السحابي وجارِ المزامنة")
    }

    fun toggleCloudSync() {
        val currentEnabled = cloudConfig.value.isEnabled
        repository.setCloudSyncEnabled(!currentEnabled)
        showToast(if (!currentEnabled) "تم تفعيل المزامنة السحابية" else "تم إيقاف المزامنة السحابية (وضع محلي)")
    }

    fun triggerManualSync() {
        repository.triggerCloudSync()
        showToast("جارِ مزامنة الرسائل والطلبات مع السحابة...")
    }

    fun switchProfile(deviceId: String) {
        repository.switchProfile(deviceId)
        _activeChatId.value = null
        showToast("تم تبديل الملف النشط إلى هذا الجهاز")
    }

    fun createSecondTestDevice(name: String) {
        viewModelScope.launch {
            val newDev = repository.createNewProfile(name)
            _activeChatId.value = null
            showToast("تم إنشاء جهاز تجريبي جديد: ${newDev.code}")
        }
    }
}
