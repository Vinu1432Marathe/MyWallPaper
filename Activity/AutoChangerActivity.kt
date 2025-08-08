package com.mahadev.shivahd.live.wallpaper.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.magic.hd.wallpapers.remote.RetrofitClient
import com.mahadev.shivahd.live.wallpaper.Adapter.SetWallpaperAdapter
import com.mahadev.shivahd.live.wallpaper.Language.LocaleHelper
import com.mahadev.shivahd.live.wallpaper.Language.PreferencesHelper11
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity
import com.mahadev.shivahd.live.wallpaper.Other.WallpaperVMFactory
import com.mahadev.shivahd.live.wallpaper.Other.WallpaperViewModel
import com.mahadev.shivahd.live.wallpaper.Other.WallpaperWorker
import com.mahadev.shivahd.live.wallpaper.R

class AutoChangerActivity : BaseActivity() {

    private lateinit var llMenu: LinearLayout
    private lateinit var txtSetTime: TextView
    private lateinit var btnApply: TextView
    private lateinit var btnBack: ImageView
    private lateinit var rvWallpaper: RecyclerView
    private lateinit var intervalSpinner: Spinner


    private lateinit var adapter: SetWallpaperAdapter

    // todo checkkkkk
    private val selectedUrls = mutableListOf<String>()

    private var isLoadingMore = false

    private val viewModel: WallpaperViewModel by viewModels {
        WallpaperVMFactory(RetrofitClient.create(this/*, Splash.BaseURL*/))

    }

    override fun attachBaseContext(newBase: Context) {
        val langCode = PreferencesHelper11(newBase).selectedLanguage ?: "en"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, langCode))
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_changer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnBack = findViewById(R.id.btnBack)
        llMenu = findViewById(R.id.llMenu)
        btnApply = findViewById(R.id.btnApply)
        rvWallpaper = findViewById(R.id.rvWallpaper)


        btnBack.setOnClickListener { onBackPressed() }
        intervalSpinner = findViewById(R.id.intervalSpinner)

        val intervals = arrayOf(
            "15 min",
            "30 min",
            "1 hour",
            "2 hours",
            "4 hours",
            "6 hours",
            "12 hours",
            "Daily"
        )

        val adapter1 = ArrayAdapter(
            this,
            R.layout.spinner_item,  // custom text color
            intervals
        )
        adapter1.setDropDownViewResource(R.layout.spinner_dropdown_item)
        intervalSpinner.adapter = adapter1


        llMenu.setOnClickListener {
            intervalSpinner.performClick()
        }


        adapter = SetWallpaperAdapter(this, mutableListOf(), selectedUrls) {}
        rvWallpaper.layoutManager = GridLayoutManager(this, 3)
        rvWallpaper.adapter = adapter

        viewModel.getWallpapersLiveData(NewSplashActivity.categoryId).observe(this) {
            adapter.updateList(it)
            isLoadingMore = false
        }

        rvWallpaper.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoadingMore) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                    ) {
                        Log.d("xyz", "calll")
                        isLoadingMore = true
                        viewModel.loadMoreWallpapers(
                            this@AutoChangerActivity,
                            NewSplashActivity.categoryId
                        )
                    }
                }
            }
        })

        // Initial Load
        viewModel.loadMoreWallpapers(this, NewSplashActivity.categoryId)

        btnApply.setOnClickListener {

            Log.e("ChecKDData", "Data :: ${selectedUrls}")

            if (selectedUrls.isEmpty()) {
                Toast.makeText(this, "Select at least one image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val interval = when (intervalSpinner.selectedItemPosition) {
                0 -> 15
                1 -> 30// 15 minutes
                2 -> 60 // 30 minutes
                3 -> 120 // 1 hour
                4 -> 240 // 2 hours
                5 -> 360 // 4 hours
                6 -> 720 // 6 hours
                7 -> 1440 // 12 hours
                else -> 15

            }

            val prefs = getSharedPreferences("wallpaper_prefs", MODE_PRIVATE)
            prefs.edit()
                .putString("urls", selectedUrls.joinToString(","))
                .putLong("interval", interval.toLong()) // 30 sec interval
                .putInt("index", 0)
                .apply()

//            startService(Intent(this, WallpaperService::class.java))
            startWallpaperChangeWorker()

            Toast.makeText(this, "Wallpaper Service Started", Toast.LENGTH_SHORT).show()

            selectedUrls.clear()

        }


    }

    private fun startWallpaperChangeWorker() {
        val request = OneTimeWorkRequestBuilder<WallpaperWorker>().build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork("wallpaper_auto_change", ExistingWorkPolicy.REPLACE, request)
    }

}