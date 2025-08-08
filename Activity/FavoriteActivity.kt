package com.mahadev.shivahd.live.wallpaper.Activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mahadev.shivahd.live.wallpaper.Adapter.FavWallpaperAdapter
import com.mahadev.shivahd.live.wallpaper.Language.LocaleHelper
import com.mahadev.shivahd.live.wallpaper.Language.PreferencesHelper11
import com.mahadev.shivahd.live.wallpaper.Model.ModelWallpaper
import com.mahadev.shivahd.live.wallpaper.Other.PrefsManager
import com.mahadev.shivahd.live.wallpaper.R

class FavoriteActivity : BaseActivity() {

    private lateinit var   recyclerView: RecyclerView
    private lateinit var rlNoData: RelativeLayout
    private lateinit var imgBack: ImageView
    private lateinit var adapter: FavWallpaperAdapter
    private lateinit var favoriteList: List<ModelWallpaper>

    override fun attachBaseContext(newBase: Context) {
        val langCode = PreferencesHelper11(newBase).selectedLanguage ?: "en"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, langCode))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imgBack = findViewById(R.id.imgBack)
        rlNoData = findViewById(R.id.rlNoData)

        imgBack.setOnClickListener { onBackPressed() }


         recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFavorites)
        favoriteList = PrefsManager.getFavoriteList(this)

        if (favoriteList.isEmpty()){
            rlNoData.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }else{
            rlNoData.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter = FavWallpaperAdapter(this, favoriteList)
//        recyclerView.adapter = adapter

            val layoutManager = GridLayoutManager(this, 2)
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if ((adapter.getItemViewType(position) == adapter.TYPE_NORMAL)) 1 else 2
                }
            }
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter

        }


    }

    override fun onResume() {
        super.onResume()


        favoriteList = PrefsManager.getFavoriteList(this)

        if (favoriteList.isEmpty()){
            rlNoData.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }else{
            rlNoData.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateList(favoriteList)
        }

    }

}