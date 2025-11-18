package com.example.control_tiempo_infantes.features.devices

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import java.util.Calendar

data class AppUsageRaw(
    val packageName: String,
    val totalMinutes: Int
)

fun readTodayUsage(context: Context): List<AppUsageRaw> {
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
        ?: return emptyList()

    val end = System.currentTimeMillis()

    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis

    val stats = usm.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        start,
        end
    )

    if (stats.isNullOrEmpty()) {
        Log.d("UsageReader", "queryUsageStats devolvió lista vacía")
        return emptyList()
    }

    val result = stats
        .filter { it.totalTimeInForeground > 0 }
        .map {
            val mins = (it.totalTimeInForeground / 1000 / 60).toInt()
            AppUsageRaw(
                packageName = it.packageName,
                totalMinutes = mins
            )
        }
        .filter { it.totalMinutes > 0 }

    Log.d("UsageReader", "Apps con uso hoy: ${result.size}")
    return result
}
