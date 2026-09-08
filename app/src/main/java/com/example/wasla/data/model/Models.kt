package com.example.wasla.data.model

data class Device(
    val id: String,
    val code: String,
    val displayName: String,
    val online: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatMember(
    val id: String,
    val chatId: String,
    val deviceId: String,
    val code: String,
    val displayName: String,
    val online: Boolean = true,
    val status: String = "active" // active, pending_incoming, pending_outgoing, rejected
)

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderCode: String? = null,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatRequest(
    val id: String,
    val chatId: String,
    val fromDeviceId: String,
    val toDeviceId: String,
    val fromCode: String,
    val toCode: String,
    val status: String = "pending", // pending, accepted, rejected
    val createdAt: Long = System.currentTimeMillis()
)

data class Chat(
    val id: String,
    val type: String, // "direct" or "group"
    val name: String,
    val status: String = "active", // active, pending_incoming, pending_outgoing, rejected
    val updatedAt: Long = System.currentTimeMillis(),
    val members: List<ChatMember> = emptyList(),
    val messages: List<Message> = emptyList()
)
