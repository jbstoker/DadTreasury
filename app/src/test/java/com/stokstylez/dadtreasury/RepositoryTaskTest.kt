package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.data.db.*
import com.stokstylez.dadtreasury.data.toEntity
import com.stokstylez.dadtreasury.domain.model.ApprovalState
import com.stokstylez.dadtreasury.domain.model.LedgerEntryType
import com.stokstylez.dadtreasury.domain.model.RewardType
import com.stokstylez.dadtreasury.domain.model.TaskStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepositoryTaskTest {

    private lateinit var taskDao: TaskDao
    private lateinit var walletDao: WalletDao
    private lateinit var timeBankDao: TimeBankDao
    private lateinit var chatDao: ChatDao
    private lateinit var calendarDao: CalendarDao
    private lateinit var geoRuleDao: GeoRuleDao
    private lateinit var libraryDao: LibraryDao
    private lateinit var syncDao: SyncDao
    private lateinit var deviceDao: DeviceDao
    private lateinit var householdDao: HouseholdDao
    private lateinit var appConnectionDao: AppConnectionDao
    private lateinit var sharedLibraryPageDao: SharedLibraryPageDao
    private lateinit var db: AppDatabase
    private lateinit var repository: DadTreasuryRepository

    @Before
    fun setUp() {
        taskDao = mockk()
        walletDao = mockk()
        timeBankDao = mockk()
        chatDao = mockk()
        calendarDao = mockk()
        geoRuleDao = mockk()
        libraryDao = mockk()
        syncDao = mockk()
        deviceDao = mockk()
        householdDao = mockk()
        appConnectionDao = mockk()
        sharedLibraryPageDao = mockk()

        db = mockk {
            every { taskDao() } returns taskDao
            every { walletDao() } returns walletDao
            every { timeBankDao() } returns timeBankDao
            every { chatDao() } returns chatDao
            every { calendarDao() } returns calendarDao
            every { geoRuleDao() } returns geoRuleDao
            every { libraryDao() } returns libraryDao
            every { syncDao() } returns syncDao
            every { deviceDao() } returns deviceDao
            every { householdDao() } returns householdDao
            every { appConnectionDao() } returns appConnectionDao
            every { sharedLibraryPageDao() } returns sharedLibraryPageDao
        }

        repository = DadTreasuryRepository(db, context = null)
    }

    @Test
    fun `createTask requires parent role`() = runTest {
        // setup - ensure sync DAO is not needed since call should fail before enqueue
        val result = runCatching { repository.createTask(title = "Test") }
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Only the parent"))
    }

    @Test
    fun `createTask with parent creates task and enqueues sync`() = runTest {
        repository.currentRole = "PARENT"
        coEvery { syncDao.upsertEvent(any()) } just runs
        coEvery { syncDao.upsertQueueItem(any()) } just runs

        val taskSlot = slot<TaskEntity>()
        coEvery { taskDao.upsert(capture(taskSlot)) } just runs

        repository.createTask(
            title = "Clean room",
            description = "Tidy up",
            expectedDurationMinutes = 30,
            rewardType = RewardType.PAID,
            rewardAmount = 500,
            notes = "Use vacuum",
            checklist = listOf("tidy", "vacuum"),
        )

        val saved = taskSlot.captured
        assertEquals("Clean room", saved.title)
        assertEquals("Tidy up", saved.description)
        assertEquals(30, saved.expectedDurationMinutes)
        assertEquals(RewardType.PAID.name, saved.rewardType)
        assertEquals(500L, saved.rewardAmount)
        assertEquals(TaskStatus.OPEN.name, saved.status)
        assertEquals(ApprovalState.NOT_SUBMITTED.name, saved.approvalState)
        assertEquals("Use vacuum", saved.notes)
        assertEquals(listOf("tidy", "vacuum"), saved.checklist)

        coVerify(exactly = 1) { syncDao.upsertEvent(any()) }
        coVerify(exactly = 1) { syncDao.upsertQueueItem(any()) }
    }

    @Test
    fun `completeTask updates status and approval state`() = runTest {
        coEvery { syncDao.upsertEvent(any()) } just runs
        coEvery { syncDao.upsertQueueItem(any()) } just runs

        val existing = TaskEntity(
            id = "t1", title = "Task", description = "", expectedDurationMinutes = 0,
            dueTimestamp = null, rewardType = RewardType.FREE.name, rewardAmount = 0,
            status = TaskStatus.OPEN.name, approvalState = ApprovalState.NOT_SUBMITTED.name,
            notes = "", locationRuleId = null, checklist = emptyList(),
            completionPhotoUri = null, childId = null, createdAt = 100L, updatedAt = 100L,
        )
        coEvery { taskDao.getById("t1") } returns existing

        val updatedSlot = slot<TaskEntity>()
        coEvery { taskDao.upsert(capture(updatedSlot)) } just runs

        repository.completeTask(taskId = "t1", childId = "c1", completionPhotoUri = "content://photo")

        val updated = updatedSlot.captured
        assertEquals(TaskStatus.COMPLETED.name, updated.status)
        assertEquals(ApprovalState.PENDING.name, updated.approvalState)
        assertEquals("content://photo", updated.completionPhotoUri)
    }

    @Test
    fun `approveTask with PAID reward creates wallet credit`() = runTest {
        repository.currentRole = "PARENT"
        coEvery { syncDao.upsertEvent(any()) } just runs
        coEvery { syncDao.upsertQueueItem(any()) } just runs

        val existing = TaskEntity(
            id = "t1", title = "Paid chore", description = "", expectedDurationMinutes = 10,
            dueTimestamp = null, rewardType = RewardType.PAID.name, rewardAmount = 200,
            status = TaskStatus.COMPLETED.name, approvalState = ApprovalState.PENDING.name,
            notes = "", locationRuleId = null, checklist = emptyList(),
            completionPhotoUri = null, childId = null, createdAt = 100L, updatedAt = 100L,
        )
        coEvery { taskDao.getById("t1") } returns existing
        coEvery { taskDao.upsert(any()) } just runs

        val walletSlot = slot<WalletTransactionEntity>()
        coEvery { walletDao.upsert(capture(walletSlot)) } just runs

        repository.approveTask(taskId = "t1", childId = "c1")

        val walletTx = walletSlot.captured
        assertEquals("c1", walletTx.childId)
        assertEquals(LedgerEntryType.CREDIT.name, walletTx.type)
        assertEquals(200L, walletTx.amountCents)
        assertEquals("Paid chore", walletTx.note.substringAfter(": "))

        coVerify(exactly = 0) { timeBankDao.upsert(any()) }
    }

    @Test
    fun `approveTask with TIME reward creates time bank credit`() = runTest {
        repository.currentRole = "PARENT"
        coEvery { syncDao.upsertEvent(any()) } just runs
        coEvery { syncDao.upsertQueueItem(any()) } just runs

        val existing = TaskEntity(
            id = "t2", title = "Time chore", description = "", expectedDurationMinutes = 15,
            dueTimestamp = null, rewardType = RewardType.TIME.name, rewardAmount = 60,
            status = TaskStatus.COMPLETED.name, approvalState = ApprovalState.PENDING.name,
            notes = "", locationRuleId = null, checklist = emptyList(),
            completionPhotoUri = null, childId = null, createdAt = 100L, updatedAt = 100L,
        )
        coEvery { taskDao.getById("t2") } returns existing
        coEvery { taskDao.upsert(any()) } just runs

        val timeSlot = slot<TimeBankTransactionEntity>()
        coEvery { timeBankDao.upsert(capture(timeSlot)) } just runs

        repository.approveTask(taskId = "t2", childId = "c2")

        val timeTx = timeSlot.captured
        assertEquals("c2", timeTx.childId)
        assertEquals(LedgerEntryType.CREDIT.name, timeTx.type)
        assertEquals(60L, timeTx.amountMinutes)

        coVerify(exactly = 0) { walletDao.upsert(any()) }
    }

    @Test
    fun `approveTask with FREE reward no ledger entry`() = runTest {
        repository.currentRole = "PARENT"
        coEvery { syncDao.upsertEvent(any()) } just runs
        coEvery { syncDao.upsertQueueItem(any()) } just runs

        val existing = TaskEntity(
            id = "t3", title = "Free chore", description = "", expectedDurationMinutes = 5,
            dueTimestamp = null, rewardType = RewardType.FREE.name, rewardAmount = 0,
            status = TaskStatus.COMPLETED.name, approvalState = ApprovalState.PENDING.name,
            notes = "", locationRuleId = null, checklist = emptyList(),
            completionPhotoUri = null, childId = null, createdAt = 100L, updatedAt = 100L,
        )
        coEvery { taskDao.getById("t3") } returns existing
        coEvery { taskDao.upsert(any()) } just runs

        repository.approveTask(taskId = "t3", childId = "c3")

        coVerify(exactly = 0) { walletDao.upsert(any()) }
        coVerify(exactly = 0) { timeBankDao.upsert(any()) }
    }

    @Test
    fun `rejectTask updates status and approval state`() = runTest {
        repository.currentRole = "PARENT"
        coEvery { syncDao.upsertEvent(any()) } just runs
        coEvery { syncDao.upsertQueueItem(any()) } just runs

        val existing = TaskEntity(
            id = "t4", title = "Task", description = "", expectedDurationMinutes = 0,
            dueTimestamp = null, rewardType = RewardType.FREE.name, rewardAmount = 0,
            status = TaskStatus.COMPLETED.name, approvalState = ApprovalState.PENDING.name,
            notes = "", locationRuleId = null, checklist = emptyList(),
            completionPhotoUri = null, childId = null, createdAt = 100L, updatedAt = 100L,
        )
        coEvery { taskDao.getById("t4") } returns existing

        val updatedSlot = slot<TaskEntity>()
        coEvery { taskDao.upsert(capture(updatedSlot)) } just runs

        repository.rejectTask(taskId = "t4")

        val updated = updatedSlot.captured
        assertEquals(TaskStatus.REJECTED.name, updated.status)
        assertEquals(ApprovalState.REJECTED.name, updated.approvalState)
    }

    @Test
    fun `observeTasks maps entities to domain`() = runTest {
        val entities = listOf(
            TaskEntity(
                id = "t1", title = "Chore", description = "D", expectedDurationMinutes = 5,
                dueTimestamp = null, rewardType = RewardType.PAID.name, rewardAmount = 100,
                status = TaskStatus.OPEN.name, approvalState = ApprovalState.NOT_SUBMITTED.name,
                notes = "", locationRuleId = null, checklist = emptyList(),
                completionPhotoUri = null, childId = null, createdAt = 1L, updatedAt = 1L,
            )
        )
        every { taskDao.observeAll() } returns flowOf(entities)

        val flow = repository.observeTasks()
        val result = flow.first()

        assertEquals(1, result.size)
        assertEquals("t1", result[0].id)
        assertEquals("Chore", result[0].title)
        assertEquals(RewardType.PAID, result[0].rewardType)
        assertEquals(TaskStatus.OPEN, result[0].status)
    }

    @Test
    fun `getTask returns null when missing`() = runTest {
        coEvery { taskDao.getById("missing") } returns null
        val task = repository.getTask("missing")
        assertEquals(null, task)
    }

    @Test
    fun `getTask returns mapped task`() = runTest {
        val entity = TaskEntity(
            id = "t1", title = "Chore", description = "D", expectedDurationMinutes = 5,
            dueTimestamp = null, rewardType = RewardType.FREE.name, rewardAmount = 0,
            status = TaskStatus.APPROVED.name, approvalState = ApprovalState.APPROVED.name,
            notes = "", locationRuleId = null, checklist = emptyList(),
            completionPhotoUri = null, childId = null, createdAt = 1L, updatedAt = 1L,
        )
        coEvery { taskDao.getById("t1") } returns entity

        val task = repository.getTask("t1")

        assertEquals("t1", task!!.id)
        assertEquals(TaskStatus.APPROVED, task.status)
        assertEquals(ApprovalState.APPROVED, task.approvalState)
    }
}