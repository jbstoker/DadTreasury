package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.data.db.AppConnectionEntity
import com.stokstylez.dadtreasury.data.db.DeviceIdentityEntity
import com.stokstylez.dadtreasury.data.db.LibraryCategoryEntity
import com.stokstylez.dadtreasury.data.db.LibraryPageEntity
import com.stokstylez.dadtreasury.data.db.SyncEventEntity
import com.stokstylez.dadtreasury.data.toDomain
import com.stokstylez.dadtreasury.data.toEntity
import com.stokstylez.dadtreasury.domain.model.AppConnection
import com.stokstylez.dadtreasury.domain.model.DeviceIdentity
import com.stokstylez.dadtreasury.domain.model.LibraryCategory
import com.stokstylez.dadtreasury.domain.model.LibraryPage
import com.stokstylez.dadtreasury.domain.model.Role
import com.stokstylez.dadtreasury.domain.model.SyncEvent
import com.stokstylez.dadtreasury.domain.model.SyncEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersRemainingTest {

    // ---- Library Category ----

    @Test
    fun `library category entity to domain maps all fields`() {
        val entity = LibraryCategoryEntity(
            id = "c1",
            name = "Chores",
            parentId = null,
            createdAt = 123L,
        )

        val domain = entity.toDomain()

        assertEquals("c1", domain.id)
        assertEquals("Chores", domain.name)
        assertNull(domain.parentId)
        assertEquals(123L, domain.createdAt)
    }

    @Test
    fun `library category domain to entity maps all fields`() {
        val domain = LibraryCategory(
            id = "c2",
            name = "Parenting",
            parentId = "root",
            createdAt = 456L,
        )

        val entity = domain.toEntity()

        assertEquals("c2", entity.id)
        assertEquals("Parenting", entity.name)
        assertEquals("root", entity.parentId)
        assertEquals(456L, entity.createdAt)
    }

    // ---- Library Page ----

    @Test
    fun `library page entity to domain maps all fields`() {
        val entity = LibraryPageEntity(
            id = "p1",
            categoryId = "c1",
            title = "How to budget",
            body = "Start with a ledger.",
            tags = listOf("money", "kid"),
            revision = 3,
            updatedAt = 300L,
            createdAt = 100L,
        )

        val domain = entity.toDomain()

        assertEquals("p1", domain.id)
        assertEquals("c1", domain.categoryId)
        assertEquals("How to budget", domain.title)
        assertEquals("Start with a ledger.", domain.body)
        assertEquals(listOf("money", "kid"), domain.tags)
        assertEquals(3, domain.revision)
        assertEquals(300L, domain.updatedAt)
        assertEquals(100L, domain.createdAt)
    }

    @Test
    fun `library page domain to entity maps all fields`() {
        val domain = LibraryPage(
            id = "p2",
            categoryId = "c2",
            title = "Screen time tips",
            body = "",
            tags = emptyList(),
            revision = 1,
            updatedAt = 400L,
            createdAt = 200L,
        )

        val entity = domain.toEntity()

        assertEquals("p2", entity.id)
        assertEquals("c2", entity.categoryId)
        assertEquals("Screen time tips", entity.title)
        assertEquals("", entity.body)
        assertTrue(entity.tags.isEmpty())
        assertEquals(1, entity.revision)
        assertEquals(400L, entity.updatedAt)
        assertEquals(200L, entity.createdAt)
    }

    // ---- Sync Event ----

    @Test
    fun `sync event entity to domain maps all fields`() {
        val entity = SyncEventEntity(
            id = "s1",
            type = SyncEventType.TASK_CREATED.name,
            senderDeviceId = "devA",
            recipientDeviceId = "devB",
            payload = "{\"task\":\"t1\"}",
            timestamp = 1000L,
            revision = 2,
            signature = "sig123",
            protocolVersion = 1,
        )

        val domain = entity.toDomain()

        assertEquals("s1", domain.id)
        assertEquals(SyncEventType.TASK_CREATED, domain.type)
        assertEquals("devA", domain.senderDeviceId)
        assertEquals("devB", domain.recipientDeviceId)
        assertEquals("{\"task\":\"t1\"}", domain.payload)
        assertEquals(1000L, domain.timestamp)
        assertEquals(2, domain.revision)
        assertEquals("sig123", domain.signature)
        assertEquals(1, domain.protocolVersion)
    }

    @Test
    fun `sync event domain to entity maps all fields`() {
        val domain = SyncEvent(
            id = "s2",
            type = SyncEventType.WALLET_CREDITED,
            senderDeviceId = "devC",
            recipientDeviceId = null,
            payload = "500",
            timestamp = 2000L,
            revision = 1,
            signature = "sig456",
            protocolVersion = 1,
        )

        val entity = domain.toEntity()

        assertEquals("s2", entity.id)
        assertEquals(SyncEventType.WALLET_CREDITED.name, entity.type)
        assertEquals("devC", entity.senderDeviceId)
        assertNull(entity.recipientDeviceId)
        assertEquals("500", entity.payload)
        assertEquals(2000L, entity.timestamp)
        assertEquals(1, entity.revision)
        assertEquals("sig456", entity.signature)
        assertEquals(1, entity.protocolVersion)
    }

    // ---- Device ----

    @Test
    fun `device identity entity to domain maps all fields`() {
        val entity = DeviceIdentityEntity(
            deviceId = "d1",
            displayName = "Dad's Phone",
            publicKey = "pubkey1",
            role = Role.PARENT.name,
            isTrusted = true,
            isRevoked = false,
            pairedAt = 100L,
            revokedAt = null,
        )

        val domain = entity.toDomain()

        assertEquals("d1", domain.deviceId)
        assertEquals("Dad's Phone", domain.displayName)
        assertEquals("pubkey1", domain.publicKey)
        assertEquals(Role.PARENT, domain.role)
        assertTrue(domain.isTrusted)
        assertEquals(false, domain.isRevoked)
        assertEquals(100L, domain.pairedAt)
        assertNull(domain.revokedAt)
    }

    @Test
    fun `device identity domain to entity maps all fields`() {
        val domain = DeviceIdentity(
            deviceId = "d2",
            displayName = "Kid's Tablet",
            publicKey = "pubkey2",
            role = Role.CHILD,
            isTrusted = false,
            isRevoked = true,
            pairedAt = 200L,
            revokedAt = 300L,
        )

        val entity = domain.toEntity()

        assertEquals("d2", entity.deviceId)
        assertEquals("Kid's Tablet", entity.displayName)
        assertEquals("pubkey2", entity.publicKey)
        assertEquals(Role.CHILD.name, entity.role)
        assertEquals(false, entity.isTrusted)
        assertTrue(entity.isRevoked)
        assertEquals(200L, entity.pairedAt)
        assertEquals(300L, entity.revokedAt)
    }

    // ---- AppConnection ----

    @Test
    fun `app connection entity to domain maps all fields`() {
        val entity = AppConnectionEntity(
            id = "a1",
            displayName = "Other Parent",
            pairingCode = "ABC123",
            peerDeviceId = "peer1",
            isTrusted = true,
            isRevoked = false,
            createdAt = 1000L,
            lastSyncAt = 2000L,
        )

        val domain = entity.toDomain()

        assertEquals("a1", domain.id)
        assertEquals("Other Parent", domain.displayName)
        assertEquals("ABC123", domain.pairingCode)
        assertEquals("peer1", domain.peerDeviceId)
        assertTrue(domain.isTrusted)
        assertEquals(false, domain.isRevoked)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.lastSyncAt)
    }

    @Test
    fun `app connection domain to entity maps all fields`() {
        val domain = AppConnection(
            id = "a2",
            displayName = "Test Parent",
            pairingCode = "XYZ789",
            peerDeviceId = "",
            isTrusted = false,
            isRevoked = true,
            createdAt = 3000L,
            lastSyncAt = null,
        )

        val entity = domain.toEntity()

        assertEquals("a2", entity.id)
        assertEquals("Test Parent", entity.displayName)
        assertEquals("XYZ789", entity.pairingCode)
        assertEquals("", entity.peerDeviceId)
        assertEquals(false, entity.isTrusted)
        assertTrue(entity.isRevoked)
        assertEquals(3000L, entity.createdAt)
        assertNull(entity.lastSyncAt)
    }
}