package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.domain.model.LocalTransport
import com.stokstylez.dadtreasury.domain.model.SyncEvent
import com.stokstylez.dadtreasury.domain.model.SyncEventType
import com.stokstylez.dadtreasury.domain.model.SyncQueueItem
import com.stokstylez.dadtreasury.domain.model.SyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncTest {

    @Test
    fun `local transport is available`() {
        val transport = LocalTransport()
        assertTrue(transport.isAvailable)
        assertEquals("local", transport.name)
    }

    @Test
    fun `local transport send and receive round-trips event`() = runBlocking {
        val transport = LocalTransport()
        val event = SyncEvent(
            id = "e1",
            type = SyncEventType.TASK_CREATED,
            senderDeviceId = "dev1",
            recipientDeviceId = null,
            payload = "test",
        )

        assertTrue(transport.sendEvent(event))
        val received = transport.receiveEvents()

        assertEquals(1, received.size)
        assertEquals("e1", received[0].id)
        assertEquals(SyncEventType.TASK_CREATED, received[0].type)
        assertEquals("dev1", received[0].senderDeviceId)
    }

    @Test
    fun `local transport receive clears inbox`() = runBlocking {
        val transport = LocalTransport()
        transport.sendEvent(SyncEvent("e1", SyncEventType.WALLET_CREDITED, "d1", null, "100"))

        transport.receiveEvents()
        val second = transport.receiveEvents()

        assertTrue(second.isEmpty())
    }

    @Test
    fun `sync queue item defaults to pending`() {
        val event = SyncEvent("e1", SyncEventType.CHAT_MESSAGE_SENT, "d1", null, "hello")
        val item = SyncQueueItem(id = "q1", event = event)

        assertEquals(SyncStatus.PENDING, item.status)
        assertEquals(0, item.retryCount)
    }

    @Test
    fun `sync queue item custom status preserved`() {
        val event = SyncEvent("e2", SyncEventType.TASK_APPROVED, "d1", null, "data")
        val item = SyncQueueItem(
            id = "q2",
            event = event,
            status = SyncStatus.RETRYING,
            retryCount = 3,
            lastAttemptAt = 999L,
        )

        assertEquals(SyncStatus.RETRYING, item.status)
        assertEquals(3, item.retryCount)
        assertEquals(999L, item.lastAttemptAt)
    }

    @Test
    fun `sync event defaults to revision 1 and protocol 1`() {
        val event = SyncEvent(
            id = "e3",
            type = SyncEventType.GEO_RULE_CREATED,
            senderDeviceId = "d1",
            recipientDeviceId = null,
            payload = "",
        )

        assertEquals(1, event.revision)
        assertEquals(1, event.protocolVersion)
        assertFalse(event.timestamp == 0L)
    }
}