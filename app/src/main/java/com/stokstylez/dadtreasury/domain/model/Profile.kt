package com.stokstylez.dadtreasury.domain.model

/**
 * Household, parent, and child profiles.
 *
 * Per spec §3.3: one child has one parent, one parent can have multiple children.
 */
data class Household(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
)

data class ParentProfile(
    val id: String,
    val householdId: String,
    val displayName: String,
    val deviceId: String,
    val createdAt: Long = System.currentTimeMillis(),
)

data class ChildProfile(
    val id: String,
    val householdId: String,
    val parentId: String,
    val displayName: String,
    val deviceId: String,
    val avatarColor: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Device identity for secure pairing and trust management.
 */
data class DeviceIdentity(
    val deviceId: String,
    val displayName: String,
    val publicKey: String,
    val role: Role,
    val isTrusted: Boolean = false,
    val isRevoked: Boolean = false,
    val pairedAt: Long = System.currentTimeMillis(),
    val revokedAt: Long? = null,
)