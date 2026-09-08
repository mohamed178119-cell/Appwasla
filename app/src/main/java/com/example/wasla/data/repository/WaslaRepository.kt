package com.example.wasla.data.repository

import android.content.Context
import com.example.wasla.data.db.WaslaDatabaseHelper
import com.example.wasla.data.model.Chat
import com.example.wasla.data.model.ChatMember
import com.example.wasla.data.model.ChatRequest
import com.example.wasla.data.model.Device
import com.example.wasla.data.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.UUID

class WaslaRepository(context: Context) {
    private val dbHelper = WaslaDatabaseHelper(context)
    private val random = SecureRandom()
    private val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _device = MutableStateFlow<Device?>(null)
    val device: StateFlow<Device?> = _device.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _requests = MutableStateFlow<List<ChatRequest>>(emptyList())
    val requests: StateFlow<List<ChatRequest>> = _requests.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        scope.launch {
            val currentDevice = dbHelper.getDevice()
            _device.value = currentDevice
            refreshData()
        }
    }

    private fun generateCode(): String {
        fun part(length: Int) = (1..length)
            .map { alphabet[random.nextInt(alphabet.length)] }
            .joinToString("")
        return "${part(3)}-${part(3)}"
    }

    fun registerDevice(name: String): Device {
        val trimmed = name.trim().ifEmpty { "نسخة وصلة" }
        val newDevice = Device(
            id = UUID.randomUUID().toString(),
            code = generateCode(),
            displayName = trimmed,
            online = true,
            createdAt = System.currentTimeMillis()
        )
        dbHelper.saveDevice(newDevice)
        _device.value = newDevice

        // Seed an initial friendly welcome contact & request so the user has an immediate interactive experience
        seedWelcomeExperience(newDevice)

        refreshData()
        return newDevice
    }

    private fun seedWelcomeExperience(currentDevice: Device) {
        val partnerId = UUID.randomUUID().toString()
        val partnerCode = "SAAD-77"
        val partnerName = "محمد سعد"

        // 1. A welcome active chat
        val chatId = UUID.randomUUID().toString()
        val chat = Chat(
            id = chatId,
            type = "direct",
            name = partnerName,
            status = "active",
            updatedAt = System.currentTimeMillis()
        )
        dbHelper.saveChat(chat)

        dbHelper.saveChatMember(
            ChatMember(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                deviceId = currentDevice.id,
                code = currentDevice.code,
                displayName = currentDevice.displayName,
                online = currentDevice.online,
                status = "active"
            )
        )
        dbHelper.saveChatMember(
            ChatMember(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                deviceId = partnerId,
                code = partnerCode,
                displayName = partnerName,
                online = true,
                status = "active"
            )
        )

        dbHelper.saveMessage(
            Message(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = partnerId,
                senderCode = partnerCode,
                text = "أهلاً بك في وصلة! هذا كودك الثابت، شاركه مع من تثق لبدء محادثات خاصة.",
                createdAt = System.currentTimeMillis() - 60000
            )
        )

        // 2. An incoming request from another contact to demonstrate accept/reject flow
        val senderId = UUID.randomUUID().toString()
        val senderCode = "NOOR-39"
        val reqChatId = UUID.randomUUID().toString()

        val reqChat = Chat(
            id = reqChatId,
            type = "direct",
            name = "نور",
            status = "pending_incoming",
            updatedAt = System.currentTimeMillis()
        )
        dbHelper.saveChat(reqChat)

        dbHelper.saveChatMember(
            ChatMember(
                id = UUID.randomUUID().toString(),
                chatId = reqChatId,
                deviceId = currentDevice.id,
                code = currentDevice.code,
                displayName = currentDevice.displayName,
                online = currentDevice.online,
                status = "pending_incoming"
            )
        )
        dbHelper.saveChatMember(
            ChatMember(
                id = UUID.randomUUID().toString(),
                chatId = reqChatId,
                deviceId = senderId,
                code = senderCode,
                displayName = "نور",
                online = true,
                status = "pending_outgoing"
            )
        )

        val chatReq = ChatRequest(
            id = UUID.randomUUID().toString(),
            chatId = reqChatId,
            fromDeviceId = senderId,
            toDeviceId = currentDevice.id,
            fromCode = senderCode,
            toCode = currentDevice.code,
            status = "pending",
            createdAt = System.currentTimeMillis() - 300000
        )
        dbHelper.saveRequest(chatReq)
    }

    fun updatePresence(online: Boolean) {
        val current = _device.value ?: return
        val updated = current.copy(online = online)
        dbHelper.updatePresence(current.id, online)
        _device.value = updated
    }

    fun createChatRequest(targetCode: String): Result<ChatRequest> {
        val current = _device.value ?: return Result.failure(Exception("الجهاز غير مسجل"))
        val normalizedCode = targetCode.trim().uppercase()

        if (normalizedCode.isEmpty()) {
            return Result.failure(Exception("يرجى إدخال رمز صحيح"))
        }
        if (normalizedCode == current.code) {
            return Result.failure(Exception("لا يمكن بدء محادثة مع نفس النسخة"))
        }

        val chatId = UUID.randomUUID().toString()
        val requestId = UUID.randomUUID().toString()
        val targetDeviceId = UUID.randomUUID().toString()

        val chat = Chat(
            id = chatId,
            type = "direct",
            name = "وصلة $normalizedCode",
            status = "pending_outgoing",
            updatedAt = System.currentTimeMillis()
        )
        dbHelper.saveChat(chat)

        dbHelper.saveChatMember(
            ChatMember(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                deviceId = current.id,
                code = current.code,
                displayName = current.displayName,
                online = current.online,
                status = "pending_outgoing"
            )
        )

        dbHelper.saveChatMember(
            ChatMember(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                deviceId = targetDeviceId,
                code = normalizedCode,
                displayName = "وصلة $normalizedCode",
                online = true,
                status = "pending_incoming"
            )
        )

        val request = ChatRequest(
            id = requestId,
            chatId = chatId,
            fromDeviceId = current.id,
            toDeviceId = targetDeviceId,
            fromCode = current.code,
            toCode = normalizedCode,
            status = "pending",
            createdAt = System.currentTimeMillis()
        )
        dbHelper.saveRequest(request)

        refreshData()

        // Auto-accept simulation after 4 seconds to simulate the remote peer accepting!
        scope.launch {
            delay(4000)
            dbHelper.updateRequestStatus(requestId, "accepted")
            dbHelper.updateChatStatus(chatId, "active")
            dbHelper.saveMessage(
                Message(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    senderId = targetDeviceId,
                    senderCode = normalizedCode,
                    text = "تم قبول طلب المحادثة! مرحباً بك.",
                    createdAt = System.currentTimeMillis()
                )
            )
            refreshData()
        }

        return Result.success(request)
    }

    fun createGroup(name: String, memberCodes: List<String>): Result<Chat> {
        val current = _device.value ?: return Result.failure(Exception("الجهاز غير مسجل"))
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(Exception("يرجى إدخال اسم للمجموعة"))
        }

        val cleanCodes = memberCodes.map { it.trim().uppercase() }.filter { it.isNotEmpty() }.distinct()
        if (cleanCodes.isEmpty()) {
            return Result.failure(Exception("يرجى إدخال رموز الأعضاء"))
        }

        val chatId = UUID.randomUUID().toString()
        val chat = Chat(
            id = chatId,
            type = "group",
            name = trimmedName,
            status = "active",
            updatedAt = System.currentTimeMillis()
        )
        dbHelper.saveChat(chat)

        // Add creator
        dbHelper.saveChatMember(
            ChatMember(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                deviceId = current.id,
                code = current.code,
                displayName = current.displayName,
                online = current.online,
                status = "active"
            )
        )

        // Add group members
        cleanCodes.forEach { code ->
            dbHelper.saveChatMember(
                ChatMember(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    deviceId = UUID.randomUUID().toString(),
                    code = code,
                    displayName = "عضو $code",
                    online = true,
                    status = "active"
                )
            )
        }

        dbHelper.saveMessage(
            Message(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = current.id,
                senderCode = current.code,
                text = "تم إنشاء مجموعة $trimmedName",
                createdAt = System.currentTimeMillis()
            )
        )

        refreshData()
        return Result.success(dbHelper.getChatById(chatId) ?: chat)
    }

    fun respondToRequest(requestId: String, accept: Boolean) {
        val status = if (accept) "accepted" else "rejected"
        dbHelper.updateRequestStatus(requestId, status)

        val request = dbHelper.getAllRequests().find { it.id == requestId }
        if (request != null) {
            val chatStatus = if (accept) "active" else "rejected"
            dbHelper.updateChatStatus(request.chatId, chatStatus)
            if (accept) {
                dbHelper.saveMessage(
                    Message(
                        id = UUID.randomUUID().toString(),
                        chatId = request.chatId,
                        senderId = request.fromDeviceId,
                        senderCode = request.fromCode,
                        text = "مرحباً! بدأت المحادثة الخاصة بيننا.",
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
        refreshData()
    }

    fun sendMessage(chatId: String, text: String) {
        val current = _device.value ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val message = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = current.id,
            senderCode = current.code,
            text = trimmed,
            createdAt = System.currentTimeMillis()
        )
        dbHelper.saveMessage(message)
        refreshData()

        // Simulate smart peer response after a realistic delay
        val chat = dbHelper.getChatById(chatId) ?: return
        val otherMember = chat.members.firstOrNull { it.deviceId != current.id }
        if (otherMember != null && chat.status == "active") {
            scope.launch {
                delay(1200)
                val peerReplies = listOf(
                    "أهلاً بك! وصلت رسالتك بوضوح.",
                    "وصلة ممتازة، التواصل بهدوء هنا رائع!",
                    "تم الاستلام، شكراً لك.",
                    "أتفق معك تماماً 👍",
                    "كل شيء يعمل بسلاسة عبر كود وصلة الخاص."
                )
                val replyText = peerReplies.random()
                val replyMessage = Message(
                    id = UUID.randomUUID().toString(),
                    chatId = chatId,
                    senderId = otherMember.deviceId,
                    senderCode = otherMember.code,
                    text = replyText,
                    createdAt = System.currentTimeMillis()
                )
                dbHelper.saveMessage(replyMessage)
                refreshData()
            }
        }
    }

    fun simulateIncomingNewRequest() {
        val current = _device.value ?: return
        val senderId = UUID.randomUUID().toString()
        val randomNum = (100..999).random()
        val senderCode = "USR-$randomNum"
        val chatId = UUID.randomUUID().toString()

        val chat = Chat(
            id = chatId,
            type = "direct",
            name = "وصلة $senderCode",
            status = "pending_incoming",
            updatedAt = System.currentTimeMillis()
        )
        dbHelper.saveChat(chat)

        dbHelper.saveChatMember(
            ChatMember(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                deviceId = current.id,
                code = current.code,
                displayName = current.displayName,
                online = current.online,
                status = "pending_incoming"
            )
        )
        dbHelper.saveChatMember(
            ChatMember(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                deviceId = senderId,
                code = senderCode,
                displayName = "وصلة $senderCode",
                online = true,
                status = "pending_outgoing"
            )
        )

        val req = ChatRequest(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            fromDeviceId = senderId,
            toDeviceId = current.id,
            fromCode = senderCode,
            toCode = current.code,
            status = "pending",
            createdAt = System.currentTimeMillis()
        )
        dbHelper.saveRequest(req)
        refreshData()
    }

    fun refreshData() {
        _chats.value = dbHelper.getAllChats()
        _requests.value = dbHelper.getAllRequests()
    }
}
