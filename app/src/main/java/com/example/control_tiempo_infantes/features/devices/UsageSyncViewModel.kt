package com.example.control_tiempo_infantes.features.devices

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
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

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun syncTodayUsage(
        ctx: Context,
        childId: String,
        deviceId: String,
        model: String
    ) {
        viewModelScope.launch {
            try {
                val today = dateFormat.format(Date())
                val usageList = readTodayUsage(ctx)

                Log.d("UsageSync", "syncTodayUsage: ${usageList.size} apps encontradas")

                if (usageList.isEmpty()) {
                    Log.d("UsageSync", "No hay uso hoy, no se sube nada")
                    return@launch
                }

                val col = db.collection("device_usage")

                // 1) Eliminar registros anteriores de HOY para este niño + dispositivo
                val existingSnap = col
                    .whereEqualTo("childId", childId)
                    .whereEqualTo("deviceId", deviceId)
                    .whereEqualTo("date", today)
                    .get()
                    .await()

                val batch = db.batch()

                existingSnap.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                // 2) Insertar registros nuevos de hoy
                usageList.forEach { appUsage ->
                    val doc = col.document()
                    val data = hashMapOf(
                        "id" to doc.id,
                        "childId" to childId,
                        "deviceId" to deviceId,
                        "model" to model,
                        "appPackage" to appUsage.packageName,
                        "appName" to resolveAppName(ctx, appUsage.packageName),
                        "date" to today,
                        "totalMinutes" to appUsage.totalMinutes,
                        "lastUpdatedAt" to System.currentTimeMillis()
                    )
                    batch.set(doc, data)
                }

                batch.commit().await()
                Log.d("UsageSync", "Se subió uso de ${usageList.size} apps a Firestore (reemplazando los datos de hoy)")

            } catch (e: Exception) {
                Log.e("UsageSync", "Error al sincronizar uso", e)
            }
        }
    }

    private fun resolveAppName(ctx: Context, pkg: String): String {
        return try {
            val pm: PackageManager = ctx.packageManager
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkg
        }
    }
}
