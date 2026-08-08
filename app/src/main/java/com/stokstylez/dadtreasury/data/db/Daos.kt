package com.stokstylez.dadtreasury.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    /** One-shot query for widget usage (no Flow required). */
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC LIMIT 10")
    suspend fun getAllOnce(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions WHERE childId = :childId ORDER BY timestamp DESC")
    fun observeForChild(childId: String): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tx: WalletTransactionEntity)
}

@Dao
interface TimeBankDao {
    @Query("SELECT * FROM time_bank_transactions WHERE childId = :childId ORDER BY timestamp DESC")
    fun observeForChild(childId: String): Flow<List<TimeBankTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tx: TimeBankTransactionEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_threads ORDER BY lastMessageAt DESC")
    fun observeThreads(): Flow<List<ChatThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThread(thread: ChatThreadEntity)
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events WHERE startTimestamp >= :from AND startTimestamp <= :to ORDER BY startTimestamp ASC")
    fun observeBetween(from: Long, to: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events ORDER BY startTimestamp ASC")
    fun observeAll(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface GeoRuleDao {
    @Query("SELECT * FROM geo_rules ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GeoRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: GeoRuleEntity)

    @Query("DELETE FROM geo_rules WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library_categories ORDER BY name ASC")
    fun observeCategories(): Flow<List<LibraryCategoryEntity>>

    @Query("SELECT * FROM library_pages ORDER BY title ASC")
    fun observePages(): Flow<List<LibraryPageEntity>>

    @Query("SELECT * FROM library_pages WHERE categoryId = :categoryId ORDER BY title ASC")
    fun observePagesInCategory(categoryId: String): Flow<List<LibraryPageEntity>>

    @Query("SELECT * FROM library_revisions WHERE pageId = :pageId ORDER BY revision DESC")
    fun observeRevisions(pageId: String): Flow<List<LibraryRevisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: LibraryCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPage(page: LibraryPageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRevision(revision: LibraryRevisionEntity)
}

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    fun observeQueue(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_events WHERE id = :id")
    suspend fun getEventById(id: String): SyncEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvent(event: SyncEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQueueItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteQueueItem(id: String)
}

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device_identities ORDER BY pairedAt DESC")
    fun observeAll(): Flow<List<DeviceIdentityEntity>>

    @Query("SELECT * FROM device_identities WHERE deviceId = :deviceId")
    suspend fun getById(deviceId: String): DeviceIdentityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: DeviceIdentityEntity)

    @Query("DELETE FROM device_identities WHERE deviceId = :deviceId")
    suspend fun deleteById(deviceId: String)
}

@Dao
interface HouseholdDao {
    @Query("SELECT * FROM households")
    fun observeHouseholds(): Flow<List<HouseholdEntity>>

    @Query("SELECT * FROM parents")
    fun observeParents(): Flow<List<ParentEntity>>

    @Query("SELECT * FROM children")
    fun observeChildren(): Flow<List<ChildEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHousehold(household: HouseholdEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParent(parent: ParentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChild(child: ChildEntity)
}

@Dao
interface AppConnectionDao {
    @Query("SELECT * FROM app_connections ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AppConnectionEntity>>

    @Query("SELECT * FROM app_connections WHERE id = :id")
    suspend fun getById(id: String): AppConnectionEntity?

    @Query("SELECT * FROM app_connections WHERE pairingCode = :pairingCode LIMIT 1")
    suspend fun getByPairingCode(pairingCode: String): AppConnectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(connection: AppConnectionEntity)

    @Query("DELETE FROM app_connections WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface SharedLibraryPageDao {
    @Query("SELECT * FROM shared_library_pages ORDER BY lastSyncedAt DESC")
    fun observeAll(): Flow<List<SharedLibraryPageEntity>>

    @Query("SELECT * FROM shared_library_pages WHERE connectionId = :connectionId ORDER BY lastSyncedAt DESC")
    fun observeForConnection(connectionId: String): Flow<List<SharedLibraryPageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(sharedPage: SharedLibraryPageEntity)

    @Query("DELETE FROM shared_library_pages WHERE id = :id")
    suspend fun deleteById(id: String)
}
