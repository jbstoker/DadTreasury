# Tasks

Tasks are the heart of RetroNest. A parent creates a task, the child completes it, and the parent approves it — which creates the reward.

## Lifecycle

```
📋 Open → ✅ Completed → ✓ Approved (reward given)
                ↓
            ✗ Rejected
```

## Parent: Creating a Task

1. Open **Tasks** tab
2. Tap **+** (top right)
3. Fill in:
   - **Title** (required)
   - **Description**
   - **Expected duration** in minutes
   - **Reward type**: Free / Paid / Time
   - **Reward amount** (cents for Paid, minutes for Time)
4. Tap **Create**

## Child: Completing a Task

1. Open the task
2. Read the description and checklist
3. When done, tap **📷 Take photo & finish**
4. The camera opens — take a photo of the result
5. The task is now **awaiting approval**

> 💡 The photo is optional, but it helps the parent verify the work.

## Parent: Approving / Rejecting

1. Open a task with status **Awaiting approval**
2. View the completion photo
3. Tap **✓ Approve** — reward is created automatically
4. Or tap **✗ Reject** — task returns to rejected state

## Rewards

| Type | Where it goes |
|------|---------------|
| 💰 Paid | Wallet (in cents, shown as €) |
| ⏰ Time | Time Bank (in minutes) |
| 🌿 Free | Nothing |

Approved rewards appear immediately in the Wallet / Time Bank tab.

## Task Status Colors

- **Open** — cyan
- **Awaiting approval** — amber
- **Approved ✓** — green
- **Rejected** — red
- **Cancelled** — gray