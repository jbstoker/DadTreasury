package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.domain.model.AppConnection
import com.stokstylez.dadtreasury.domain.model.ApprovalState
import com.stokstylez.dadtreasury.domain.model.ChatMessage
import com.stokstylez.dadtreasury.domain.model.ChatThread
import com.stokstylez.dadtreasury.domain.model.ChildProfile
import com.stokstylez.dadtreasury.domain.model.DeviceIdentity
import com.stokstylez.dadtreasury.domain.model.GeoRule
import com.stokstylez.dadtreasury.domain.model.Household
import com.stokstylez.dadtreasury.domain.model.LibraryCategory
import com.stokstylez.dadtreasury.domain.model.LibraryPage
import com.stokstylez.dadtreasury.domain.model.LibraryRevision
import com.stokstylez.dadtreasury.domain.model.ParentProfile
import com.stokstylez.dadtreasury.domain.model.RewardType
import com.stokstylez.dadtreasury.domain.model.Role
import com.stokstylez.dadtreasury.domain.model.Task
import com.stokstylez.dadtreasury.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelDefaultsTest {

    @Test
    fun `task defaults are sensible`() {
        val task = Task(id = "t1", title = "Chore")
        assertEquals("", task.description)
        assertEquals(0, task.expectedDurationMinutes)
        assertNull(task.dueTimestamp)
        assertEquals(RewardType.FREE, task.rewardType)
        assertEquals(0L, task.rewardAmount)
        assertEquals(TaskStatus.OPEN, task.status)
        assertEquals(ApprovalState.NOT_SUBMITTED, task.approvalState)
        assertEquals("", task.notes)
        assertNull(task.locationRuleId)
        assertTrue(task.checklist.isEmpty())
        assertNull(task.completionPhotoUri)
    }

    @Test
    fun `geo rule defaults`() {
        val rule = GeoRule(id = "g1", title = "Home", message = "Hi", latitude = 1.0, longitude = 2.0)
        assertEquals(100, rule.radiusMeters)
        assertEquals(0, rule.activeStartHour)
        assertEquals(24, rule.activeEndHour)
        assertTrue(rule.repeatDaily)
        assertTrue(rule.isEnabled)
        assertNull(rule.taskId)
    }

    @Test
    fun `app connection defaults`() {
        val conn = AppConnection(id = "a1", displayName = "Test", pairingCode = "ABC", peerDeviceId = "peer")
        assertFalse(conn.isTrusted)
        assertFalse(conn.isRevoked)
        assertNull(conn.lastSyncAt)
    }

    @Test
    fun `device identity defaults`() {
        val device = DeviceIdentity(deviceId = "d1", displayName = "Phone", publicKey = "key", role = Role.PARENT)
        assertFalse(device.isTrusted)
        assertFalse(device.isRevoked)
        assertNull(device.revokedAt)
    }

    @Test
    fun `chat message defaults`() {
        val msg = ChatMessage(id = "m1", threadId = "t1", senderRole = Role.PARENT, text = "hi")
        assertFalse(msg.isDelivered)
        assertFalse(msg.isRead)
        assertNull(msg.syncEventId)
    }

    @Test
    fun `chat thread defaults`() {
        val thread = ChatThread(id = "th1", parentId = "p1", childId = "c1")
        assertEquals(0, thread.unreadCount)
    }

    @Test
    fun `household defaults`() {
        val h = Household(id = "h1", name = "My Family")
        assertTrue(h.createdAt > 0)
    }

    @Test
    fun `child profile defaults avatar color to zero`() {
        val child = ChildProfile(id = "c1", householdId = "h1", parentId = "p1", displayName = "Kid", deviceId = "d1")
        assertEquals(0, child.avatarColor)
    }

    @Test
    fun `parent profile defaults`() {
        val parent = ParentProfile(id = "p1", householdId = "h1", displayName = "Dad", deviceId = "d1")
        assertTrue(parent.createdAt > 0)
    }

    @Test
    fun `library category default parent is null`() {
        val cat = LibraryCategory(id = "c1", name = "Chores")
        assertNull(cat.parentId)
    }

    @Test
    fun `library page defaults`() {
        val page = LibraryPage(id = "p1", categoryId = "c1", title = "T")
        assertEquals("", page.body)
        assertTrue(page.tags.isEmpty())
        assertEquals(1, page.revision)
    }

    @Test
    fun `library revision defaults`() {
        val rev = LibraryRevision(id = "r1", pageId = "p1", revision = 1, body = "b")
        assertEquals("", rev.note)
    }
}