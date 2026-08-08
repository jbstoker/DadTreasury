package com.stokstylez.dadtreasury.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Secure PIN lock manager.
 *
 * Uses EncryptedSharedPreferences (AndroidX Security Crypto) to store the PIN's
 * PBKDF2-SHA256 hash, never the PIN itself. No plaintext secrets stored.
 */
class PinLockManager(context: Context) {

    private val prefs: SharedPreferences = createEncryptedPrefs(context)

    private object Keys {
        const val PIN_HASH = "pin_hash"
        const val PIN_SALT = "pin_salt"
        const val PIN_ENABLED = "pin_enabled"
        const val LOCK_TIMEOUT_MS = "lock_timeout_ms"
        const val LAST_LOCK_AT = "last_locked_at"
    }

    val isPinEnabled: Boolean
        get() = prefs.getBoolean(Keys.PIN_ENABLED, false)

    val lockTimeoutMs: Long
        get() = prefs.getLong(Keys.LOCK_TIMEOUT_MS, 0L)

    /**
     * Set a new PIN. Stores only the PBKDF2-SHA256 hash with a random salt.
     * If a PIN already exists, [currentPin] must match it.
     */
    fun setPin(currentPin: String?, newPin: String): Boolean {
        // Require current PIN when changing, allow when not set
        if (isPinEnabled) {
            if (currentPin == null || !verifyPin(currentPin)) return false
        }
        require(newPin.length >= 4) { "PIN must be at least 4 digits" }
        require(newPin.all { it.isDigit() }) { "PIN must contain only digits" }

        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(newPin, salt)

        prefs.edit()
            .putString(Keys.PIN_SALT, android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP))
            .putString(Keys.PIN_HASH, android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP))
            .putBoolean(Keys.PIN_ENABLED, true)
            .apply()
        return true
    }

    /**
     * Verify a PIN attempt against the stored hash.
     */
    fun verifyPin(pin: String): Boolean {
        val saltB64 = prefs.getString(Keys.PIN_SALT, null) ?: return false
        val hashB64 = prefs.getString(Keys.PIN_HASH, null) ?: return false

        val salt = android.util.Base64.decode(saltB64, android.util.Base64.NO_WRAP)
        val expected = android.util.Base64.decode(hashB64, android.util.Base64.NO_WRAP)
        val actual = hashPin(pin, salt)

        // Constant-time comparison to reduce timing attacks
        return MessageDigest.isEqual(expected, actual)
    }

    /**
     * Disable PIN lock.
     */
    fun disablePin(currentPin: String?): Boolean {
        if (!isPinEnabled) return true
        if (currentPin == null || !verifyPin(currentPin)) return false
        prefs.edit().remove(Keys.PIN_HASH).remove(Keys.PIN_SALT).putBoolean(Keys.PIN_ENABLED, false).apply()
        return true
    }

    /**
     * Set auto-lock timeout. 0 = always lock on app open.
     */
    fun setLockTimeout(timeoutMs: Long) {
        prefs.edit().putLong(Keys.LOCK_TIMEOUT_MS, timeoutMs).apply()
    }

    fun recordLockTimestamp() {
        prefs.edit().putLong(Keys.LAST_LOCK_AT, System.currentTimeMillis()).apply()
    }

    fun shouldLockOnOpen(): Boolean {
        if (!isPinEnabled) return false
        val timeout = lockTimeoutMs
        if (timeout <= 0L) return true // always lock
        val lastLock = prefs.getLong(Keys.LAST_LOCK_AT, System.currentTimeMillis())
        return System.currentTimeMillis() - lastLock >= timeout
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        // PBKDF2-HMAC-SHA256 with 100,000 iterations
        val spec = javax.crypto.spec.PBEKeySpec(pin.toCharArray(), salt, 100_000, 256)
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "dad_treasury_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }
}