package com.example.control_tiempo_infantes.data.usage

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object UsageStatsHelper {

    fun hasUsagePermission(ctx: Context): Boolean {
        return try {
            val appOps = android.app.AppOpsManager::class.java
            val mode = (ctx.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager)
                .checkOpNoThrow(
                    "android:get_usage_stats",
                    android.os.Process.myUid(),
                    ctx.packageName
                )
            mode == android.app.AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun openUsageAccessSettings(ctx: Context) {
        ctx.startActivity(
            android.content.Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }


    fun getTodayUsageMillis(ctx: Context): Map<String, Long> {
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val cal = Calendar.getInstance()
        // Inicio del día (00:00)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val start = cal.timeInMillis
        val end = System.currentTimeMillis()

        val stats: List<UsageStats> = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            start,
            end
        ) ?: emptyList()

        val map = mutableMapOf<String, Long>()

        stats.forEach { us ->
            val pkg = us.packageName ?: return@forEach
            val total = us.totalTimeInForeground
            if (total > 0) {
                map[pkg] = (map[pkg] ?: 0L) + total
            }
        }

        return map
    }

    fun todayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(System.currentTimeMillis())
    }
}
