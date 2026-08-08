package com.stokstylez.dadtreasury.familysync

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Google Family Link screen-time adapter.
 *
 * Per spec §17: any Google Family integration must:
 *  1. require validated internet
 *  2. be blocked when offline
 *  3. queue pending actions locally
 *  4. retry when internet returns
 *
 * Note: Google Family Link does not expose a public API for third-party apps
 * to directly modify daily screen-time limits. The closest legitimate path is:
 *   - When a TIME-reward task is approved, Dad's Treasury credits the
 *     local time bank AND queues a Google Family Link screen-time grant
 *     so the parent can approve the extra time in one tap.
 *   - The request is queued locally until validated internet is available,
 *     then retried automatically.
 */
object GoogleFamilyLinkManager {

    data class ScreenTimeGrant(
        val childId: String,
        val minutes: Long,
        val taskTitle: String,
        val timestamp: Long = System.currentTimeMillis(),
        var status: GrantStatus = GrantStatus.QUEUED,
    )

    enum class GrantStatus {
        QUEUED,
        PROCESSING,
        GRANTED,
        FAILED,
    }

    private val queue = mutableListOf<ScreenTimeGrant>()

    fun pendingGrants(): List<ScreenTimeGrant> = queue.filter { it.status == GrantStatus.QUEUED }

    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Queue a screen-time grant for the child's Google Family Link account.
     * If validated internet is available AND Family Link is installed, opens
     * Family Link so the parent can approve the extra time immediately.
     */
    suspend fun grantScreenTime(
        context: Context,
        childId: String,
        minutes: Long,
        taskTitle: String,
    ) {
        val grant = ScreenTimeGrant(
            childId = childId,
            minutes = minutes,
            taskTitle = taskTitle,
            status = if (isInternetAvailable(context)) GrantStatus.PROCESSING else GrantStatus.QUEUED,
        )
        queue.add(grant)

        if (grant.status == GrantStatus.PROCESSING && !pushToFamilyLink(context)) {
            grant.status = GrantStatus.QUEUED
        } else if (grant.status == GrantStatus.PROCESSING) {
            grant.status = GrantStatus.GRANTED
        }
    }

    /**
     * Attempt to open Google Family Link so the parent can grant the extra
     * screen time. If Family Link is not installed, returns false and the
     * grant stays queued for retry.
     */
    private fun pushToFamilyLink(context: Context): Boolean {
        val familyLinkPackage = "com.google.android.apps.familylink"
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(familyLinkPackage)
        if (intent == null) {
            return false
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    /** Retry all queued grants when connectivity returns. */
    suspend fun retryQueuedGrants(context: Context) {
        if (!isInternetAvailable(context)) return
        queue.forEach { grant ->
            if (grant.status == GrantStatus.QUEUED && pushToFamilyLink(context)) {
                grant.status = GrantStatus.GRANTED
            }
        }
    }
}