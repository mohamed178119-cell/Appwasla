package com.example.wasla.data.cloud

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.wasla.data.db.WaslaDatabaseHelper
import com.example.wasla.data.model.Chat
import com.example.wasla.data.model.ChatMember
import com.example.wasla.data.model.ChatRequest
import com.example.wasla.data.model.Device
import com.example.wasla.data.model.Message
import com.example.wasla.data.util.WaslaImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

enum class CloudStatus {
    CONNECTED,
    SYNCING,
    OFFLINE_LOCAL,
    ERROR
}

data class CloudConfig(
    val serverUrl: String,
    val isEnabled: Boolean = true,
    val lastSyncTime: Long = 0L,
    val statusMessage: String = "متصل"
)

class WaslaCloudSyncManager(
    private val context: Context,
    private val dbHelper: WaslaDatabaseHelper
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wasla_cloud_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_FIREBASE_URL = "https://wasla-chat-app-default-rtdb.firebaseio.com"
        private const val PREF_KEY_SERVER_URL = "cloud_server_url"
        private const val PREF_KEY_CLOUD_ENABLED = "cloud_sync_enabled"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .writeTimeout(6, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null

    private val _cloudStatus = MutableStateFlow(CloudStatus.CONNECTED)
    val cloudStatus: StateFlow<CloudStatus> = _cloudStatus.asStateFlow()

    private val _config = MutableStateFlow(
        CloudConfig(
            serverUrl = prefs.getString(PREF_KEY_SERVER_URL, DEFAULT_FIREBASE_URL) ?: DEFAULT_FIREBASE_URL,
            isEnabled = prefs.getBoolean(PREF_KEY_CLOUD_ENABLED, true)
        )
    )
    val config: StateFlow<CloudConfig> = _config.asStateFlow()

    var onDataSynchronized: (() -> Unit)? = null

    init {
        startSyncLoop()
    }

    fun updateServerUrl(url: String) {
        val trimmed = url.trim().trimEnd('/')
        prefs.edit().putString(PREF_KEY_SERVER_URL, trimmed).apply()
        _config.value = _config.value.copy(serverUrl = trimmed)
        triggerImmediateSync()
    }

    fun setCloudEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_KEY_CLOUD_ENABLED, enabled).apply()
        _config.value = _config.value.copy(isEnabled = enabled)
        if (enabled) {
            startSyncLoop()
            triggerImmediateSync()
        } else {
            _cloudStatus.value = CloudStatus.OFFLINE_LOCAL
        }
    }

    fun startSyncLoop() {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                if (_config.value.isEnabled) {
                    performSync()
                }
                delay(1600) // Fast near real-time polling (1.6 seconds)
            }
        }
    }

    fun triggerImmediateSync() {
        scope.launch {
            performSync()
        }
    }

    private fun getNormalizedBaseUrl(): String {
        var url = _config.value.serverUrl.trim().trimEnd('/')
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }
        return url
    }

    private suspend fun performSync() {
        val currentDevice = dbHelper.getDevice() ?: return
        if (!_config.value.isEnabled) {
            _cloudStatus.value = CloudStatus.OFFLINE_LOCAL
            return
        }

        try {
            val baseUrl = getNormalizedBaseUrl()

            // 1. Publish our device identity & presence
            publishDevicePresence(baseUrl, currentDevice)

            // 2. Fetch incoming requests addressed to our device code
            val hasNewRequests = fetchIncomingRequests(baseUrl, currentDevice)

            // 3. Fetch messages for each of our active chats
            val hasNewMessages = fetchMessagesForAllChats(baseUrl, currentDevice)

            _cloudStatus.value = CloudStatus.CONNECTED
            _config.value = _config.value.copy(
                lastSyncTime = System.currentTimeMillis(),
                statusMessage = "متصل"
            )

            if (hasNewRequests || hasNewMessages) {
                onDataSynchronized?.invoke()
            }
        } catch (e: Exception) {
            _cloudStatus.value = CloudStatus.ERROR
        }
    }

    private fun publishDevicePresence(baseUrl: String, device: Device) {
        try {
            val bodyJson = JSONObject().apply {
                put("id", device.id)
                put("code", device.code)
                put("displayName", device.displayName)
                put("online", device.online)
                put("lastSeen", System.currentTimeMillis())
            }

            val request = Request.Builder()
                .url("$baseUrl/wasla/devices/${device.code}.json")
                .put(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().close()
        } catch (_: Exception) {}
    }

    // Publish an outgoing chat request to the recipient's inbox on the cloud
    fun uploadChatRequest(request: ChatRequest) {
        scope.launch {
            try {
                val baseUrl = getNormalizedBaseUrl()
                val currentDevice = dbHelper.getDevice()
                val fromName = request.fromDisplayName.ifBlank { currentDevice?.displayName ?: "" }

                val bodyJson = JSONObject().apply {
                    put("id", request.id)
                    put("chatId", request.chatId)
                    put("fromDeviceId", request.fromDeviceId)
                    put("toDeviceId", request.toDeviceId)
                    put("fromCode", request.fromCode)
                    put("toCode", request.toCode)
                    put("fromDisplayName", fromName)
                    put("toDisplayName", request.toDisplayName)
                    put("status", request.status)
                    put("createdAt", request.createdAt)
                }

                // Put into target code's inbox
                val httpReq = Request.Builder()
                    .url("$baseUrl/wasla/requests/${request.toCode}/${request.id}.json")
                    .put(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                client.newCall(httpReq).execute().close()

                // Also publish chat definition
                val chatJson = JSONObject().apply {
                    put("id", request.chatId)
                    put("type", "direct")
                    put("name", fromName.ifBlank { "وصلة ${request.fromCode}" })
                    put("status", "pending_incoming")
                    put("updatedAt", request.createdAt)
                    put("senderCode", request.fromCode)
                    put("targetCode", request.toCode)
                    put("fromDisplayName", fromName)
                }
                val chatHttpReq = Request.Builder()
                    .url("$baseUrl/wasla/chats/${request.chatId}.json")
                    .put(chatJson.toString().toRequestBody(jsonMediaType))
                    .build()
                client.newCall(chatHttpReq).execute().close()

                triggerImmediateSync()
            } catch (_: Exception) {}
        }
    }

    // Update request status (e.g. accepted / rejected) in cloud with responder's display name
    fun uploadRequestStatus(request: ChatRequest, newStatus: String) {
        scope.launch {
            try {
                val baseUrl = getNormalizedBaseUrl()
                val currentDevice = dbHelper.getDevice()
                val myDisplayName = currentDevice?.displayName ?: ""

                val bodyJson = JSONObject().apply {
                    put("status", newStatus)
                    if (newStatus == "accepted") {
                        put("toDisplayName", myDisplayName)
                    }
                }

                val httpReq = Request.Builder()
                    .url("$baseUrl/wasla/requests/${request.toCode}/${request.id}.json")
                    .patch(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()
                client.newCall(httpReq).execute().close()

                // Also update chat status and display name
                val chatStatus = if (newStatus == "accepted") "active" else "rejected"
                val chatPatchJson = JSONObject().apply {
                    put("status", chatStatus)
                    if (newStatus == "accepted") {
                        put("toDisplayName", myDisplayName)
                    }
                }
                val chatHttpReq = Request.Builder()
                    .url("$baseUrl/wasla/chats/${request.chatId}.json")
                    .patch(chatPatchJson.toString().toRequestBody(jsonMediaType))
                    .build()
                client.newCall(chatHttpReq).execute().close()

                triggerImmediateSync()
            } catch (_: Exception) {}
        }
    }

    // Upload a message (text and/or image) to cloud
    fun uploadMessage(message: Message) {
        scope.launch {
            try {
                val baseUrl = getNormalizedBaseUrl()
                val bodyJson = JSONObject().apply {
                    put("id", message.id)
                    put("chatId", message.chatId)
                    put("senderId", message.senderId)
                    put("senderCode", message.senderCode)
                    put("text", message.text)
                    put("createdAt", message.createdAt)

                    // If message contains an image, send as base64
                    if (!message.imageUri.isNullOrBlank()) {
                        try {
                            val imgFile = File(message.imageUri)
                            if (imgFile.exists()) {
                                val bytes = imgFile.readBytes()
                                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                put("imageBase64", base64)
                            }
                        } catch (_: Exception) {}
                    }
                }

                val httpReq = Request.Builder()
                    .url("$baseUrl/wasla/messages/${message.chatId}/${message.id}.json")
                    .put(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                client.newCall(httpReq).execute().close()
            } catch (_: Exception) {}
        }
    }

    // Delete chat from cloud
    fun deleteChatFromCloud(chatId: String) {
        scope.launch {
            try {
                val baseUrl = getNormalizedBaseUrl()
                val httpReq = Request.Builder()
                    .url("$baseUrl/wasla/messages/$chatId.json")
                    .delete()
                    .build()
                client.newCall(httpReq).execute().close()

                val chatDeleteReq = Request.Builder()
                    .url("$baseUrl/wasla/chats/$chatId.json")
                    .delete()
                    .build()
                client.newCall(chatDeleteReq).execute().close()
            } catch (_: Exception) {}
        }
    }

    // Fetch incoming requests from cloud
    private fun fetchIncomingRequests(baseUrl: String, currentDevice: Device): Boolean {
        var hasNew = false
        val httpReq = Request.Builder()
            .url("$baseUrl/wasla/requests/${currentDevice.code}.json")
            .get()
            .build()

        try {
            client.newCall(httpReq).execute().use { response ->
                if (!response.isSuccessful) return false
                val body = response.body?.string() ?: return false
                if (body == "null" || body.isBlank()) return false

                val json = JSONObject(body)
                val keys = json.keys()
                val existingRequests = dbHelper.getAllRequests().associateBy { it.id }

                while (keys.hasNext()) {
                    val reqId = keys.next()
                    val reqObj = json.optJSONObject(reqId) ?: continue

                    val chatId = reqObj.optString("chatId")
                    val fromDeviceId = reqObj.optString("fromDeviceId")
                    val fromCode = reqObj.optString("fromCode")
                    val toCode = reqObj.optString("toCode")
                    val fromDisplayName = reqObj.optString("fromDisplayName", "").ifBlank { "وصلة $fromCode" }
                    val toDisplayName = reqObj.optString("toDisplayName", "")
                    val status = reqObj.optString("status", "pending")
                    val createdAt = reqObj.optLong("createdAt", System.currentTimeMillis())

                    if (!existingRequests.containsKey(reqId)) {
                        hasNew = true
                        val existingChat = dbHelper.getChatById(chatId)
                        if (existingChat == null) {
                            val chat = Chat(
                                id = chatId,
                                type = "direct",
                                name = fromDisplayName, // Registered display name of the sender!
                                status = if (status == "accepted") "active" else "pending_incoming",
                                updatedAt = createdAt
                            )
                            dbHelper.saveChat(chat)

                            // Member: me
                            dbHelper.saveChatMember(
                                ChatMember(
                                    id = java.util.UUID.randomUUID().toString(),
                                    chatId = chatId,
                                    deviceId = currentDevice.id,
                                    code = currentDevice.code,
                                    displayName = currentDevice.displayName,
                                    online = currentDevice.online,
                                    status = "pending_incoming"
                                )
                            )
                            // Member: sender
                            dbHelper.saveChatMember(
                                ChatMember(
                                    id = java.util.UUID.randomUUID().toString(),
                                    chatId = chatId,
                                    deviceId = fromDeviceId,
                                    code = fromCode,
                                    displayName = fromDisplayName,
                                    online = true,
                                    status = "pending_outgoing"
                                )
                            )
                        }

                        dbHelper.saveRequest(
                            ChatRequest(
                                id = reqId,
                                chatId = chatId,
                                fromDeviceId = fromDeviceId,
                                toDeviceId = currentDevice.id,
                                fromCode = fromCode,
                                toCode = toCode,
                                fromDisplayName = fromDisplayName,
                                toDisplayName = toDisplayName,
                                status = status,
                                createdAt = createdAt
                            )
                        )
                    } else {
                        // Update status and mutual display names if changed
                        val localReq = existingRequests[reqId]
                        if (localReq != null && (localReq.status != status || (toDisplayName.isNotBlank() && localReq.toDisplayName != toDisplayName))) {
                            dbHelper.updateRequestStatus(reqId, status)
                            val chatStatus = if (status == "accepted") "active" else "rejected"
                            dbHelper.updateChatStatus(chatId, chatStatus)

                            // If recipient accepted, and we are the sender, update chat name to recipient's display name!
                            if (toDisplayName.isNotBlank() && localReq.fromDeviceId == currentDevice.id) {
                                dbHelper.updateChatName(chatId, toDisplayName)
                            }
                            hasNew = true
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        return hasNew
    }

    // Fetch messages for all chats
    private fun fetchMessagesForAllChats(baseUrl: String, currentDevice: Device): Boolean {
        var hasNew = false
        val chats = dbHelper.getAllChats()

        for (chat in chats) {
            val httpReq = Request.Builder()
                .url("$baseUrl/wasla/messages/${chat.id}.json")
                .get()
                .build()

            try {
                client.newCall(httpReq).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    if (body == "null" || body.isBlank()) return@use

                    val json = JSONObject(body)
                    val keys = json.keys()
                    val existingMsgIds = chat.messages.map { it.id }.toSet()

                    while (keys.hasNext()) {
                        val msgId = keys.next()
                        if (!existingMsgIds.contains(msgId)) {
                            val msgObj = json.optJSONObject(msgId) ?: continue
                            val senderId = msgObj.optString("senderId")
                            val senderCode = msgObj.optString("senderCode")
                            val text = msgObj.optString("text")
                            val createdAt = msgObj.optLong("createdAt", System.currentTimeMillis())

                            // Decode image if present
                            val imageBase64 = msgObj.optString("imageBase64", "")
                            var localImagePath: String? = null
                            if (imageBase64.isNotBlank()) {
                                localImagePath = WaslaImageUtils.saveBase64Image(context, imageBase64)
                            }

                            dbHelper.saveMessage(
                                Message(
                                    id = msgId,
                                    chatId = chat.id,
                                    senderId = senderId,
                                    senderCode = senderCode,
                                    text = text,
                                    imageUri = localImagePath,
                                    createdAt = createdAt
                                )
                            )
                            hasNew = true
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return hasNew
    }
}
