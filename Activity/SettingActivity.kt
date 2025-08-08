package com.mahadev.shivahd.live.wallpaper.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mahadev.shivahd.live.wallpaper.Language.LanguageActivity
import com.mahadev.shivahd.live.wallpaper.Language.LocaleHelper
import com.mahadev.shivahd.live.wallpaper.Language.PreferencesHelper11
import com.mahadev.shivahd.live.wallpaper.Other.SharePref
import com.mahadev.shivahd.live.wallpaper.R

class SettingActivity : BaseActivity() {

    private lateinit var imgBack: ImageView
    private lateinit var rlHome: RelativeLayout
//    private lateinit var swAutoChangeWall: Switch
    private lateinit var rlFavorite: RelativeLayout
    private lateinit var rlAuToChange: RelativeLayout
    private lateinit var rlMyDownloads: RelativeLayout
    private lateinit var rlLanguage: RelativeLayout
    private lateinit var rlShareApp: RelativeLayout
    private lateinit var txtAppVersion: TextView
    private lateinit var rlPrivacyPolicy: RelativeLayout
    private lateinit var intent: Intent


    override fun attachBaseContext(newBase: Context) {
        val langCode = PreferencesHelper11(newBase).selectedLanguage ?: "en"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, langCode))
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("wallpaper_prefs", MODE_PRIVATE)
        val urlList = prefs.getString("urls", "")?.split(",")?.filter { it.isNotBlank() } ?: return

        imgBack = findViewById(R.id.btnBack)
        rlHome = findViewById(R.id.rlHome)
//        swAutoChangeWall = findViewById(R.id.swAutoChangeWall)
        rlFavorite = findViewById(R.id.rlFavorite)
        rlMyDownloads = findViewById(R.id.rlMyDownloads)
        rlLanguage = findViewById(R.id.rlLanguage)
        rlShareApp = findViewById(R.id.rlShareApp)
        txtAppVersion = findViewById(R.id.txtAppVersion)
        rlPrivacyPolicy = findViewById(R.id.rlPrivacyPolicy)
        rlAuToChange = findViewById(R.id.rlAuToChange)

        imgBack.setOnClickListener { onBackPressed() }


        val isSwitchOn = SharePref.isSwitch(this)
//        swAutoChangeWall.isChecked = isSwitchOn


//        val isServiceRunning = WallpaperService.isRunning
//        swAutoChangeWall.isChecked = isServiceRunning


//        swAutoChangeWall.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                if (urlList.isEmpty()){
//                    Toast.makeText(this, "Please Add Wallpaper !!", Toast.LENGTH_SHORT).show()
//                    swAutoChangeWall.isChecked = false
//                }else{
//                    WallpaperService.isRunning = true
//                    startService(Intent(this, WallpaperService::class.java))
//                    SharePref.setSwitch(this, true)
//                }
//
//
//            } else {
//                SharePref.setSwitch(this, false)
//                // Stop wallpaper service
//                stopService(Intent(this, WallpaperService::class.java))
//                WallpaperService.isRunning = false
//                Toast.makeText(this, "Auto wallpaper stopped", Toast.LENGTH_SHORT).show()
//            }
//        }

        rlHome.setOnClickListener {

            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        rlAuToChange.setOnClickListener {

            intent = Intent(this, AutoChangerActivity::class.java)
            startActivity(intent)
        }


        rlFavorite.setOnClickListener {
            intent = Intent(this, FavoriteActivity::class.java)
            startActivity(intent)
        }
        rlMyDownloads.setOnClickListener {
            intent = Intent(this, FolderImageActivity::class.java)
            startActivity(intent)
        }

        rlLanguage.setOnClickListener {
            intent = Intent(this, LanguageActivity::class.java)
            startActivity(intent)
        }

        rlShareApp.setOnClickListener {
            SharePref.shareApp(this)
        }

        //App Version
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        txtAppVersion.text = "$versionName"

        // Privacy Policy
        rlPrivacyPolicy.setOnClickListener {
            SharePref.openPrivacy(this)
        }

    }


    override fun onResume() {
        super.onResume()

        val isSwitchOn = SharePref.isSwitch(this)
//        swAutoChangeWall.isChecked = isSwitchOn

        // Restore previous switch state
        // Start service if switch was ON

//        val isServiceRunning = WallpaperService.isRunning
//        swAutoChangeWall.isChecked = isServiceRunning
    }


}