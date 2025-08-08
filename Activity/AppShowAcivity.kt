package com.mahadev.shivahd.live.wallpaper.Activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.addsdemo.mysdk.ADPrefrences.MyApp
import com.addsdemo.mysdk.ADPrefrences.NativeAds_Class
import com.addsdemo.mysdk.utils.UtilsClass
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.mahadev.shivahd.live.wallpaper.Adapter.SlideViewPagerAdapter
import com.mahadev.shivahd.live.wallpaper.Language.LocaleHelper
import com.mahadev.shivahd.live.wallpaper.Model.Model_slide
import com.mahadev.shivahd.live.wallpaper.Other.NotificationPermissionHelper
import com.mahadev.shivahd.live.wallpaper.Other.PreferencesHelper11
import com.mahadev.shivahd.live.wallpaper.Other.SharePref
import com.mahadev.shivahd.live.wallpaper.R

class AppShowAcivity : BaseActivity() {

    lateinit var viewPager: ViewPager2

    lateinit var txtDone: TextView
    lateinit var imgNext: ImageView
    lateinit var indicatorLayout: LinearLayout
    lateinit var container: LinearLayout

    private lateinit var remoteConfig: FirebaseRemoteConfig
    override fun attachBaseContext(newBase: Context) {
        val langCode = PreferencesHelper11(newBase).selectedLanguage ?: "en"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, langCode))
    }


    val lstSlide = mutableListOf<Model_slide?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_app_show_acivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewPager = findViewById(R.id.viewPager)
        txtDone = findViewById(R.id.txtDone)
//
        imgNext = findViewById(R.id.imgNext)
        indicatorLayout = findViewById(R.id.indicatorLayout)

        txtDone.setOnClickListener {
            SharePref.setOnboarding(this, true)
            navigateNext()
        }

        lstSlide.add(
            Model_slide(
                R.drawable.show1,
                getString(R.string.divine_ultra_hd_wallpapers),
                "",
                0
            )
        )
        lstSlide.add(
            Model_slide(
                R.drawable.show2,
                getString(R.string.feel_lord_shiva_s_power),
                "",
                0
            )
        )
        lstSlide.add(
            Model_slide(
                R.drawable.show3,
                getString(R.string.feel_lord_shiva_s_power),
                "",
                0
            )
        )


        viewPager.adapter = SlideViewPagerAdapter(lstSlide as List<Model_slide>)

        setupIndicators()
        setCurrentIndicator(0)
//        TabLayoutMediator(tabDots, viewPager) { _, _ -> }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setCurrentIndicator(position)
            }
        })

        imgNext.setOnClickListener {
            val current = viewPager.currentItem
            if (current < lstSlide.size - 1) {
                viewPager.setCurrentItem(current + 1, true)

            } else {
                // Navigate to next activity
                navigateNext()
            }
        }

//
//        container = findViewById(R.id.container)
//        container.isVisible =
//            MyApp.ad_preferences.getRemoteConfig()?.isAdShow == true
//
//        val llline_full = findViewById<LinearLayout>(com.addsdemo.mysdk.R.id.llline_full)
//        val llnative_full = findViewById<LinearLayout>(com.addsdemo.mysdk.R.id.llnative_full)
//        NativeAds_Class.Fix_NativeFull_Show(this, llnative_full, llline_full, "medium")
//



    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(lstSlide.size)
        for (i in indicators.indices) {
            indicators[i] = ImageView(this)
            val layoutParams = LinearLayout.LayoutParams(
                if (i == 0) 24.dpToPx() else 12.dpToPx(),
                if (i == 0) 8.dpToPx() else 12.dpToPx()
            )
            layoutParams.setMargins(8, 0, 8, 0)
            indicators[i]!!.layoutParams = layoutParams
            indicators[i]!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == 0) R.drawable.active_dot else R.drawable.inactive_dot
                )
            )
            indicatorLayout.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = indicatorLayout.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorLayout.getChildAt(i) as ImageView
            val layoutParams = LinearLayout.LayoutParams(
                if (i == index) 15.dpToPx() else 8.dpToPx(),
                if (i == index) 6.dpToPx() else 8.dpToPx()
            )
            layoutParams.setMargins(8, 0, 8, 0)
            imageView.layoutParams = layoutParams

            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == index) R.drawable.active_dot else R.drawable.inactive_dot
                )
            )

            imageView.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
        }
    }

    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun navigateNext() {
        if (!NotificationPermissionHelper.isNotificationPermissionGranted(this)) {
            NotificationPermissionHelper.requestNotificationPermission(this)
        } else {
            goToMain()

        }
    }

    private fun goToMain() {
        SharePref.setOnboarding(this, true)
        val intent = Intent(this, MainActivity::class.java)
        UtilsClass.startSpecialActivity(this, intent, false);
        finish() // Optional: if you want to finish the current activity
    }

    // ✅ Handle result from permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NotificationPermissionHelper.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goToMain()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Please allow notification permission from settings to proceed.")
                    .setPositiveButton("Go to Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

}