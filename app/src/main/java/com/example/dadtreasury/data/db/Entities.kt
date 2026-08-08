package com.example.dadtreasury.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dadtreasury.domain.model.ApprovalState
import com.example.dadtreasury.domain.model.LedgerEntryType
import com.example.dadtreasury.domain.model.Role
import com.example.dadtreasury.domain.model.RewardType
import com.example.dadtreasury.domain.model.SyncEventType
import com.example.dadtreasury.domain.model.SyncStatus
import com.example.dadtreasury.domain.model.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val expectedDurationMinutes: Int,
    val dueTimestamp: Long?,
    val rewardType: String,
    val rewardAmount: Long,
    val status: String,
    val approvalState: String,
    val notes: String,
    val locationRuleId: String?,
    val checklist: List<String>,
    val completionPhotoUri: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val type: String,
    val amountCents: Long,
    val note: String,
    val taskId: String?,
    val timestamp: Long,
    val syncEventId: String?,
)

@Entity(tableName = "time_bank_transactions")
data class TimeBankTransactionEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val type: String,
    val amountMinutes: Long,
    val note: String,
    val taskId: String?,
    val timestamp: Long,
    val syncEventId: String?,
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val senderRole: String,
    val text: String,
    val timestamp: Long,
    val syncEventId: String?,
    val isDelivered: Boolean,
    val isRead: Boolean,
)

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val parentId: String,
    val childId: String,
    val lastMessageAt: Long,
    val unreadCount: Int,
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val isRecurring: Boolean,
    val recurrenceRule: String?,
    val reminderMinutes: Int?,
    val routineId: String?,
    val createdAt: Long,
)

@Entity(tableName = "geo_rules")
data class GeoRuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int,
    val activeStartHour: Int,
    val activeEndHour: Int,
    val repeatDaily: Boolean,
    val isEnabled: Boolean,
    val taskId: String?,
    val createdAt: Long,
)

@Entity(tableName = "library_categories")
data class LibraryCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentId: String?,
    val createdAt: Long,
)

@Entity(tableName = "library_pages")
data class LibraryPageEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val title: String,
    val body: String,
    val tags: List<String>,
    val revision: Int,
    val updatedAt: Long,
    val createdAt: Long,
)

@Entity(tableName = "library_revisions")
data class LibraryRevisionEntity(
    @PrimaryKey val id: String,
    val pageId: String,
    val revision: Int,
    val body: String,
    val note: String,
    val timestamp: Long,
)

@Entity(tableName = "sync_events")
data class SyncEventEntity(
    @PrimaryKey val id: String,
    val type: String,
    val senderDeviceId: String,
    val recipientDeviceId: String?,
    val payload: String,
    val timestamp: Long,
    val revision: Int,
    val signature: String,
    val protocolVersion: Int,
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val status: String,
    val retryCount: Int,
    val createdAt: Long,
    val lastAttemptAt: Long?,
)

@Entity(tableName = "device_identities")
data class DeviceIdentityEntity(
    @PrimaryKey val deviceId: String,
    val displayName: String,
    val publicKey: String,
    val role: String,
    val isTrusted: Boolean,
    val isRevoked: Boolean,
    val pairedAt: Long,
    val revokedAt: Long?,
)

@Entity(tableName = "households")
data class HouseholdEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long,
)

@Entity(tableName = "parents")
data class ParentEntity(
    @PrimaryKey val id: String,
    val householdId: String,
    val displayName: String,
    val deviceId: String,
    val createdAt: Long,
)

@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey val id: String,
    val householdId: String,
    val parentId: String,
    val displayName: String,
    val deviceId: String,
    val avatarColor: Int,
    val createdAt: Long,
)