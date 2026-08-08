package com.example.dadtreasury.data

import com.example.dadtreasury.data.db.*
import com.example.dadtreasury.domain.model.*

// ---- Task ----
fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    expectedDurationMinutes = expectedDurationMinutes,
    dueTimestamp = dueTimestamp,
    rewardType = RewardType.valueOf(rewardType),
    rewardAmount = rewardAmount,
    status = TaskStatus.valueOf(status),
    approvalState = ApprovalState.valueOf(approvalState),
    notes = notes,
    locationRuleId = locationRuleId,
    checklist = checklist,
    completionPhotoUri = completionPhotoUri,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    expectedDurationMinutes = expectedDurationMinutes,
    dueTimestamp = dueTimestamp,
    rewardType = rewardType.name,
    rewardAmount = rewardAmount,
    status = status.name,
    approvalState = approvalState.name,
    notes = notes,
    locationRuleId = locationRuleId,
    checklist = checklist,
    completionPhotoUri = completionPhotoUri,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// ---- Wallet ----
fun WalletTransactionEntity.toDomain(): WalletTransaction = WalletTransaction(
    id = id,
    childId = childId,
    type = LedgerEntryType.valueOf(type),
    amountCents = amountCents,
    note = note,
    taskId = taskId,
    timestamp = timestamp,
    syncEventId = syncEventId,
)

fun WalletTransaction.toEntity(): WalletTransactionEntity = WalletTransactionEntity(
    id = id,
    childId = childId,
    type = type.name,
    amountCents = amountCents,
    note = note,
    taskId = taskId,
    timestamp = timestamp,
    syncEventId = syncEventId,
)

// ---- TimeBank ----
fun TimeBankTransactionEntity.toDomain(): TimeBankTransaction = TimeBankTransaction(
    id = id,
    childId = childId,
    type = LedgerEntryType.valueOf(type),
    amountMinutes = amountMinutes,
    note = note,
    taskId = taskId,
    timestamp = timestamp,
    syncEventId = syncEventId,
)

fun TimeBankTransaction.toEntity(): TimeBankTransactionEntity = TimeBankTransactionEntity(
    id = id,
    childId = childId,
    type = type.name,
    amountMinutes = amountMinutes,
    note = note,
    taskId = taskId,
    timestamp = timestamp,
    syncEventId = syncEventId,
)

// ---- Chat ----
fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    threadId = threadId,
    senderRole = Role.valueOf(senderRole),
    text = text,
    timestamp = timestamp,
    syncEventId = syncEventId,
    isDelivered = isDelivered,
    isRead = isRead,
)

fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    threadId = threadId,
    senderRole = senderRole.name,
    text = text,
    timestamp = timestamp,
    syncEventId = syncEventId,
    isDelivered = isDelivered,
    isRead = isRead,
)

fun ChatThreadEntity.toDomain(): ChatThread = ChatThread(
    id = id,
    parentId = parentId,
    childId = childId,
    lastMessageAt = lastMessageAt,
    unreadCount = unreadCount,
)

fun ChatThread.toEntity(): ChatThreadEntity = ChatThreadEntity(
    id = id,
    parentId = parentId,
    childId = childId,
    lastMessageAt = lastMessageAt,
    unreadCount = unreadCount,
)

// ---- Calendar ----
fun CalendarEventEntity.toDomain(): CalendarEvent = CalendarEvent(
    id = id,
    title = title,
    description = description,
    startTimestamp = startTimestamp,
    endTimestamp = endTimestamp,
    isRecurring = isRecurring,
    recurrenceRule = recurrenceRule,
    reminderMinutes = reminderMinutes,
    routineId = routineId,
    createdAt = createdAt,
)

fun CalendarEvent.toEntity(): CalendarEventEntity = CalendarEventEntity(
    id = id,
    title = title,
    description = description,
    startTimestamp = startTimestamp,
    endTimestamp = endTimestamp,
    isRecurring = isRecurring,
    recurrenceRule = recurrenceRule,
    reminderMinutes = reminderMinutes,
    routineId = routineId,
    createdAt = createdAt,
)

// ---- GeoRule ----
fun GeoRuleEntity.toDomain(): GeoRule = GeoRule(
    id = id,
    title = title,
    message = message,
    latitude = latitude,
    longitude = longitude,
    radiusMeters = radiusMeters,
    activeStartHour = activeStartHour,
    activeEndHour = activeEndHour,
    repeatDaily = repeatDaily,
    isEnabled = isEnabled,
    taskId = taskId,
    createdAt = createdAt,
)

fun GeoRule.toEntity(): GeoRuleEntity = GeoRuleEntity(
    id = id,
    title = title,
    message = message,
    latitude = latitude,
    longitude = longitude,
    radiusMeters = radiusMeters,
    activeStartHour = activeStartHour,
    activeEndHour = activeEndHour,
    repeatDaily = repeatDaily,
    isEnabled = isEnabled,
    taskId = taskId,
    createdAt = createdAt,
)

// ---- Library ----
fun LibraryCategoryEntity.toDomain(): LibraryCategory = LibraryCategory(
    id = id,
    name = name,
    parentId = parentId,
    createdAt = createdAt,
)

fun LibraryCategory.toEntity(): LibraryCategoryEntity = LibraryCategoryEntity(
    id = id,
    name = name,
    parentId = parentId,
    createdAt = createdAt,
)

fun LibraryPageEntity.toDomain(): LibraryPage = LibraryPage(
    id = id,
    categoryId = categoryId,
    title = title,
    body = body,
    tags = tags,
    revision = revision,
    updatedAt = updatedAt,
    createdAt = createdAt,
)

fun LibraryPage.toEntity(): LibraryPageEntity = LibraryPageEntity(
    id = id,
    categoryId = categoryId,
    title = title,
    body = body,
    tags = tags,
    revision = revision,
    updatedAt = updatedAt,
    createdAt = createdAt,
)

// ---- Sync ----
fun SyncEventEntity.toDomain(): SyncEvent = SyncEvent(
    id = id,
    type = SyncEventType.valueOf(type),
    senderDeviceId = senderDeviceId,
    recipientDeviceId = recipientDeviceId,
    payload = payload,
    timestamp = timestamp,
    revision = revision,
    signature = signature,
    protocolVersion = protocolVersion,
)

fun SyncEvent.toEntity(): SyncEventEntity = SyncEventEntity(
    id = id,
    type = type.name,
    senderDeviceId = senderDeviceId,
    recipientDeviceId = recipientDeviceId,
    payload = payload,
    timestamp = timestamp,
    revision = revision,
    signature = signature,
    protocolVersion = protocolVersion,
)

// ---- Device ----
fun DeviceIdentityEntity.toDomain(): DeviceIdentity = DeviceIdentity(
    deviceId = deviceId,
    displayName = displayName,
    publicKey = publicKey,
    role = Role.valueOf(role),
    isTrusted = isTrusted,
    isRevoked = isRevoked,
    pairedAt = pairedAt,
    revokedAt = revokedAt,
)

fun DeviceIdentity.toEntity(): DeviceIdentityEntity = DeviceIdentityEntity(
    deviceId = deviceId,
    displayName = displayName,
    publicKey = publicKey,
    role = role.name,
    isTrusted = isTrusted,
    isRevoked = isRevoked,
    pairedAt = pairedAt,
    revokedAt = revokedAt,
)