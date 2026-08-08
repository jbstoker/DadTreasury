package com.stokstylez.dadtreasury.data.db

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString("\u0001")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split("\u0001")
}

@Database(
    entities = [
        TaskEntity::class,
        WalletTransactionEntity::class,
        TimeBankTransactionEntity::class,
        ChatMessageEntity::class,
        ChatThreadEntity::class,
        CalendarEventEntity::class,
        GeoRuleEntity::class,
        LibraryCategoryEntity::class,
        LibraryPageEntity::class,
        LibraryRevisionEntity::class,
        SyncEventEntity::class,
        SyncQueueEntity::class,
        DeviceIdentityEntity::class,
        HouseholdEntity::class,
        ParentEntity::class,
        ChildEntity::class,
        AppConnectionEntity::class,
        SharedLibraryPageEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun walletDao(): WalletDao
    abstract fun timeBankDao(): TimeBankDao
    abstract fun chatDao(): ChatDao
    abstract fun calendarDao(): CalendarDao
    abstract fun geoRuleDao(): GeoRuleDao
    abstract fun libraryDao(): LibraryDao
    abstract fun syncDao(): SyncDao
    abstract fun deviceDao(): DeviceDao
    abstract fun householdDao(): HouseholdDao
    abstract fun appConnectionDao(): AppConnectionDao
    abstract fun sharedLibraryPageDao(): SharedLibraryPageDao

    companion object {
        private const val DB_PASSPHRASE_KEY = "db_passphrase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                // Get or create a stable 32-char passphrase stored in EncryptedSharedPreferences
                val passphrase = getOrCreateDbPassphrase(context)
                val factory = SupportFactory(passphrase.toByteArray())

                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dad_treasury.db",
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }

        /**
         * Get or create a stable random 32-char passphrase stored in EncryptedSharedPreferences.
         * This keeps the DB encryption key secure on-device.
         */
        private fun getOrCreateDbPassphrase(context: Context): String {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs: SharedPreferences = EncryptedSharedPreferences.create(
                context,
                "dad_treasury_db_secure",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            prefs.getString(DB_PASSPHRASE_KEY, null)?.let { return it }

            // Generate a new random 32-char passphrase (alphanumeric)
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            val random = SecureRandom()
            val passphrase = StringBuilder(32)
            repeat(32) {
                passphrase.append(chars[random.nextInt(chars.length)])
            }
            val value = passphrase.toString()
            prefs.edit().putString(DB_PASSPHRASE_KEY, value).apply()
            return value
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE tasks ADD COLUMN completionPhotoUri TEXT DEFAULT NULL"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN childId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE geo_rules ADD COLUMN targetRole TEXT NOT NULL DEFAULT 'CHILD'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `app_connections` (" +
                        "`id` TEXT NOT NULL, " +
                        "`displayName` TEXT NOT NULL, " +
                        "`pairingCode` TEXT NOT NULL, " +
                        "`peerDeviceId` TEXT NOT NULL, " +
                        "`isTrusted` INTEGER NOT NULL, " +
                        "`isRevoked` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`lastSyncAt` INTEGER, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `shared_library_pages` (" +
                        "`id` TEXT NOT NULL, " +
                        "`connectionId` TEXT NOT NULL, " +
                        "`origin` TEXT NOT NULL, " +
                        "`pageId` TEXT NOT NULL, " +
                        "`lastSyncedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
            }
        }
    }
}
