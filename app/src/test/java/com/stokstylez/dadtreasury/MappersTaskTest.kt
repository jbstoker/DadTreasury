package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.data.db.TaskEntity
import com.stokstylez.dadtreasury.data.toDomain
import com.stokstylez.dadtreasury.data.toEntity
import com.stokstylez.dadtreasury.domain.model.ApprovalState
import com.stokstylez.dadtreasury.domain.model.RewardType
import com.stokstylez.dadtreasury.domain.model.Task
import com.stokstylez.dadtreasury.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTaskTest {

    @Test
    fun `task entity to domain maps all fields`() {
        val entity = TaskEntity(
            id = "t1",
            title = "Clean room",
            description = "Tidy and vacuum",
            expectedDurationMinutes = 30,
            dueTimestamp = 123456L,
            rewardType = RewardType.PAID.name,
            rewardAmount = 500,
            status = TaskStatus.OPEN.name,
            approvalState = ApprovalState.NOT_SUBMITTED.name,
            notes = "use the vacuum",
            locationRuleId = "loc1",
            checklist = listOf("tidy", "vacuum"),
            completionPhotoUri = "content://photo",
            createdAt = 100L,
            updatedAt = 200L,
        )

        val domain = entity.toDomain()

        assertEquals("t1", domain.id)
        assertEquals("Clean room", domain.title)
        assertEquals("Tidy and vacuum", domain.description)
        assertEquals(30, domain.expectedDurationMinutes)
        assertEquals(123456L, domain.dueTimestamp)
        assertEquals(RewardType.PAID, domain.rewardType)
        assertEquals(500L, domain.rewardAmount)
        assertEquals(TaskStatus.OPEN, domain.status)
        assertEquals(ApprovalState.NOT_SUBMITTED, domain.approvalState)
        assertEquals("use the vacuum", domain.notes)
        assertEquals("loc1", domain.locationRuleId)
        assertEquals(listOf("tidy", "vacuum"), domain.checklist)
        assertEquals("content://photo", domain.completionPhotoUri)
        assertEquals(100L, domain.createdAt)
        assertEquals(200L, domain.updatedAt)
    }

    @Test
    fun `task domain to entity maps all fields`() {
        val domain = Task(
            id = "t1",
            title = "Clean room",
            description = "Tidy",
            expectedDurationMinutes = 15,
            dueTimestamp = null,
            rewardType = RewardType.TIME,
            rewardAmount = 45,
            status = TaskStatus.COMPLETED,
            approvalState = ApprovalState.PENDING,
            notes = "n",
            locationRuleId = null,
            checklist = emptyList(),
            completionPhotoUri = null,
            createdAt = 100L,
            updatedAt = 200L,
        )

        val entity = domain.toEntity()

        assertEquals("t1", entity.id)
        assertEquals("Clean room", entity.title)
        assertEquals("Tidy", entity.description)
        assertEquals(15, entity.expectedDurationMinutes)
        assertNull(entity.dueTimestamp)
        assertEquals(RewardType.TIME.name, entity.rewardType)
        assertEquals(45L, entity.rewardAmount)
        assertEquals(TaskStatus.COMPLETED.name, entity.status)
        assertEquals(ApprovalState.PENDING.name, entity.approvalState)
        assertEquals("n", entity.notes)
        assertNull(entity.locationRuleId)
        assertTrue(entity.checklist.isEmpty())
        assertNull(entity.completionPhotoUri)
        assertEquals(100L, entity.createdAt)
        assertEquals(200L, entity.updatedAt)
    }

    @Test
    fun `task round-trip preserves all fields`() {
        val original = Task(
            id = "t1",
            title = "Walk dog",
            description = "15 min around the block",
            expectedDurationMinutes = 20,
            dueTimestamp = 555L,
            rewardType = RewardType.FREE,
            rewardAmount = 0,
            status = TaskStatus.APPROVED,
            approvalState = ApprovalState.APPROVED,
            notes = "with leash",
            locationRuleId = "g1",
            checklist = listOf("leash", "poop bags"),
            completionPhotoUri = "content://done",
            createdAt = 10L,
            updatedAt = 20L,
        )

        assertEquals(original, original.toEntity().toDomain())
    }
}