package com.example.dadtreasury.domain.model

/**
 * Secure app-to-app chat messages.
 *
 * Per spec §8: text-first, short-message optimized, encrypted and signed at the app layer.
 */
data class ChatMessage(
    val id: String,
    val threadId: String,
    val senderRole: Role,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncEventId: String? = null,
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
)

data class ChatThread(
    val id: String,
    val parentId: String,
    val childId: String,
    val lastMessageAt: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
)