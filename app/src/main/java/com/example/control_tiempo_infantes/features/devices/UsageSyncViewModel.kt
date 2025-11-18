package com.example.control_tiempo_infantes.features.devices

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class UsageSyncViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    fun syncTodayUsage(
        ctx: Context,
        childId: String,
        deviceId: String
    ) {
        viewModelScope.launch {
            try {
                // 1. Comprobar permiso de uso
                if (!hasUsageAccess(ctx)) {
                    // No lanzamos settings aquí para mantener el VM limpio.
                    // Solo no guardamos nada si no hay permiso.
                    return@launch
                }

                val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

                val now = System.currentTimeMillis()
                val dayStart = getTodayStartMillis()
                val dateStr = formatDate(now)

                val stats = usm.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    dayStart,
                    now
                )

                if (stats.isNullOrEmpty()) return@launch

                // 2. Agrupar minutos por paquete
                val minutesPerPackage = mutableMapOf<String, Long>()

                stats.forEach { s ->
                    val minutes = s.totalTimeInForeground / 1000L / 60L
                    if (minutes > 0) {
                        minutesPerPackage[s.packageName] =
                            (minutesPerPackage[s.packageName] ?: 0L) + minutes
                    }
                }

                if (minutesPerPackage.isEmpty()) return@launch

                // 3. Guardar en Firestore en batch (colección device_usage)
                val col = db.collection("device_usage")
                val batch = db.batch()

                minutesPerPackage.forEach { (pkg, mins) ->
                    val doc = col.document()
                    val data = hashMapOf(
                        "id" to doc.id,
                        "childId" to childId,
                        "deviceId" to deviceId,
                        "appPackage" to pkg,
                        "appName" to pkg, // luego puedes resolver nombres bonitos si quieres
                        "date" to dateStr,
                        "totalMinutes" to mins.toInt(),
                        "lastUpdatedAt" to now
                    )
                    batch.set(doc, data)
                }

                batch.commit().await()
            } catch (_: Exception) {
                // En producción puedes loggear el error
            }
        }
    }

    private fun getTodayStartMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    private fun hasUsageAccess(ctx: Context): Boolean {
        val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(),
                ctx.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                "android:get_usage_stats",
                android.os.Process.myUid(),
                ctx.packageName
            )
        }
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return Settings.Secure.getInt(
                ctx.contentResolver,
                "usage_access",
                0
            ) == 1
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
