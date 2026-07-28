package com.tradingsim.app.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Capture une région précise de la fenêtre (le graphique en chandeliers) au moment
 * de la clôture d'un trade, et l'enregistre en PNG dans le stockage interne de l'app
 * (aucune permission requise, non accessible aux autres apps).
 */
object ChartSnapshotCapturer {

    suspend fun captureWindowRegion(activity: Activity, bounds: Rect): Bitmap? {
        if (bounds.width() <= 0 || bounds.height() <= 0) return null
        val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)

        return suspendCoroutine { continuation ->
            try {
                PixelCopy.request(
                    activity.window,
                    bounds,
                    bitmap,
                    { result ->
                        continuation.resume(if (result == PixelCopy.SUCCESS) bitmap else null)
                    },
                    Handler(Looper.getMainLooper())
                )
            } catch (_: Exception) {
                continuation.resume(null)
            }
        }
    }

    fun saveToFile(filesDir: File, bitmap: Bitmap): String? {
        return try {
            val dir = File(filesDir, "trade_snapshots").apply { mkdirs() }
            val file = File(dir, "trade_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 90, out) }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
