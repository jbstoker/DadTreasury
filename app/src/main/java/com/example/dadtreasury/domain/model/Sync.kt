package com.example.dadtreasury.domain.model

/**
 * Event-based sync model.
 *
 * Per spec §6.4: each sync event must have unique ID, sender ID, recipient ID,
 * timestamp, revision, signature. Repeated delivery should not duplicate changes.
 */
enum class SyncEventType {
    TASK_CREATED,
    TASK_UPDATED,
    TASK_COMPLETED,
    TASK_APPROVED,
    TASK_REJECTED,
    WALLET_CREDITED,
    WALLET_DEBITED,
    TIME_CREDITED,
    TIME_DEBITED,
    CHAT_MESSAGE_SENT,
    CALENDAR_EVENT_CREATED,
    CALENDAR_EVENT_UPDATED,
    GEO_RULE_CREATED,
    GEO_RULE_UPDATED,
    LIBRARY_PAGE_UPDATED,
    PAIRING_REQUEST,
    PAIRING_APPROVED,
    PAIRING_REVOKED,
    DEVICE_REVOKED,
}

data class SyncEvent(
    val id: String,
    val type: SyncEventType,
    val senderDeviceId: String,
    val recipientDeviceId: String?,
    val payload: String,          // serialized JSON payload
    val timestamp: Long = System.currentTimeMillis(),
    val revision: Int = 1,
    val signature: String = "",   // app-layer signature
    val protocolVersion: Int = 1,
)

enum class SyncStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
    RETRYING
}

data class SyncQueueItem(
    val id: String,
    val event: SyncEvent,
    val status: SyncStatus = SyncStatus.PENDING,
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long? = null,
)

/**
 * Transport abstraction - per spec §6.1, Meshtastic / Bluetooth / Wi-Fi Direct
 * are behind interfaces.
 */
interface SyncTransport {
    val name: String
    val isAvailable: Boolean
    suspend fun sendEvent(event: SyncEvent): Boolean
    suspend fun receiveEvents(): List<SyncEvent>
}

/**
 * In-memory transport for local testing / single-device simulation.
 */
class LocalTransport(
    override val name: String = "local",
    private val inbox: MutableList<SyncEvent> = mutableListOf(),
) : SyncTransport {
    override val isAvailable: Boolean = true
    override suspend fun sendEvent(event: SyncEvent): Boolean {
        inbox.add(event)
        return true
    }
    override suspend fun receiveEvents(): List<SyncEvent> = synchronized(inbox) {
        val events = inbox.toList()
        inbox.clear()
        events
    }
}