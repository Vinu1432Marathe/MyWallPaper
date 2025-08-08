package com.mahadev.shivahd.live.wallpaper.Activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.magic.hd.wallpapers.remote.RetrofitClient
import com.mahadev.shivahd.live.wallpaper.Adapter.WallpaperAdapter
import com.mahadev.shivahd.live.wallpaper.Language.LocaleHelper
import com.mahadev.shivahd.live.wallpaper.Language.PreferencesHelper11
import com.mahadev.shivahd.live.wallpaper.Model.ModelWallpaper
import com.mahadev.shivahd.live.wallpaper.Model.WallpaperModel
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity.Companion.categoryId
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity.Companion.categoryIdSpecial
import com.mahadev.shivahd.live.wallpaper.Other.PrefsManager
import com.mahadev.shivahd.live.wallpaper.Other.Splash
import com.mahadev.shivahd.live.wallpaper.Other.WallpaperVMFactory
import com.mahadev.shivahd.live.wallpaper.Other.WallpaperViewModel
import com.mahadev.shivahd.live.wallpaper.R

class ImageListActivity : BaseActivity() {

    private lateinit var linearProgressIndicator: LinearProgressIndicator
    private lateinit var adapter: WallpaperAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var imgBack: ImageView

    private lateinit var favoriteList: MutableList<ModelWallpaper>

    lateinit var comefrom: String
    private var isLoadingMore = false

    private val viewModel: WallpaperViewModel by viewModels {
        WallpaperVMFactory(RetrofitClient.create(this/*, Splash.BaseURL*/))

    }

    override fun attachBaseContext(newBase: Context) {
        val langCode = PreferencesHelper11(newBase).selectedLanguage ?: "en"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, langCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        linearProgressIndicator = findViewById(R.id.linearProgressIndicator)
        imgBack = findViewById(R.id.imgBack)
        recyclerView = findViewById(R.id.rclImageList)
        favoriteList = PrefsManager.getFavoriteList(this)

        imgBack.setOnClickListener { onBackPressed() }

        comefrom = intent.getStringExtra("comefrom").toString()

        adapter = WallpaperAdapter(this, mutableListOf())
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter

        val wallpapers =
            intent.getParcelableArrayListExtra<ModelWallpaper>("wallpapers") ?: emptyList()
        if (comefrom.equals("Special")) {
            adapter.updateList(wallpapers)

            viewModel.getLoadingLiveData(categoryIdSpecial).observe(this) { isLoading ->
                if (isLoading) {
                    // Show loader
                    linearProgressIndicator.visibility = View.VISIBLE
                } else {
                    // Hide loader
                    linearProgressIndicator.visibility = View.GONE
                }
            }
        } else {

            adapter.updateList(wallpapers)
            viewModel.getLoadingLiveData(categoryId).observe(this) { isLoading ->
                if (isLoading) {
                    // Show loader
                    linearProgressIndicator.visibility = View.VISIBLE
                } else {
                    // Hide loader
                    linearProgressIndicator.visibility = View.GONE
                }
            }
        }


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                        viewModel.loadMoreWallpapers(this@ImageListActivity, NewSplashActivity.categoryId)
                        viewModel.loadMoreWallpapers(this@ImageListActivity, NewSplashActivity.categoryIdSpecial)
                    }
                }
            }
        })

        // Initial Load
        viewModel.loadMoreWallpapers(this, NewSplashActivity.categoryId)
        viewModel.loadMoreWallpapers(this, NewSplashActivity.categoryIdSpecial)
    }

    override fun onResume() {
        super.onResume()
        // Refresh favorites
        favoriteList = PrefsManager.getFavoriteList(this)

    }
}