package com.mahadev.shivahd.live.wallpaper.Other

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import java.util.concurrent.TimeUnit


class WallpaperWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
        val urlList = prefs.getString("urls", "")?.split(",")?.filter { it.isNotBlank() } ?: return Result.failure()
        if (urlList.isEmpty()) return Result.failure()

        val interval = prefs.getLong("interval", 30)
        val index = prefs.getInt("index", 0)
        val url = urlList[index % urlList.size]

        try {
            val bitmap = Glide.with(applicationContext)
                .asBitmap()
                .load(url)
                .submit()
                .get()

            val wallpaperManager = WallpaperManager.getInstance(applicationContext)

            // ✅ Only change the home screen wallpaper to avoid Realme popup
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }

            prefs.edit().putInt("index", (index + 1) % urlList.size).apply()

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
        Log.e("ChecKDData", "interval Wark Manager :: ${interval}")
        // 🔁 Reschedule next wallpaper change after 30 seconds
        val request = OneTimeWorkRequestBuilder<WallpaperWorker>()
            .setInitialDelay(interval, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork("wallpaper_auto_change", ExistingWorkPolicy.REPLACE, request)

        return Result.success()
    }
}
