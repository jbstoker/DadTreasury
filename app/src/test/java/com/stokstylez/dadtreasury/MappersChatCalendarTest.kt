package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.data.db.CalendarEventEntity
import com.stokstylez.dadtreasury.data.db.ChatMessageEntity
import com.stokstylez.dadtreasury.data.db.ChatThreadEntity
import com.stokstylez.dadtreasury.data.db.GeoRuleEntity
import com.stokstylez.dadtreasury.data.toDomain
import com.stokstylez.dadtreasury.data.toEntity
import com.stokstylez.dadtreasury.domain.model.CalendarEvent
import com.stokstylez.dadtreasury.domain.model.ChatMessage
import com.stokstylez.dadtreasury.domain.model.ChatThread
import com.stokstylez.dadtreasury.domain.model.GeoRule
import com.stokstylez.dadtreasury.domain.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersChatCalendarTest {

    // ---- Chat Message ----

    @Test
    fun `chat message entity to domain maps all fields`() {
        val entity = ChatMessageEntity(
            id = "m1",
            threadId = "th1",
            senderRole = Role.PARENT.name,
            text = "Hi!",
            timestamp = 123L,
            syncEventId = "sx",
            isDelivered = true,
            isRead = false,
        )

        val domain = entity.toDomain()

        assertEquals("m1", domain.id)
        assertEquals("th1", domain.threadId)
        assertEquals(Role.PARENT, domain.senderRole)
        assertEquals("Hi!", domain.text)
        assertEquals(123L, domain.timestamp)
        assertEquals("sx", domain.syncEventId)
        assertTrue(domain.isDelivered)
        assertEquals(false, domain.isRead)
    }

    @Test
    fun `chat message domain to entity maps all fields`() {
        val domain = ChatMessage(
            id = "m2",
            threadId = "th2",
            senderRole = Role.CHILD,
            text = "Done!",
            timestamp = 456L,
            syncEventId = null,
            isDelivered = false,
            isRead = true,
        )

        val entity = domain.toEntity()

        assertEquals("m2", entity.id)
        assertEquals("th2", entity.threadId)
        assertEquals(Role.CHILD.name, entity.senderRole)
        assertEquals("Done!", entity.text)
        assertEquals(456L, entity.timestamp)
        assertNull(entity.syncEventId)
        assertEquals(false, entity.isDelivered)
        assertTrue(entity.isRead)
    }

    // ---- Chat Thread ----

    @Test
    fun `chat thread entity to domain maps all fields`() {
        val entity = ChatThreadEntity(
            id = "th1",
            parentId = "p1",
            childId = "c1",
            lastMessageAt = 1000L,
            unreadCount = 3,
        )

        val domain = entity.toDomain()

        assertEquals("th1", domain.id)
        assertEquals("p1", domain.parentId)
        assertEquals("c1", domain.childId)
        assertEquals(1000L, domain.lastMessageAt)
        assertEquals(3, domain.unreadCount)
    }

    @Test
    fun `chat thread domain to entity maps all fields`() {
        val domain = ChatThread(
            id = "th2",
            parentId = "p2",
            childId = "c2",
            lastMessageAt = 2000L,
            unreadCount = 0,
        )

        val entity = domain.toEntity()

        assertEquals("th2", entity.id)
        assertEquals("p2", entity.parentId)
        assertEquals("c2", entity.childId)
        assertEquals(2000L, entity.lastMessageAt)
        assertEquals(0, entity.unreadCount)
    }

    // ---- Calendar ----

    @Test
    fun `calendar event entity to domain maps all fields`() {
        val entity = CalendarEventEntity(
            id = "e1",
            title = "Soccer",
            description = "Team practice",
            startTimestamp = 1000L,
            endTimestamp = 2000L,
            isRecurring = true,
            recurrenceRule = "FREQ=WEEKLY",
            reminderMinutes = 30,
            routineId = "r1",
            createdAt = 500L,
        )

        val domain = entity.toDomain()

        assertEquals("e1", domain.id)
        assertEquals("Soccer", domain.title)
        assertEquals("Team practice", domain.description)
        assertEquals(1000L, domain.startTimestamp)
        assertEquals(2000L, domain.endTimestamp)
        assertTrue(domain.isRecurring)
        assertEquals("FREQ=WEEKLY", domain.recurrenceRule)
        assertEquals(30, domain.reminderMinutes)
        assertEquals("r1", domain.routineId)
        assertEquals(500L, domain.createdAt)
    }

    @Test
    fun `calendar event domain to entity maps all fields`() {
        val domain = CalendarEvent(
            id = "e2",
            title = "Dentist",
            description = "",
            startTimestamp = 3000L,
            endTimestamp = 4000L,
            isRecurring = false,
            recurrenceRule = null,
            reminderMinutes = null,
            routineId = null,
            createdAt = 600L,
        )

        val entity = domain.toEntity()

        assertEquals("e2", entity.id)
        assertEquals("Dentist", entity.title)
        assertEquals("", entity.description)
        assertEquals(3000L, entity.startTimestamp)
        assertEquals(4000L, entity.endTimestamp)
        assertEquals(false, entity.isRecurring)
        assertNull(entity.recurrenceRule)
        assertNull(entity.reminderMinutes)
        assertNull(entity.routineId)
        assertEquals(600L, entity.createdAt)
    }

    // ---- GeoRule ----

    @Test
    fun `geo rule entity to domain maps all fields`() {
        val entity = GeoRuleEntity(
            id = "g1",
            title = "Home",
            message = "You arrived home",
            latitude = 52.1,
            longitude = 4.9,
            radiusMeters = 100,
            activeStartHour = 8,
            activeEndHour = 20,
            repeatDaily = true,
            isEnabled = true,
            taskId = "t1",
            createdAt = 999L,
        )

        val domain = entity.toDomain()

        assertEquals("g1", domain.id)
        assertEquals("Home", domain.title)
        assertEquals("You arrived home", domain.message)
        assertEquals(52.1, domain.latitude, 0.0)
        assertEquals(4.9, domain.longitude, 0.0)
        assertEquals(100, domain.radiusMeters)
        assertEquals(8, domain.activeStartHour)
        assertEquals(20, domain.activeEndHour)
        assertTrue(domain.repeatDaily)
        assertTrue(domain.isEnabled)
        assertEquals("t1", domain.taskId)
        assertEquals(999L, domain.createdAt)
    }

    @Test
    fun `geo rule domain to entity maps all fields`() {
        val domain = GeoRule(
            id = "g2",
            title = "School",
            message = "Pick up time",
            latitude = 51.5,
            longitude = -0.1,
            radiusMeters = 200,
            activeStartHour = 15,
            activeEndHour = 17,
            repeatDaily = false,
            isEnabled = false,
            taskId = null,
            createdAt = 111L,
        )

        val entity = domain.toEntity()

        assertEquals("g2", entity.id)
        assertEquals("School", entity.title)
        assertEquals("Pick up time", entity.message)
        assertEquals(51.5, entity.latitude, 0.0)
        assertEquals(-0.1, entity.longitude, 0.0)
        assertEquals(200, entity.radiusMeters)
        assertEquals(15, entity.activeStartHour)
        assertEquals(17, entity.activeEndHour)
        assertEquals(false, entity.repeatDaily)
        assertEquals(false, entity.isEnabled)
        assertNull(entity.taskId)
        assertEquals(111L, entity.createdAt)
    }
}