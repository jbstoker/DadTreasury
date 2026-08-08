package com.stokstylez.dadtreasury.domain.model

/**
 * A secure peer-to-peer connection between two parent apps.
 *
 * Allows parents to share the wiki/library and message each other,
 * per the "connect parent apps" requirement.
 */
data class AppConnection(
    val id: String,
    val displayName: String,
    val pairingCode: String,
    val peerDeviceId: String,
    val isTrusted: Boolean = false,
    val isRevoked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncAt: Long? = null,
)

/** A wiki/library page origin tracking marker for shared pages. */
enum class PageOrigin {
    LOCAL,
    REMOTE,
}