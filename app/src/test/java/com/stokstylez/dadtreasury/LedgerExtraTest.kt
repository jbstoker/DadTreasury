package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerExtraTest {

    @Test
    fun `wallet balance handles all entry types`() {
        val txs = listOf(
            WalletTransaction("1", "c1", LedgerEntryType.CREDIT, 100),
            WalletTransaction("2", "c1", LedgerEntryType.DEBIT, 30),
            WalletTransaction("3", "c1", LedgerEntryType.PAYOUT, 20),
            WalletTransaction("4", "c1", LedgerEntryType.CORRECTION, 10),
            WalletTransaction("5", "c1", LedgerEntryType.REVERSAL, 5),
        )
        assertEquals(55L, walletBalance(txs))
    }

    @Test
    fun `time bank handles payout and reversal`() {
        val txs = listOf(
            TimeBankTransaction("1", "c1", LedgerEntryType.CREDIT, 60),
            TimeBankTransaction("2", "c1", LedgerEntryType.PAYOUT, 25),
            TimeBankTransaction("3", "c1", LedgerEntryType.REVERSAL, 10),
        )
        assertEquals(25L, timeBankBalance(txs))
    }

    @Test
    fun `wallet balance with negative amounts stays consistent`() {
        val txs = listOf(
            WalletTransaction("1", "c1", LedgerEntryType.CREDIT, -50),
            WalletTransaction("2", "c1", LedgerEntryType.DEBIT, 10),
        )
        assertEquals(-60L, walletBalance(txs))
    }
}