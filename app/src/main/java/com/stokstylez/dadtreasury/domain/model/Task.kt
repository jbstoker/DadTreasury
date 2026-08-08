package com.stokstylez.dadtreasury.domain.model

/**
 * A household task created by the parent, completed by the child, and approved by the parent.
 *
 * Per spec §7: each task contains ID, title, description, expected duration,
 * due date/time slot, reward type, reward amount, status, approval state,
 * optional notes, optional location rule, optional checklist.
 */
data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val expectedDurationMinutes: Int = 0,
    val dueTimestamp: Long? = null,
    val rewardType: RewardType = RewardType.FREE,
    val rewardAmount: Long = 0, // cents for PAID, minutes for TIME
    val status: TaskStatus = TaskStatus.OPEN,
    val approvalState: ApprovalState = ApprovalState.NOT_SUBMITTED,
    val notes: String = "",
    val locationRuleId: String? = null,
    val checklist: List<String> = emptyList(),
    val completionPhotoUri: String? = null, // child's photo of completed task result
    val childId: String? = null, // which child the task is assigned to (null = any/default)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class RewardType {
    FREE,   // no reward, just a task
    PAID,   // wallet reward
    TIME    // time-bank reward
}

enum class TaskStatus {
    OPEN,          // visible to child
    COMPLETED,     // child marked complete, awaiting approval
    APPROVED,      // parent approved, reward credited
    REJECTED,      // parent rejected completion
    CANCELLED
}

enum class ApprovalState {
    NOT_SUBMITTED,
    PENDING,
    APPROVED,
    REJECTED
}