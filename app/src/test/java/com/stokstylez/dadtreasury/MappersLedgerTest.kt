package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.data.db.TimeBankTransactionEntity
import com.stokstylez.dadtreasury.data.db.WalletTransactionEntity
import com.stokstylez.dadtreasury.data.toDomain
import com.stokstylez.dadtreasury.data.toEntity
import com.stokstylez.dadtreasury.domain.model.LedgerEntryType
import com.stokstylez.dadtreasury.domain.model.TimeBankTransaction
import com.stokstylez.dadtreasury.domain.model.WalletTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersLedgerTest {

    @Test
    fun `wallet entity to domain maps all fields`() {
        val entity = WalletTransactionEntity(
            id = "w1",
            childId = "c1",
            type = LedgerEntryType.CREDIT.name,
            amountCents = 250,
            note = "Allowance",
            taskId = "t1",
            timestamp = 999L,
            syncEventId = "s1",
        )

        val domain = entity.toDomain()

        assertEquals("w1", domain.id)
        assertEquals("c1", domain.childId)
        assertEquals(LedgerEntryType.CREDIT, domain.type)
        assertEquals(250L, domain.amountCents)
        assertEquals("Allowance", domain.note)
        assertEquals("t1", domain.taskId)
        assertEquals(999L, domain.timestamp)
        assertEquals("s1", domain.syncEventId)
    }

    @Test
    fun `wallet domain to entity maps all fields`() {
        val domain = WalletTransaction(
            id = "w1",
            childId = "c1",
            type = LedgerEntryType.DEBIT,
            amountCents = 100,
            note = "Spent",
            taskId = null,
            timestamp = 100L,
            syncEventId = null,
        )

        val entity = domain.toEntity()

        assertEquals("w1", entity.id)
        assertEquals("c1", entity.childId)
        assertEquals(LedgerEntryType.DEBIT.name, entity.type)
        assertEquals(100L, entity.amountCents)
        assertEquals("Spent", entity.note)
        assertNull(entity.taskId)
        assertEquals(100L, entity.timestamp)
        assertNull(entity.syncEventId)
    }

    @Test
    fun `wallet round-trip preserves all fields`() {
        val original = WalletTransaction(
            id = "w9",
            childId = "c9",
            type = LedgerEntryType.REVERSAL,
            amountCents = 42,
            note = "undo",
            taskId = "t9",
            timestamp = 77L,
            syncEventId = "e9",
        )
        assertEquals(original, original.toEntity().toDomain())
    }

    @Test
    fun `time bank entity to domain maps all fields`() {
        val entity = TimeBankTransactionEntity(
            id = "tb1",
            childId = "c1",
            type = LedgerEntryType.CREDIT.name,
            amountMinutes = 30,
            note = "Chore reward",
            taskId = "t2",
            timestamp = 111L,
            syncEventId = null,
        )

        val domain = entity.toDomain()

        assertEquals("tb1", domain.id)
        assertEquals("c1", domain.childId)
        assertEquals(LedgerEntryType.CREDIT, domain.type)
        assertEquals(30L, domain.amountMinutes)
        assertEquals("Chore reward", domain.note)
        assertEquals("t2", domain.taskId)
        assertEquals(111L, domain.timestamp)
        assertNull(domain.syncEventId)
    }

    @Test
    fun `time bank domain to entity maps all fields`() {
        val domain = TimeBankTransaction(
            id = "tb1",
            childId = "c1",
            type = LedgerEntryType.PAYOUT,
            amountMinutes = 15,
            note = "Time spend",
            taskId = null,
            timestamp = 222L,
            syncEventId = "s2",
        )

        val entity = domain.toEntity()

        assertEquals("tb1", entity.id)
        assertEquals("c1", entity.childId)
        assertEquals(LedgerEntryType.PAYOUT.name, entity.type)
        assertEquals(15L, entity.amountMinutes)
        assertEquals("Time spend", entity.note)
        assertNull(entity.taskId)
        assertEquals(222L, entity.timestamp)
        assertEquals("s2", entity.syncEventId)
    }

    @Test
    fun `time bank round-trip preserves all fields`() {
        val original = TimeBankTransaction(
            id = "tb9",
            childId = "c9",
            type = LedgerEntryType.CORRECTION,
            amountMinutes = 5,
            note = "fix",
            taskId = "t9",
            timestamp = 123L,
            syncEventId = "e9",
        )
        assertEquals(original, original.toEntity().toDomain())
    }
}