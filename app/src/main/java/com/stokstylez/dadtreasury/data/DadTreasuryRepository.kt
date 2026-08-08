package com.stokstylez.dadtreasury.data

import android.content.Context
import com.stokstylez.dadtreasury.data.db.*
import com.stokstylez.dadtreasury.domain.model.*
import com.stokstylez.dadtreasury.familysync.GoogleFamilyLinkManager
import com.stokstylez.dadtreasury.location.ProximityAlertManager
import com.stokstylez.dadtreasury.location.TimeReminderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Central repository - coordinates DAOs, honors offline-first, ledger-based,
 * and sync-aware design per the specs.
 */
class DadTreasuryRepository(
    private val db: AppDatabase,
    private val context: Context? = null,
) {
    private val taskDao = db.taskDao()
    private val walletDao = db.walletDao()
    private val timeBankDao = db.timeBankDao()
    private val chatDao = db.chatDao()
    private val calendarDao = db.calendarDao()
    private val geoRuleDao = db.geoRuleDao()
    private val libraryDao = db.libraryDao()
    private val syncDao = db.syncDao()
    private val deviceDao = db.deviceDao()
    private val householdDao = db.householdDao()
    private val appConnectionDao = db.appConnectionDao()
    private val sharedLibraryPageDao = db.sharedLibraryPageDao()

    // ---- Tasks ----
    fun observeTasks(): Flow<List<Task>> =
        taskDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getTask(id: String): Task? = taskDao.getById(id)?.toDomain()

    suspend fun createTask(
        title: String,
        description: String = "",
        expectedDurationMinutes: Int = 0,
        dueTimestamp: Long? = null,
        rewardType: RewardType = RewardType.FREE,
        rewardAmount: Long = 0,
        notes: String = "",
        checklist: List<String> = emptyList(),
    ) {
        val now = System.currentTimeMillis()
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            expectedDurationMinutes = expectedDurationMinutes,
            dueTimestamp = dueTimestamp,
            rewardType = rewardType,
            rewardAmount = rewardAmount,
            notes = notes,
            checklist = checklist,
            createdAt = now,
            updatedAt = now,
        )
        taskDao.upsert(task.toEntity())
        enqueueSyncEvent(SyncEventType.TASK_CREATED, task.id, task.title)
    }

    /** Child marks a task complete, optionally with a photo of the result. */
    suspend fun completeTask(taskId: String, childId: String, completionPhotoUri: String? = null) {
        val task = taskDao.getById(taskId) ?: return
        taskDao.upsert(
            task.copy(
                status = TaskStatus.COMPLETED.name,
                approvalState = ApprovalState.PENDING.name,
                completionPhotoUri = completionPhotoUri,
                updatedAt = System.currentTimeMillis(),
            )
        )
        enqueueSyncEvent(SyncEventType.TASK_COMPLETED, taskId, childId)
    }

    /** Parent approves a completed task - reward is created immediately per spec §7.5. */
    suspend fun approveTask(taskId: String, childId: String) {
        val task = taskDao.getById(taskId) ?: return
        val now = System.currentTimeMillis()

        taskDao.upsert(
            task.copy(
                status = TaskStatus.APPROVED.name,
                approvalState = ApprovalState.APPROVED.name,
                updatedAt = now,
            )
        )

        // Create reward ledger entry immediately per spec §7 flow step 5
        when (RewardType.valueOf(task.rewardType)) {
            RewardType.PAID -> {
                val tx = WalletTransaction(
                    id = UUID.randomUUID().toString(),
                    childId = childId,
                    type = LedgerEntryType.CREDIT,
                    amountCents = task.rewardAmount,
                    note = "Task approved: ${task.title}",
                    taskId = taskId,
                    timestamp = now,
                )
                walletDao.upsert(tx.toEntity())
                enqueueSyncEvent(SyncEventType.WALLET_CREDITED, tx.id, task.title)
            }
            RewardType.TIME -> {
                val tx = TimeBankTransaction(
                    id = UUID.randomUUID().toString(),
                    childId = childId,
                    type = LedgerEntryType.CREDIT,
                    amountMinutes = task.rewardAmount,
                    note = "Task approved: ${task.title}",
                    taskId = taskId,
                    timestamp = now,
                )
                timeBankDao.upsert(tx.toEntity())
                enqueueSyncEvent(SyncEventType.TIME_CREDITED, tx.id, task.title)

                // Google Family Link: queue a screen-time grant so the child
                // gets more screen/app time immediately (when approved + online).
                context?.let {
                    GoogleFamilyLinkManager.grantScreenTime(
                        context = it,
                        childId = childId,
                        minutes = task.rewardAmount,
                        taskTitle = task.title,
                    )
                }
            }
            RewardType.FREE -> { /* no ledger entry */ }
        }

        enqueueSyncEvent(SyncEventType.TASK_APPROVED, taskId, childId)
    }

    /** Parent rejects a completed task. */
    suspend fun rejectTask(taskId: String) {
        val task = taskDao.getById(taskId) ?: return
        taskDao.upsert(
            task.copy(
                status = TaskStatus.REJECTED.name,
                approvalState = ApprovalState.REJECTED.name,
                updatedAt = System.currentTimeMillis(),
            )
        )
        enqueueSyncEvent(SyncEventType.TASK_REJECTED, taskId, "")
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.deleteById(taskId)
    }

    // ---- Wallet ----
    fun observeWallet(childId: String): Flow<List<WalletTransaction>> =
        walletDao.observeForChild(childId).map { list -> list.map { it.toDomain() } }

    fun observeWalletBalance(childId: String): Flow<Long> =
        observeWallet(childId).map { walletBalance(it) }

    suspend fun addWalletTransaction(
        childId: String,
        type: LedgerEntryType,
        amountCents: Long,
        note: String,
    ) {
        val tx = WalletTransaction(
            id = UUID.randomUUID().toString(),
            childId = childId,
            type = type,
            amountCents = amountCents,
            note = note,
            timestamp = System.currentTimeMillis(),
        )
        walletDao.upsert(tx.toEntity())
        enqueueSyncEvent(
            if (type == LedgerEntryType.CREDIT) SyncEventType.WALLET_CREDITED else SyncEventType.WALLET_DEBITED,
            tx.id,
            note,
        )
    }

    // ---- Time Bank ----
    fun observeTimeBank(childId: String): Flow<List<TimeBankTransaction>> =
        timeBankDao.observeForChild(childId).map { list -> list.map { it.toDomain() } }

    fun observeTimeBankBalance(childId: String): Flow<Long> =
        observeTimeBank(childId).map { timeBankBalance(it) }

    suspend fun addTimeTransaction(
        childId: String,
        type: LedgerEntryType,
        amountMinutes: Long,
        note: String,
    ) {
        val tx = TimeBankTransaction(
            id = UUID.randomUUID().toString(),
            childId = childId,
            type = type,
            amountMinutes = amountMinutes,
            note = note,
            timestamp = System.currentTimeMillis(),
        )
        timeBankDao.upsert(tx.toEntity())
        enqueueSyncEvent(
            if (type == LedgerEntryType.CREDIT) SyncEventType.TIME_CREDITED else SyncEventType.TIME_DEBITED,
            tx.id,
            note,
        )
    }

    // ---- Chat ----
    fun observeMessages(threadId: String): Flow<List<ChatMessage>> =
        chatDao.observeMessages(threadId).map { list -> list.map { it.toDomain() } }

    fun observeThreads(): Flow<List<ChatThread>> =
        chatDao.observeThreads().map { list -> list.map { it.toDomain() } }

    suspend fun sendMessage(threadId: String, senderRole: Role, text: String) {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            threadId = threadId,
            senderRole = senderRole,
            text = text,
            timestamp = System.currentTimeMillis(),
        )
        chatDao.upsertMessage(msg.toEntity())
        enqueueSyncEvent(SyncEventType.CHAT_MESSAGE_SENT, msg.id, text)
    }

    // ---- Calendar ----
    fun observeAllEvents(): Flow<List<CalendarEvent>> =
        calendarDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun addCalendarEvent(
        title: String,
        description: String,
        startTimestamp: Long,
        endTimestamp: Long,
        isRecurring: Boolean = false,
        recurrenceRule: String? = null,
        reminderMinutes: Int? = null,
    ) {
        val event = CalendarEvent(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            startTimestamp = startTimestamp,
            endTimestamp = endTimestamp,
            isRecurring = isRecurring,
            recurrenceRule = recurrenceRule,
            reminderMinutes = reminderMinutes,
            createdAt = System.currentTimeMillis(),
        )
        calendarDao.upsert(event.toEntity())

        // Schedule time-based reminder via AlarmManager if requested
        reminderMinutes?.let { mins ->
            val triggerAt = startTimestamp - (mins * 60_000L)
            if (triggerAt > System.currentTimeMillis()) {
                context?.let {
                    TimeReminderManager.scheduleReminder(
                        context = it,
                        reminderId = "event_" + event.id,
                        triggerAtMillis = triggerAt,
                        message = "Reminder: $title",
                    )
                }
            }
        }

        enqueueSyncEvent(SyncEventType.CALENDAR_EVENT_CREATED, event.id, title)
    }

    suspend fun deleteCalendarEvent(eventId: String) {
        calendarDao.deleteById(eventId)
    }

    // ---- Geo Rules ----
    fun observeGeoRules(): Flow<List<GeoRule>> =
        geoRuleDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun addGeoRule(
        title: String,
        message: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        activeStartHour: Int,
        activeEndHour: Int,
        taskId: String? = null,
    ) {
        val rule = GeoRule(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
            activeStartHour = activeStartHour,
            activeEndHour = activeEndHour,
            taskId = taskId,
            createdAt = System.currentTimeMillis(),
        )
        geoRuleDao.upsert(rule.toEntity())

        // Register native proximity alert (no Google Play Services)
        context?.let {
            ProximityAlertManager.registerProximityAlert(
                context = it,
                ruleId = rule.id,
                taskId = rule.taskId,
                message = message,
                latitude = latitude,
                longitude = longitude,
                radiusMeters = radiusMeters.toFloat(),
                expirationMillis = -1L, // Never expire; active hours checked at trigger
            )
        }

        enqueueSyncEvent(SyncEventType.GEO_RULE_CREATED, rule.id, title)
    }

    suspend fun deleteGeoRule(ruleId: String) {
        context?.let { ProximityAlertManager.removeProximityAlert(it, ruleId) }
        geoRuleDao.deleteById(ruleId)
    }

    // ---- Library ----
    fun observeCategories(): Flow<List<LibraryCategory>> =
        libraryDao.observeCategories().map { list -> list.map { it.toDomain() } }

    fun observePages(): Flow<List<LibraryPage>> =
        libraryDao.observePages().map { list -> list.map { it.toDomain() } }

    suspend fun addCategory(name: String) {
        val category = LibraryCategory(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = System.currentTimeMillis(),
        )
        libraryDao.upsertCategory(category.toEntity())
    }

    suspend fun addPage(categoryId: String, title: String, body: String, tags: List<String> = emptyList()) {
        val now = System.currentTimeMillis()
        val page = LibraryPage(
            id = UUID.randomUUID().toString(),
            categoryId = categoryId,
            title = title,
            body = body,
            tags = tags,
            revision = 1,
            updatedAt = now,
            createdAt = now,
        )
        libraryDao.upsertPage(page.toEntity())
        enqueueSyncEvent(SyncEventType.LIBRARY_PAGE_UPDATED, page.id, title)
    }

    suspend fun updatePage(page: LibraryPage, note: String = "") {
        val updated = page.copy(
            revision = page.revision + 1,
            updatedAt = System.currentTimeMillis(),
        )
        libraryDao.upsertPage(updated.toEntity())
        libraryDao.upsertRevision(
            LibraryRevisionEntity(
                id = UUID.randomUUID().toString(),
                pageId = updated.id,
                revision = updated.revision,
                body = updated.body,
                note = note,
                timestamp = System.currentTimeMillis(),
            )
        )
        enqueueSyncEvent(SyncEventType.LIBRARY_PAGE_UPDATED, updated.id, updated.title)
    }

    // ---- Household ----
    fun observeHousehold(): Flow<List<Household>> =
        householdDao.observeHouseholds().map { list ->
            list.map {
                Household(id = it.id, name = it.name, createdAt = it.createdAt)
            }
        }

    fun observeChildren(): Flow<List<ChildProfile>> =
        householdDao.observeChildren().map { list ->
            list.map {
                ChildProfile(
                    id = it.id,
                    householdId = it.householdId,
                    parentId = it.parentId,
                    displayName = it.displayName,
                    deviceId = it.deviceId,
                    avatarColor = it.avatarColor,
                    createdAt = it.createdAt,
                )
            }
        }

    suspend fun createHousehold(name: String) {
        householdDao.upsertHousehold(
            HouseholdEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun addChild(householdId: String, parentId: String, displayName: String, deviceId: String) {
        householdDao.upsertChild(
            ChildEntity(
                id = UUID.randomUUID().toString(),
                householdId = householdId,
                parentId = parentId,
                displayName = displayName,
                deviceId = deviceId,
                avatarColor = 0,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    // ---- Devices / Pairing ----
    fun observeDevices(): Flow<List<DeviceIdentity>> =
        deviceDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun addDevice(device: DeviceIdentity) {
        deviceDao.upsert(device.toEntity())
    }

    suspend fun revokeDevice(deviceId: String) {
        val device = deviceDao.getById(deviceId) ?: return
        deviceDao.upsert(
            device.copy(
                isTrusted = false,
                isRevoked = true,
                revokedAt = System.currentTimeMillis(),
            )
        )
        enqueueSyncEvent(SyncEventType.DEVICE_REVOKED, deviceId, "")
    }

    // ---- Parent App Connections ----
    fun observeAppConnections(): Flow<List<AppConnection>> =
        appConnectionDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun createAppConnection(displayName: String): String {
        val id = UUID.randomUUID().toString()
        val pairingCode = generatePairingCode()
        appConnectionDao.upsert(
            AppConnectionEntity(
                id = id,
                displayName = displayName,
                pairingCode = pairingCode,
                peerDeviceId = "",
                isTrusted = false,
                isRevoked = false,
                createdAt = System.currentTimeMillis(),
                lastSyncAt = null,
            )
        )
        enqueueSyncEvent(SyncEventType.PARENT_APP_CONNECTED, id, displayName)
        return pairingCode
    }

    suspend fun acceptAppConnection(pairingCode: String, myDisplayName: String) {
        // This device accepts a pairing code generated by another parent app.
        // Create (or reuse) a connection record keyed to that code.
        val existing = appConnectionDao.getByPairingCode(pairingCode)
        val connection = existing ?: AppConnectionEntity(
            id = UUID.randomUUID().toString(),
            displayName = myDisplayName,
            pairingCode = pairingCode,
            peerDeviceId = "",
            isTrusted = true,
            isRevoked = false,
            createdAt = System.currentTimeMillis(),
            lastSyncAt = System.currentTimeMillis(),
        )
        appConnectionDao.upsert(connection)
        enqueueSyncEvent(SyncEventType.PARENT_APP_CONNECTED, connection.id, myDisplayName)
    }

    suspend fun disconnectAppConnection(connectionId: String) {
        val conn = appConnectionDao.getById(connectionId) ?: return
        appConnectionDao.upsert(
            conn.copy(
                isTrusted = false,
                isRevoked = true,
                lastSyncAt = System.currentTimeMillis(),
            )
        )
        enqueueSyncEvent(SyncEventType.PARENT_APP_DISCONNECTED, connectionId, conn.displayName)
    }

    suspend fun shareLibraryPage(connectionId: String, pageId: String) {
        sharedLibraryPageDao.upsert(
            SharedLibraryPageEntity(
                id = UUID.randomUUID().toString(),
                connectionId = connectionId,
                origin = PageOrigin.LOCAL.name,
                pageId = pageId,
                lastSyncedAt = System.currentTimeMillis(),
            )
        )
        appConnectionDao.getById(connectionId)?.let {
            appConnectionDao.upsert(it.copy(lastSyncAt = System.currentTimeMillis()))
        }
        enqueueSyncEvent(SyncEventType.PARENT_LIBRARY_PAGE_SHARED, pageId, connectionId)
    }

    suspend fun sendParentMessage(connectionId: String, text: String) {
        chatDao.upsertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                threadId = "parent-$connectionId",
                senderRole = Role.PARENT.name,
                text = text,
                timestamp = System.currentTimeMillis(),
                syncEventId = null,
                isDelivered = false,
                isRead = false,
            )
        )
        enqueueSyncEvent(SyncEventType.PARENT_CHAT_MESSAGE, connectionId, text)
    }

    fun observeSharedPages(): Flow<List<SharedLibraryPageEntity>> =
        sharedLibraryPageDao.observeAll()

    private fun generatePairingCode(): String {
        val chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    // ---- Sync ----
    fun observeSyncQueue(): Flow<List<SyncEvent>> =
        syncDao.observeQueue().map { items ->
            items.mapNotNull { item ->
                syncDao.getEventById(item.eventId)?.toDomain()
            }
        }

    private suspend fun enqueueSyncEvent(type: SyncEventType, entityId: String, payload: String) {
        val event = SyncEvent(
            id = UUID.randomUUID().toString(),
            type = type,
            senderDeviceId = "local-device",
            recipientDeviceId = null,
            payload = payload,
            timestamp = System.currentTimeMillis(),
            signature = "signed-${entityId}",
        )
        syncDao.upsertEvent(event.toEntity())
        syncDao.upsertQueueItem(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                eventId = event.id,
                status = SyncStatus.PENDING.name,
                retryCount = 0,
                createdAt = System.currentTimeMillis(),
                lastAttemptAt = null,
            )
        )
    }
}