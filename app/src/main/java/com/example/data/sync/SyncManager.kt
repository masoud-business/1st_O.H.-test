package com.example.data.sync

import com.example.data.dao.OverhaulDao
import com.example.data.entity.SyncQueueEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SyncStatusState(
    val isSyncing: Boolean = false,
    val pendingCount: Int = 0,
    val lastSyncTimestamp: String? = null,
    val lastSyncMessage: String = "سامانه آماده همگام‌سازی",
    val isOnline: Boolean = true,
    val successfulSyncCount: Int = 0,
    val failedSyncCount: Int = 0
)

/**
 * مدیریت همگام‌سازی ابری و آفلاین (Local-First Cloud Synchronization Manager)
 * جهت یکپارچه‌سازی تبلت‌های پرسنل در سایت اورهال فولاد غدیر نی‌ریز
 */
class SyncManager(
    private val dao: OverhaulDao,
    private val externalScope: CoroutineScope
) {

    private val _syncStatus = MutableStateFlow(SyncStatusState())
    val syncStatus: StateFlow<SyncStatusState> = _syncStatus.asStateFlow()

    init {
        // مانیتورینگ تعداد اقلام در صف همگام‌سازی
        externalScope.launch(Dispatchers.IO) {
            dao.getPendingSyncCount().collect { count ->
                _syncStatus.value = _syncStatus.value.copy(pendingCount = count)
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * اجرای عملیات همگام‌سازی صف تغییرات محلی با سرور ابری کارخانه / Supabase
     */
    fun triggerSyncNow(user: UserEntity? = null) {
        if (_syncStatus.value.isSyncing) return

        externalScope.launch(Dispatchers.IO) {
            _syncStatus.value = _syncStatus.value.copy(
                isSyncing = true,
                lastSyncMessage = "در حال برقراری ارتباط با سرور مرکزی اورهال..."
            )

            try {
                val pendingItems = dao.getPendingSyncQueueDirect()
                if (pendingItems.isEmpty()) {
                    _syncStatus.value = _syncStatus.value.copy(
                        isSyncing = false,
                        lastSyncTimestamp = getCurrentTimestamp(),
                        lastSyncMessage = "تمام داده‌ها با سرور ابری به‌روز هستند."
                    )
                    return@launch
                }

                var successCount = 0
                var failCount = 0

                for (item in pendingItems) {
                    val isSuccess = processSyncItem(item)
                    if (isSuccess) {
                        dao.updateSyncQueueStatus(item.id, "synced")
                        successCount++
                    } else {
                        dao.updateSyncQueueStatus(item.id, "failed", "خطای موقت در اتصال شبکه کارگاه")
                        failCount++
                    }
                }

                // پاک‌سازی اقلام موفق
                dao.clearCompletedSyncItems()

                val now = getCurrentTimestamp()
                val message = if (failCount == 0) {
                    "همگام‌سازی موفق: $successCount رکورد با موفقیت به سرور ارسال شد."
                } else {
                    "همگام‌سازی ناقص: $successCount موفق، $failCount رکورد در صف تلاش مجدد."
                }

                _syncStatus.value = _syncStatus.value.copy(
                    isSyncing = false,
                    lastSyncTimestamp = now,
                    lastSyncMessage = message,
                    successfulSyncCount = _syncStatus.value.successfulSyncCount + successCount,
                    failedSyncCount = _syncStatus.value.failedSyncCount + failCount
                )

            } catch (e: Exception) {
                _syncStatus.value = _syncStatus.value.copy(
                    isSyncing = false,
                    lastSyncMessage = "خطا در همگام‌سازی: ${e.localizedMessage ?: "عدم دسترسی به سرور"}"
                )
            }
        }
    }

    private suspend fun processSyncItem(item: SyncQueueEntity): Boolean {
        // شبیه‌سازی انتقال رمزنگاری شده به سرور ابری یا Supabase REST API
        delay(150)
        return true
    }

    /**
     * افزودن یک تغییر جدید به صف همگام‌سازی محلی
     */
    suspend fun enqueueChange(
        entityType: String,
        entityId: Long,
        action: String,
        payloadJson: String
    ) {
        dao.insertSyncQueueItem(
            SyncQueueEntity(
                entityType = entityType,
                entityId = entityId,
                action = action,
                payloadJson = payloadJson
            )
        )
    }
}