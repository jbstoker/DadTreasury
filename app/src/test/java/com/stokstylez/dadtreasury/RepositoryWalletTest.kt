package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.data.db.*
import com.stokstylez.dadtreasury.domain.model.LedgerEntryType
import com.stokstylez.dadtreasury.domain.model.Role
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFailsWith
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepositoryWalletTest {

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

    private fun stubSyncDao() {
        every { syncDao.upsertEvent(any()) } just runs
        every { syncDao.upsertQueueItem(any()) } just runs
    }

    // ---- Wallet ----

    @Test
    fun `addWalletTransaction requires parent role`() = runTest {
        val ex = assertFailsWith<IllegalArgumentException> {
            repository.addWalletTransaction("c1", LedgerEntryType.CREDIT, 100, "test")
        }
        assertTrue(ex.message!!.contains("Only the parent"))
    }

    @Test
    fun `addWalletTransaction creates transaction and enqueues sync`() = runTest {
        repository.currentRole = "PARENT"
        stubSyncDao()
        val slot = slot<WalletTransactionEntity>()
        every { walletDao.upsert(capture(slot)) } just runs

        repository.addWalletTransaction("c1", LedgerEntryType.CREDIT, 250, "Allowance")

        val tx = slot.captured
        assertEquals("c1", tx.childId)
        assertEquals(LedgerEntryType.CREDIT.name, tx.type)
        assertEquals(250L, tx.amountCents)
        assertEquals("Allowance", tx.note)
    }

    @Test
    fun `observeWallet maps entities to domain`() = runTest {
        val entities = listOf(
            WalletTransactionEntity(
                id = "w1", childId = "c1", type = LedgerEntryType.CREDIT.name,
                amountCents = 100, note = "n", taskId = null, timestamp = 1L, syncEventId = null,
            )
        )
        every { walletDao.observeForChild("c1") } returns flowOf(entities)

        val result = repository.observeWallet("c1").first()

        assertEquals(1, result.size)
        assertEquals("w1", result[0].id)
        assertEquals(LedgerEntryType.CREDIT, result[0].type)
        assertEquals(100L, result[0].amountCents)
    }

    @Test
    fun `observeWalletBalance computes sum`() = runTest {
        val entities = listOf(
            WalletTransactionEntity("w1", "c1", LedgerEntryType.CREDIT.name, 500, "", null, 1L, null),
            WalletTransactionEntity("w2", "c1", LedgerEntryType.DEBIT.name, 100, "", null, 2L, null),
        )
        every { walletDao.observeForChild("c1") } returns flowOf(entities)

        val balance = repository.observeWalletBalance("c1").first()

        assertEquals(400L, balance)
    }

    // ---- Time Bank ----

    @Test
    fun `addTimeTransaction requires parent role`() = runTest {
        val ex = assertFailsWith<IllegalArgumentException> {
            repository.addTimeTransaction("c1", LedgerEntryType.CREDIT, 30, "test")
        }
        assertTrue(ex.message!!.contains("Only the parent"))
    }

    @Test
    fun `addTimeTransaction creates transaction and enqueues sync`() = runTest {
        repository.currentRole = "PARENT"
        stubSyncDao()
        val slot = slot<TimeBankTransactionEntity>()
        every { timeBankDao.upsert(capture(slot)) } just runs

        repository.addTimeTransaction("c1", LedgerEntryType.CREDIT, 30, "Chore")

        val tx = slot.captured
        assertEquals("c1", tx.childId)
        assertEquals(LedgerEntryType.CREDIT.name, tx.type)
        assertEquals(30L, tx.amountMinutes)
        assertEquals("Chore", tx.note)
    }

    @Test
    fun `observeTimeBank maps entities to domain`() = runTest {
        val entities = listOf(
            TimeBankTransactionEntity(
                id = "tb1", childId = "c1", type = LedgerEntryType.CREDIT.name,
                amountMinutes = 45, note = "n", taskId = null, timestamp = 1L, syncEventId = null,
            )
        )
        every { timeBankDao.observeForChild("c1") } returns flowOf(entities)

        val result = repository.observeTimeBank("c1").first()

        assertEquals(1, result.size)
        assertEquals("tb1", result[0].id)
        assertEquals(45L, result[0].amountMinutes)
    }

    @Test
    fun `observeTimeBankBalance computes sum`() = runTest {
        val entities = listOf(
            TimeBankTransactionEntity("tb1", "c1", LedgerEntryType.CREDIT.name, 60, "", null, 1L, null),
            TimeBankTransactionEntity("tb2", "c1", LedgerEntryType.PAYOUT.name, 20, "", null, 2L, null),
        )
        every { timeBankDao.observeForChild("c1") } returns flowOf(entities)

        val balance = repository.observeTimeBankBalance("c1").first()

        assertEquals(40L, balance)
    }

    // ---- Chat ----

    @Test
    fun `sendMessage creates message and enqueues sync`() = runTest {
        stubSyncDao()
        val slot = slot<ChatMessageEntity>()
        every { chatDao.upsertMessage(capture(slot)) } just runs

        repository.sendMessage("th1", Role.CHILD, "Done!")

        val msg = slot.captured
        assertEquals("th1", msg.threadId)
        assertEquals(Role.CHILD.name, msg.senderRole)
        assertEquals("Done!", msg.text)
    }

    @Test
    fun `observeMessages maps entities to domain`() = runTest {
        val entities = listOf(
            ChatMessageEntity(
                id = "m1", threadId = "th1", senderRole = Role.PARENT.name,
                text = "Hello", timestamp = 1L, syncEventId = null,
                isDelivered = true, isRead = true,
            )
        )
        every { chatDao.observeMessages("th1") } returns flowOf(entities)

        val result = repository.observeMessages("th1").first()

        assertEquals(1, result.size)
        assertEquals("m1", result[0].id)
        assertEquals(Role.PARENT, result[0].senderRole)
        assertTrue(result[0].isDelivered)
        assertTrue(result[0].isRead)
    }

    @Test
    fun `observeThreads maps entities to domain`() = runTest {
        val entities = listOf(
            ChatThreadEntity("th1", "p1", "c1", 1000L, 2)
        )
        every { chatDao.observeThreads() } returns flowOf(entities)

        val result = repository.observeThreads().first()

        assertEquals(1, result.size)
        assertEquals("th1", result[0].id)
        assertEquals("p1", result[0].parentId)
        assertEquals("c1", result[0].childId)
        assertEquals(2, result[0].unreadCount)
    }

    // ---- Calendar ----

    @Test
    fun `addCalendarEvent requires parent role`() = runTest {
        val ex = assertFailsWith<IllegalArgumentException> {
            repository.addCalendarEvent("Event", "", 100L, 200L)
        }
        assertTrue(ex.message!!.contains("Only the parent"))
    }

    @Test
    fun `addCalendarEvent creates event and enqueues sync`() = runTest {
        repository.currentRole = "PARENT"
        stubSyncDao()
        val slot = slot<CalendarEventEntity>()
        every { calendarDao.upsert(capture(slot)) } just runs

        repository.addCalendarEvent(
            title = "Soccer",
            description = "Practice",
            startTimestamp = 1000L,
            endTimestamp = 2000L,
            isRecurring = true,
            recurrenceRule = "FREQ=WEEKLY",
            reminderMinutes = 30,
        )

        val event = slot.captured
        assertEquals("Soccer", event.title)
        assertEquals("Practice", event.description)
        assertEquals(1000L, event.startTimestamp)
        assertEquals(2000L, event.endTimestamp)
        assertTrue(event.isRecurring)
        assertEquals("FREQ=WEEKLY", event.recurrenceRule)
        assertEquals(30, event.reminderMinutes)
    }

    @Test
    fun `observeAllEvents maps entities to domain`() = runTest {
        val entities = listOf(
            CalendarEventEntity(
                id = "e1", title = "Event", description = "D",
                startTimestamp = 100L, endTimestamp = 200L,
                isRecurring = false, recurrenceRule = null, reminderMinutes = null,
                routineId = null, createdAt = 1L,
            )
        )
        every { calendarDao.observeAll() } returns flowOf(entities)

        val result = repository.observeAllEvents().first()

        assertEquals(1, result.size)
        assertEquals("e1", result[0].id)
        assertEquals("Event", result[0].title)
    }

    // ---- Library ----

    @Test
    fun `addCategory requires parent role`() = runTest {
        val ex = assertFailsWith<IllegalArgumentException> {
            repository.addCategory("Chores")
        }
        assertTrue(ex.message!!.contains("Only the parent"))
    }

    @Test
    fun `addCategory creates category`() = runTest {
        repository.currentRole = "PARENT"
        val slot = slot<LibraryCategoryEntity>()
        every { libraryDao.upsertCategory(capture(slot)) } just runs

        repository.addCategory("Chores")

        val cat = slot.captured
        assertEquals("Chores", cat.name)
    }

    @Test
    fun `addPage requires parent role`() = runTest {
        val ex = assertFailsWith<IllegalArgumentException> {
            repository.addPage("c1", "Title", "Body")
        }
        assertTrue(ex.message!!.contains("Only the parent"))
    }

    @Test
    fun `addPage creates page with revision 1 and enqueues sync`() = runTest {
        repository.currentRole = "PARENT"
        stubSyncDao()
        val slot = slot<LibraryPageEntity>()
        every { libraryDao.upsertPage(capture(slot)) } just runs

        repository.addPage("c1", "Budget", "How to budget", listOf("money", "kid"))

        val page = slot.captured
        assertEquals("c1", page.categoryId)
        assertEquals("Budget", page.title)
        assertEquals("How to budget", page.body)
        assertEquals(listOf("money", "kid"), page.tags)
        assertEquals(1, page.revision)
    }

    @Test
    fun `updatePage increments revision and creates revision entry`() = runTest {
        repository.currentRole = "PARENT"
        stubSyncDao()
        every { libraryDao.upsertPage(any()) } just runs

        val revSlot = slot<LibraryRevisionEntity>()
        every { libraryDao.upsertRevision(capture(revSlot)) } just runs

        val page = com.stokstylez.dadtreasury.domain.model.LibraryPage(
            id = "p1", categoryId = "c1", title = "Budget", body = "Updated",
            tags = emptyList(), revision = 2, updatedAt = 100L, createdAt = 100L,
        )
        repository.updatePage(page, note = "Improved")

        assertEquals(3, revSlot.captured.revision)
        assertEquals("Updated", revSlot.captured.body)
        assertEquals("Improved", revSlot.captured.note)
        assertEquals("p1", revSlot.captured.pageId)
    }

    @Test
    fun `observeCategories maps entities to domain`() = runTest {
        val entities = listOf(
            LibraryCategoryEntity("c1", "Chores", null, 1L)
        )
        every { libraryDao.observeCategories() } returns flowOf(entities)

        val result = repository.observeCategories().first()

        assertEquals(1, result.size)
        assertEquals("c1", result[0].id)
        assertEquals("Chores", result[0].name)
    }

    @Test
    fun `observePages maps entities to domain`() = runTest {
        val entities = listOf(
            LibraryPageEntity(
                id = "p1", categoryId = "c1", title = "Title", body = "Body",
                tags = listOf("a"), revision = 1, updatedAt = 1L, createdAt = 1L,
            )
        )
        every { libraryDao.observePages() } returns flowOf(entities)

        val result = repository.observePages().first()

        assertEquals(1, result.size)
        assertEquals("p1", result[0].id)
        assertEquals(listOf("a"), result[0].tags)
    }
}