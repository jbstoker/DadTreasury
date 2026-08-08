package com.stokstylez.dadtreasury.domain.model

/**
 * Wallet and time-bank ledger entries.
 *
 * Per spec §6: never store a single mutable balance - store transaction entries and
 * compute the balance from the ledger.
 */
data class WalletTransaction(
    val id: String,
    val childId: String,
    val type: LedgerEntryType,
    val amountCents: Long,
    val note: String = "",
    val taskId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val syncEventId: String? = null,
)

data class TimeBankTransaction(
    val id: String,
    val childId: String,
    val type: LedgerEntryType,
    val amountMinutes: Long,
    val note: String = "",
    val taskId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val syncEventId: String? = null,
)

enum class LedgerEntryType {
    CREDIT,     // reward credited
    DEBIT,      // reward debited / spent
    PAYOUT,     // wallet payout / time payout
    CORRECTION, // manual correction
    REVERSAL    // undo a previous entry
}

/**
 * Compute wallet balance from a list of transactions (pure function, testable).
 */
fun walletBalance(transactions: List<WalletTransaction>): Long =
    transactions.fold(0L) { acc, t ->
        when (t.type) {
            LedgerEntryType.CREDIT -> acc + t.amountCents
            LedgerEntryType.DEBIT -> acc - t.amountCents
            LedgerEntryType.PAYOUT -> acc - t.amountCents
            LedgerEntryType.CORRECTION -> acc + t.amountCents
            LedgerEntryType.REVERSAL -> acc - t.amountCents
        }
    }

/**
 * Compute time-bank balance from a list of transactions (pure function, testable).
 */
fun timeBankBalance(transactions: List<TimeBankTransaction>): Long =
    transactions.fold(0L) { acc, t ->
        when (t.type) {
            LedgerEntryType.CREDIT -> acc + t.amountMinutes
            LedgerEntryType.DEBIT -> acc - t.amountMinutes
            LedgerEntryType.PAYOUT -> acc - t.amountMinutes
            LedgerEntryType.CORRECTION -> acc + t.amountMinutes
            LedgerEntryType.REVERSAL -> acc - t.amountMinutes
        }
    }