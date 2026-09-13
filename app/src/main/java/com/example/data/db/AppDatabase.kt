package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.OverhaulDao
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        OversightEntity::class,
        OversightItemEntity::class,
        ItemPrerequisiteEntity::class,
        ItemAssignmentEntity::class,
        DailyWorkLogEntity::class,
        PlanningSessionEntity::class,
        SessionNoteEntity::class,
        SessionDecisionEntity::class,
        ProcurementRequestEntity::class,
        AuditLogEntity::class,
        NotificationEntity::class,
        SafetyPermitEntity::class,
        DigitalSignatureEntity::class,
        SyncQueueEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun overhaulDao(): OverhaulDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ۱. افزودن فیلدهای Soft-Delete به جدول آیتم‌های WBS
                db.execSQL("ALTER TABLE oversight_items ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE oversight_items ADD COLUMN deletedAt TEXT DEFAULT NULL")

                // ۲. ایجاد جدول صف همگام‌سازی ابری و آفلاین
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS sync_queue (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entityType TEXT NOT NULL,
                        entityId INTEGER NOT NULL,
                        action TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL,
                        retryCount INTEGER NOT NULL,
                        lastError TEXT,
                        siteId TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_queue_syncStatus ON sync_queue(syncStatus)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_queue_entityType ON sync_queue(entityType)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ghadir_neyriz_overhaul_db"
                )
                    .addMigrations(MIGRATION_8_9)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
