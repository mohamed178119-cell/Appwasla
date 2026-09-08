package com.example.wasla.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.wasla.data.model.Chat
import com.example.wasla.data.model.ChatMember
import com.example.wasla.data.model.ChatRequest
import com.example.wasla.data.model.Device
import com.example.wasla.data.model.Message

class WaslaDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "wasla.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS devices (
                id TEXT PRIMARY KEY,
                code TEXT NOT NULL UNIQUE,
                display_name TEXT NOT NULL,
                online INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS chats (
                id TEXT PRIMARY KEY,
                type TEXT NOT NULL,
                name TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'active',
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS chat_members (
                id TEXT PRIMARY KEY,
                chat_id TEXT NOT NULL,
                device_id TEXT NOT NULL,
                code TEXT NOT NULL,
                display_name TEXT NOT NULL,
                online INTEGER NOT NULL DEFAULT 1,
                status TEXT NOT NULL DEFAULT 'active'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS chat_requests (
                id TEXT PRIMARY KEY,
                chat_id TEXT NOT NULL,
                from_device_id TEXT NOT NULL,
                to_device_id TEXT NOT NULL,
                from_code TEXT NOT NULL,
                to_code TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'pending',
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS messages (
                id TEXT PRIMARY KEY,
                chat_id TEXT NOT NULL,
                sender_id TEXT NOT NULL,
                sender_code TEXT,
                text TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS messages")
        db.execSQL("DROP TABLE IF EXISTS chat_requests")
        db.execSQL("DROP TABLE IF EXISTS chat_members")
        db.execSQL("DROP TABLE IF EXISTS chats")
        db.execSQL("DROP TABLE IF EXISTS devices")
        onCreate(db)
    }

    private val sharedPrefs = context.getSharedPreferences("wasla_db_prefs", Context.MODE_PRIVATE)

    fun setActiveDeviceId(deviceId: String) {
        sharedPrefs.edit().putString("active_device_id", deviceId).apply()
    }

    fun getActiveDeviceId(): String? {
        return sharedPrefs.getString("active_device_id", null)
    }

    // Devices
    fun saveDevice(device: Device) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("id", device.id)
                put("code", device.code)
                put("display_name", device.displayName)
                put("online", if (device.online) 1 else 0)
                put("created_at", device.createdAt)
            }
            db.insertWithOnConflict("devices", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
        if (getActiveDeviceId() == null) {
            setActiveDeviceId(device.id)
        }
    }

    fun getDevice(): Device? {
        val activeId = getActiveDeviceId()
        readableDatabase.use { db ->
            val cursor = if (activeId != null) {
                db.query("devices", null, "id = ?", arrayOf(activeId), null, null, null)
            } else {
                db.query("devices", null, null, null, null, null, "created_at ASC", "1")
            }
            cursor.use {
                if (it.moveToFirst()) {
                    val id = it.getString(it.getColumnIndexOrThrow("id"))
                    if (activeId == null) setActiveDeviceId(id)
                    return Device(
                        id = id,
                        code = it.getString(it.getColumnIndexOrThrow("code")),
                        displayName = it.getString(it.getColumnIndexOrThrow("display_name")),
                        online = it.getInt(it.getColumnIndexOrThrow("online")) == 1,
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                }
            }
        }
        return null
    }

    fun getAllDevices(): List<Device> {
        val list = mutableListOf<Device>()
        readableDatabase.use { db ->
            val cursor = db.query("devices", null, null, null, null, null, "created_at ASC")
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        Device(
                            id = it.getString(it.getColumnIndexOrThrow("id")),
                            code = it.getString(it.getColumnIndexOrThrow("code")),
                            displayName = it.getString(it.getColumnIndexOrThrow("display_name")),
                            online = it.getInt(it.getColumnIndexOrThrow("online")) == 1,
                            createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                        )
                    )
                }
            }
        }
        return list
    }

    fun updatePresence(deviceId: String, online: Boolean) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("online", if (online) 1 else 0)
            }
            db.update("devices", values, "id = ?", arrayOf(deviceId))
        }
    }

    // Chats
    fun saveChat(chat: Chat) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("id", chat.id)
                put("type", chat.type)
                put("name", chat.name)
                put("status", chat.status)
                put("updated_at", chat.updatedAt)
            }
            db.insertWithOnConflict("chats", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun updateChatStatus(chatId: String, status: String) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("status", status)
                put("updated_at", System.currentTimeMillis())
            }
            db.update("chats", values, "id = ?", arrayOf(chatId))
        }
    }

    fun getAllChats(): List<Chat> {
        val chats = mutableListOf<Chat>()
        readableDatabase.use { db ->
            val cursor = db.query("chats", null, null, null, null, null, "updated_at DESC")
            cursor.use {
                while (it.moveToNext()) {
                    val chatId = it.getString(it.getColumnIndexOrThrow("id"))
                    val type = it.getString(it.getColumnIndexOrThrow("type"))
                    val name = it.getString(it.getColumnIndexOrThrow("name"))
                    val status = it.getString(it.getColumnIndexOrThrow("status"))
                    val updatedAt = it.getLong(it.getColumnIndexOrThrow("updated_at"))

                    val members = getMembersForChat(db, chatId)
                    val messages = getMessagesForChat(db, chatId)

                    chats.add(
                        Chat(
                            id = chatId,
                            type = type,
                            name = name,
                            status = status,
                            updatedAt = updatedAt,
                            members = members,
                            messages = messages
                        )
                    )
                }
            }
        }
        return chats
    }

    fun getChatById(chatId: String): Chat? {
        readableDatabase.use { db ->
            val cursor = db.query("chats", null, "id = ?", arrayOf(chatId), null, null, null)
            cursor.use {
                if (it.moveToFirst()) {
                    val type = it.getString(it.getColumnIndexOrThrow("type"))
                    val name = it.getString(it.getColumnIndexOrThrow("name"))
                    val status = it.getString(it.getColumnIndexOrThrow("status"))
                    val updatedAt = it.getLong(it.getColumnIndexOrThrow("updated_at"))

                    val members = getMembersForChat(db, chatId)
                    val messages = getMessagesForChat(db, chatId)

                    return Chat(
                        id = chatId,
                        type = type,
                        name = name,
                        status = status,
                        updatedAt = updatedAt,
                        members = members,
                        messages = messages
                    )
                }
            }
        }
        return null
    }

    private fun getMembersForChat(db: SQLiteDatabase, chatId: String): List<ChatMember> {
        val members = mutableListOf<ChatMember>()
        val cursor = db.query("chat_members", null, "chat_id = ?", arrayOf(chatId), null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                members.add(
                    ChatMember(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        chatId = it.getString(it.getColumnIndexOrThrow("chat_id")),
                        deviceId = it.getString(it.getColumnIndexOrThrow("device_id")),
                        code = it.getString(it.getColumnIndexOrThrow("code")),
                        displayName = it.getString(it.getColumnIndexOrThrow("display_name")),
                        online = it.getInt(it.getColumnIndexOrThrow("online")) == 1,
                        status = it.getString(it.getColumnIndexOrThrow("status"))
                    )
                )
            }
        }
        return members
    }

    fun saveChatMember(member: ChatMember) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("id", member.id)
                put("chat_id", member.chatId)
                put("device_id", member.deviceId)
                put("code", member.code)
                put("display_name", member.displayName)
                put("online", if (member.online) 1 else 0)
                put("status", member.status)
            }
            db.insertWithOnConflict("chat_members", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    // Messages
    fun saveMessage(message: Message) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("id", message.id)
                put("chat_id", message.chatId)
                put("sender_id", message.senderId)
                put("sender_code", message.senderCode)
                put("text", message.text)
                put("created_at", message.createdAt)
            }
            db.insertWithOnConflict("messages", null, values, SQLiteDatabase.CONFLICT_REPLACE)

            // Update chat's updated_at
            val chatUpdate = ContentValues().apply {
                put("updated_at", message.createdAt)
            }
            db.update("chats", chatUpdate, "id = ?", arrayOf(message.chatId))
        }
    }

    private fun getMessagesForChat(db: SQLiteDatabase, chatId: String): List<Message> {
        val messages = mutableListOf<Message>()
        val cursor = db.query("messages", null, "chat_id = ?", arrayOf(chatId), null, null, "created_at ASC")
        cursor.use {
            while (it.moveToNext()) {
                messages.add(
                    Message(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        chatId = it.getString(it.getColumnIndexOrThrow("chat_id")),
                        senderId = it.getString(it.getColumnIndexOrThrow("sender_id")),
                        senderCode = it.getString(it.getColumnIndexOrThrow("sender_code")),
                        text = it.getString(it.getColumnIndexOrThrow("text")),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return messages
    }

    // Requests
    fun saveRequest(request: ChatRequest) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("id", request.id)
                put("chat_id", request.chatId)
                put("from_device_id", request.fromDeviceId)
                put("to_device_id", request.toDeviceId)
                put("from_code", request.fromCode)
                put("to_code", request.toCode)
                put("status", request.status)
                put("created_at", request.createdAt)
            }
            db.insertWithOnConflict("chat_requests", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun updateRequestStatus(requestId: String, status: String) {
        writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("status", status)
            }
            db.update("chat_requests", values, "id = ?", arrayOf(requestId))
        }
    }

    fun getAllRequests(): List<ChatRequest> {
        val requests = mutableListOf<ChatRequest>()
        readableDatabase.use { db ->
            val cursor = db.query("chat_requests", null, null, null, null, null, "created_at DESC")
            cursor.use {
                while (it.moveToNext()) {
                    requests.add(
                        ChatRequest(
                            id = it.getString(it.getColumnIndexOrThrow("id")),
                            chatId = it.getString(it.getColumnIndexOrThrow("chat_id")),
                            fromDeviceId = it.getString(it.getColumnIndexOrThrow("from_device_id")),
                            toDeviceId = it.getString(it.getColumnIndexOrThrow("to_device_id")),
                            fromCode = it.getString(it.getColumnIndexOrThrow("from_code")),
                            toCode = it.getString(it.getColumnIndexOrThrow("to_code")),
                            status = it.getString(it.getColumnIndexOrThrow("status")),
                            createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                        )
                    )
                }
            }
        }
        return requests
    }
}
