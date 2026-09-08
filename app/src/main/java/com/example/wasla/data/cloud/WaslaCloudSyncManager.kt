package com.example.wasla.data.cloud

import android.content.Context
import android.content.SharedPreferences
import com.example.wasla.data.db.WaslaDatabaseHelper
import com.example.wasla.data.model.Chat
import com.example.wasla.data.model.ChatMember
import com.example.wasla.data.model.ChatRequest
import com.example.wasla.data.model.Device
import com.example.wasla.data.model.Message
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
    val statusMessage: String = "جاهز للمزامنة"
)

class WaslaCloudSyncManager(
    private val context: Context,
    private val dbHelper: WaslaDatabaseHelper
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wasla_cloud_prefs", Context.MODE_PRIVATE)

    companion object {
        // Default public demo Firebase Realtime Database endpoint for Wasla
        const val DEFAULT_FIREBASE_URL = "https://wasla-chat-app-default-rtdb.firebaseio.com"
        private const val PREF_KEY_SERVER_URL = "cloud_server_url"
        private const val PREF_KEY_CLOUD_ENABLED = "cloud_sync_enabled"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null

    private val _cloudStatus = MutableStateFlow(CloudStatus.OFFLINE_LOCAL)
    val cloudStatus: StateFlow<CloudStatus> = _cloudStatus.asStateFlow()

    private val _config = MutableStateFlow(
        CloudConfig(
            serverUrl = prefs.getString(PREF_KEY_SERVER_URL, DEFAULT_FIREBASE_URL) ?: DEFAULT_FIREBASE_URL,
            isEnabled = prefs.getBoolean(PREF_KEY_CLOUD_ENABLED, true)
        )
    )
    val config: StateFlow<CloudConfig> = _config.asStateFlow()

    // Listener callback for when new data arrives from cloud
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
                delay(4000) // Poll every 4 seconds
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
            _cloudStatus.value = CloudStatus.SYNCING
            val baseUrl = getNormalizedBaseUrl()

            // 1. Publish our device identity & presence to Cloud
            publishDevicePresence(baseUrl, currentDevice)

            // 2. Fetch incoming requests addressed to our device code
            val hasNewRequests = fetchIncomingRequests(baseUrl, currentDevice)

            // 3. Fetch messages for each of our active chats
            val hasNewMessages = fetchMessagesForAllChats(baseUrl, currentDevice)

            _cloudStatus.value = CloudStatus.CONNECTED
            _config.value = _config.value.copy(
                lastSyncTime = System.currentTimeMillis(),
                statusMessage = "متصل بالسحابة • كل شيء محدث"
            )

            if (hasNewRequests || hasNewMessages) {
                onDataSynchronized?.invoke()
            }
        } catch (e: Exception) {
            _cloudStatus.value = CloudStatus.ERROR
            _config.value = _config.value.copy(
                statusMessage = "تعذر الاتصال بالسحابة: ${e.localizedMessage ?: "خطأ في الشبكة"}"
            )
        }
    }

    // Publish current device
    private fun publishDevicePresence(baseUrl: String, device: Device) {
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
    }

    // Publish an outgoing chat request to the recipient's inbox on the cloud
    fun uploadChatRequest(request: ChatRequest) {
        scope.launch {
            try {
                val baseUrl = getNormalizedBaseUrl()
                val bodyJson = JSONObject().apply {
                    put("id", request.id)
                    put("chatId", request.chatId)
                    put("fromDeviceId", request.fromDeviceId)
                    put("toDeviceId", request.toDeviceId)
                    put("fromCode", request.fromCode)
                    put("toCode", request.toCode)
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
                    put("name", "وصلة ${request.fromCode}")
                    put("status", "pending_incoming")
                    put("updatedAt", request.createdAt)
                    put("senderCode", request.fromCode)
                    put("targetCode", request.toCode)
                }
                val chatHttpReq = Request.Builder()
                    .url("$baseUrl/wasla/chats/${request.chatId}.json")
                    .put(chatJson.toString().toRequestBody(jsonMediaType))
                    .build()
                client.newCall(chatHttpReq).execute().close()
            } catch (e: Exception) {
                // Keep local if cloud fails
            }
        }
    }

    // Update request status (e.g. accepted / rejected) in cloud
    fun uploadRequestStatus(request: ChatRequest, newStatus: String) {
        scope.launch {
            try {
                val baseUrl = getNormalizedBaseUrl()
                val bodyJson = JSONObject().apply {
                    put("status", newStatus)
                }

                val httpReq = Request.Builder()
                    .url("$baseUrl/wasla/requests/${request.toCode}/${request.id}/status.json")
                    .put(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()
                client.newCall(httpReq).execute().close()

                // Also update chat status
                val chatStatus = if (newStatus == "accepted") "active" else "rejected"
                val chatHttpReq = Request.Builder()
                    .url("$baseUrl/wasla/chats/${request.chatId}/status.json")
                    .put("\"$chatStatus\"".toRequestBody(jsonMediaType))
                    .build()
                client.newCall(chatHttpReq).execute().close()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Upload a message to cloud
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
                }

                val httpReq = Request.Builder()
                    .url("$baseUrl/wasla/messages/${message.chatId}/${message.id}.json")
                    .put(bodyJson.toString().toRequestBody(jsonMediaType))
                    .build()

                client.newCall(httpReq).execute().close()
            } catch (e: Exception) {
                // Retained in local SQLite
            }
        }
    }

    // Fetch incoming requests from cloud
    private fun fetchIncomingRequests(baseUrl: String, currentDevice: Device): Boolean {
        var hasNew = false
        val httpReq = Request.Builder()
            .url("$baseUrl/wasla/requests/${currentDevice.code}.json")
            .get()
            .build()

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
                val status = reqObj.optString("status", "pending")
                val createdAt = reqObj.optLong("createdAt", System.currentTimeMillis())

                if (!existingRequests.containsKey(reqId)) {
                    hasNew = true
                    // Create local chat if doesn't exist
                    val existingChat = dbHelper.getChatById(chatId)
                    if (existingChat == null) {
                        val chat = Chat(
                            id = chatId,
                            type = "direct",
                            name = "وصلة $fromCode",
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
                                displayName = "وصلة $fromCode",
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
                            status = status,
                            createdAt = createdAt
                        )
                    )
                } else {
                    // Update status if changed remotely
                    val localReq = existingRequests[reqId]
                    if (localReq != null && localReq.status != status) {
                        dbHelper.updateRequestStatus(reqId, status)
                        val chatStatus = if (status == "accepted") "active" else "rejected"
                        dbHelper.updateChatStatus(chatId, chatStatus)
                        hasNew = true
                    }
                }
            }
        }
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

                            dbHelper.saveMessage(
                                Message(
                                    id = msgId,
                                    chatId = chat.id,
                                    senderId = senderId,
                                    senderCode = senderCode,
                                    text = text,
                                    createdAt = createdAt
                                )
                            )
                            hasNew = true
                        }
                    }
                }
            } catch (e: Exception) {
                // Continue to next chat
            }
        }
        return hasNew
    }
}
