package com.example.dadtreasury

import com.example.dadtreasury.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerTest {

    @Test
    fun `wallet balance starts at zero`() {
        assertEquals(0L, walletBalance(emptyList()))
    }

    @Test
    fun `wallet balance sums credits and debits correctly`() {
        val transactions = listOf(
            WalletTransaction(id = "1", childId = "c1", type = LedgerEntryType.CREDIT, amountCents = 500),
            WalletTransaction(id = "2", childId = "c1", type = LedgerEntryType.CREDIT, amountCents = 250),
            WalletTransaction(id = "3", childId = "c1", type = LedgerEntryType.DEBIT, amountCents = 100),
            WalletTransaction(id = "4", childId = "c1", type = LedgerEntryType.PAYOUT, amountCents = 200),
        )
        assertEquals(450L, walletBalance(transactions))
    }

    @Test
    fun `wallet balance handles correction entries`() {
        val transactions = listOf(
            WalletTransaction(id = "1", childId = "c1", type = LedgerEntryType.CREDIT, amountCents = 1000),
            WalletTransaction(id = "2", childId = "c1", type = LedgerEntryType.CORRECTION, amountCents = 50),
        )
        assertEquals(1050L, walletBalance(transactions))
    }

    @Test
    fun `wallet balance handles reversals`() {
        val original = WalletTransaction(id = "1", childId = "c1", type = LedgerEntryType.CREDIT, amountCents = 1000)
        val reversal = WalletTransaction(id = "2", childId = "c1", type = LedgerEntryType.REVERSAL, amountCents = 1000)
        assertEquals(0L, walletBalance(listOf(original, reversal)))
    }

    @Test
    fun `time bank balance sums credits and debits correctly`() {
        val transactions = listOf(
            TimeBankTransaction(id = "1", childId = "c1", type = LedgerEntryType.CREDIT, amountMinutes = 30),
            TimeBankTransaction(id = "2", childId = "c1", type = LedgerEntryType.DEBIT, amountMinutes = 10),
        )
        assertEquals(20L, timeBankBalance(transactions))
    }

    @Test
    fun `time bank balance can go negative with overspending`() {
        val transactions = listOf(
            TimeBankTransaction(id = "1", childId = "c1", type = LedgerEntryType.CREDIT, amountMinutes = 5),
            TimeBankTransaction(id = "2", childId = "c1", type = LedgerEntryType.DEBIT, amountMinutes = 15),
        )
        assertEquals(-10L, timeBankBalance(transactions))
    }
}