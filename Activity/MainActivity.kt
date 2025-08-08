package com.mahadev.shivahd.live.wallpaper.Activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.addsdemo.mysdk.ADPrefrences.MyApp
import com.addsdemo.mysdk.ADPrefrences.NativeAds_Class
import com.addsdemo.mysdk.utils.UtilsClass
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.magic.hd.wallpapers.remote.RetrofitClient
import com.mahadev.shivahd.live.wallpaper.Adapter.HoriWallpaperAdapter
import com.mahadev.shivahd.live.wallpaper.Adapter.MainWallpaperAdapter
import com.mahadev.shivahd.live.wallpaper.Language.LocaleHelper
import com.mahadev.shivahd.live.wallpaper.Language.PreferencesHelper11
import com.mahadev.shivahd.live.wallpaper.Model.ModelWallpaper
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity.Companion.categoryId
import com.mahadev.shivahd.live.wallpaper.Other.NewSplashActivity.Companion.categoryIdSpecial
import com.mahadev.shivahd.live.wallpaper.Other.PrefsManager
import com.mahadev.shivahd.live.wallpaper.Other.WallpaperVMFactory
import com.mahadev.shivahd.live.wallpaper.Other.WallpaperViewModel
import com.mahadev.shivahd.live.wallpaper.R

class MainActivity : AppCompatActivity() {

    //todo for App Update......
    private lateinit var appUpdateManager: AppUpdateManager
    private val REQUEST_UPDATE = 123


    private lateinit var adapter: MainWallpaperAdapter
    private lateinit var adapter2: HoriWallpaperAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewHori: RecyclerView
    private lateinit var txtSeeSpecial: TextView
    private lateinit var txtSeeTop: TextView
    private lateinit var imgMenu: ImageView
    private lateinit var container: LinearLayout

    private lateinit var shimmerContainer: ShimmerFrameLayout
    private lateinit var shimmerContainer11: ShimmerFrameLayout

    var lstWallSpecial: MutableList<ModelWallpaper> = mutableListOf()
    var lstWallTop: MutableList<ModelWallpaper> = mutableListOf()
    private lateinit var favoriteList: MutableList<ModelWallpaper>

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
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerViewHori = findViewById(R.id.rclHoriZontal)
        txtSeeSpecial = findViewById(R.id.txtSeeSpecial)
        txtSeeTop = findViewById(R.id.txtSeeTop)
        imgMenu = findViewById(R.id.imgMenu)

        shimmerContainer = findViewById(R.id.shimmerContainer)
        shimmerContainer11 = findViewById(R.id.shimmerContainer11)


        // todo App Update
        checkForForceUpdate()


        // todo Ads Code
        container = findViewById(R.id.container)
        container.isVisible =
            MyApp.ad_preferences.getRemoteConfig()?.isAdShow == true
        val llline_full = findViewById<LinearLayout>(com.addsdemo.mysdk.R.id.llline_full)
        val llnative_full = findViewById<LinearLayout>(com.addsdemo.mysdk.R.id.llnative_full)
        NativeAds_Class.NativeFull_Show(this, llnative_full, llline_full, "medium")

        //todo Click
        imgMenu.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        txtSeeSpecial.setOnClickListener {

            Log.d("lstWall", "lstWallSpecial :: ${lstWallSpecial}")
            val intent = Intent(this, ImageListActivity::class.java)
            intent.putExtra("comefrom", "Special")
            intent.putParcelableArrayListExtra(
                "wallpapers",
                ArrayList(lstWallSpecial)
            )
            UtilsClass.startSpecialActivity(this, intent, false);
        }
        txtSeeTop.setOnClickListener {

            Log.d("lstWall", "lstWallTop :: ${lstWallTop}")
            val intent = Intent(this, ImageListActivity::class.java)
            intent.putParcelableArrayListExtra(
                "wallpapers",
                ArrayList(lstWallTop)
            )
            intent.putExtra("comefrom", "Tops")
            UtilsClass.startSpecialActivity(this, intent, false);
        }

        favoriteList = PrefsManager.getFavoriteList(this)


        //todo Horizontal Recyclerview
        adapter2 = HoriWallpaperAdapter(this, emptyList())
        recyclerViewHori.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewHori.adapter = adapter2

        //todo Vertical Recyclerview
        adapter = MainWallpaperAdapter(this, mutableListOf())
        val layoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter


        // todo get data from ViewModel
        viewModel.getWallpapersLiveData(NewSplashActivity.categoryId).observe(this) {
            recyclerView.visibility = View.VISIBLE
            lstWallTop.addAll(it)
            adapter.updateList(it)
            isLoadingMore = false

        }

        viewModel.getLoadingLiveData(categoryId).observe(this) { loading ->
            shimmerContainer.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.getLoadingLiveData(categoryIdSpecial).observe(this) { loading ->

            shimmerContainer11.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.getWallpapersLiveData(NewSplashActivity.categoryIdSpecial).observe(this) {
            recyclerViewHori.visibility = View.VISIBLE
            lstWallSpecial.addAll(it)
            adapter2.updateList(it)

            Log.d("xyz", "calll22 :: ${it.size}")
        }

// todo get data from ViewModel for Pagination
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
                        viewModel.loadMoreWallpapers(
                            this@MainActivity,
                            NewSplashActivity.categoryId
                        )
                    }
                }
            }
        })

        // Initial Load
        viewModel.loadMoreWallpapers(this, NewSplashActivity.categoryId)
        viewModel.loadMoreWallpapers(this, NewSplashActivity.categoryIdSpecial)
    }


    override fun onBackPressed() {

        showCongratulationsDialog()
    }


    override fun onStart() {
        super.onStart()
        favoriteList = PrefsManager.getFavoriteList(this)
    }

    private fun showCongratulationsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_exit)
        dialog.setCancelable(false)

        val mbtn_no = dialog.findViewById<TextView>(R.id.btnCancel)
        val mbtn_yes = dialog.findViewById<TextView>(R.id.btnExit)

        mbtn_yes.setOnClickListener { finishAffinity() }

        mbtn_no.setOnClickListener { dialog.dismiss() }
        val window = dialog.window
        val wlp = window!!.attributes

        wlp.gravity = Gravity.CENTER
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window.attributes = wlp
        dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.show()

    }


    // todo App Update......


    override fun onResume() {
        super.onResume()
        favoriteList = PrefsManager.getFavoriteList(this)

    }

    private fun checkForForceUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Start the update
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_UPDATE
                )
            }
        }
    }

    // If update is canceled or fails
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_UPDATE) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "Update is required to continue", Toast.LENGTH_LONG).show()
                finish() // Optional: force exit app if update is not accepted
            }
        }
    }


}