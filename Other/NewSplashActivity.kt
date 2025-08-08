package com.mahadev.shivahd.live.wallpaper.Other

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.addsdemo.mysdk.ADPrefrences.Ads_Interstitial
import com.addsdemo.mysdk.ADPrefrences.AppOpenAdManager
import com.addsdemo.mysdk.ADPrefrences.MyApp
import com.addsdemo.mysdk.ADPrefrences.NativeAds_Class
import com.addsdemo.mysdk.model.RemoteConfigModel
import com.addsdemo.mysdk.retrofit.InstallerID
import com.addsdemo.mysdk.retrofit.MyReferrer
import com.addsdemo.mysdk.utils.CustomTabLinkOpen
import com.addsdemo.mysdk.utils.UtilsClass
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.RenderMode
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.magic.hd.wallpapers.remote.ApiService
import com.mahadev.shivahd.live.wallpaper.Activity.AppShowAcivity
import com.mahadev.shivahd.live.wallpaper.Activity.MainActivity
import com.mahadev.shivahd.live.wallpaper.Language.LanguageActivity
import com.mahadev.shivahd.live.wallpaper.R
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import java.util.concurrent.Executors

class NewSplashActivity : AppCompatActivity() {

    private var apiService: ApiService? = null
    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig
    private var remoteConfigModel: RemoteConfigModel? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var lottieAnimationView: LottieAnimationView

    companion object {
        var BaseUrl: String? = null
        var ImgUri: String? = null
        var strToken: String? = null
        var categoryIdSpecial: Int = 0
        var categoryId: Int = 0
        const val ONESIGNAL_APP_ID = "f95387a8-0a67-4317-83a8-0b6a44f7e107"

        fun checkConnection(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lottieAnimationView = findViewById(R.id.lottieView);
//        lottieAnimationView.playAnimation();
        preloadAndPlayLottie();
        mediaPlayer = MediaPlayer.create(this, R.raw.shivsong);
        mediaPlayer?.setLooping(true);
        mediaPlayer?.start()
        initViews()
    }


    private fun initViews() {
        if (checkConnection(this)) {
            // Enable verbose logging for debugging (remove in production)
            OneSignal.Debug.logLevel = LogLevel.VERBOSE
            // Initialize with your OneSignal App ID
            OneSignal.initWithContext(this, ONESIGNAL_APP_ID)
            OneSignal.Notifications.addClickListener(object : INotificationClickListener {
                override fun onClick(event: INotificationClickEvent) {
                    val data = event.notification.additionalData
                    val actionType = data?.optString("action_type")
                    val url = data?.optString("url")

                    if (actionType == "open_url" && !url.isNullOrEmpty()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        this@NewSplashActivity.startActivity(intent) // ✅ using Activity context
                    } else {
                        Log.d("OneSignal", "No actionable data in notification")
                    }
                }
            })


            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(5)
                .build()
            mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)

            InstallerID().callInstallerID(this)
            fetchAndSetRemoteConfig()
        } else {
            checkConnectivity()
        }
    }

    private fun checkConnectivity() {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = manager.activeNetworkInfo

        if (activeNetwork == null) {
            val dialogBuilder = AlertDialog.Builder(this)

            dialogBuilder.setMessage("Make sure that WI-FI or mobile data is turned on, then try again")
                .setCancelable(false)
                .setPositiveButton("Retry") { _, _ ->
                    recreate()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    finish()
                }

            val alert = dialogBuilder.create()
            alert.setTitle("No Internet Connection..")
            alert.setIcon(R.drawable.s_logo)
            alert.show()
        }
    }

    private fun fetchAndSetRemoteConfig() {
        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    Log.e("InstallerID", "InstallerID ${InstallerID.referrerUrl}")

                    remoteConfigModel = if (
                        InstallerID.referrerUrl != null &&
                        (InstallerID.referrerUrl.contains("organic", true) ||
                                InstallerID.referrerUrl.contains("not%20set", true))
                    ) {
                        Gson().fromJson(
                            mFirebaseRemoteConfig.getString("app_orgdata"),
                            RemoteConfigModel::class.java
                        ).apply {  }.also {
                            Log.e("InstallerID", "InstallerID app_orgdata")
                        }
                    } else {
                        Gson().fromJson(
                            mFirebaseRemoteConfig.getString("app_data"),
                            RemoteConfigModel::class.java
                        ).apply {  }.also {
                            Log.e("InstallerID", "InstallerID app_data")
                        }
                    }

                    Log.d("TAG", "fetchAndSetRemoteConfig: 11 $remoteConfigModel")

                    val versionCode = try {
                        packageManager.getPackageInfo(packageName, 0).versionName ?: "0"
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                        "0"
                    }

                    if (versionCode == remoteConfigModel?.versionName) {
                        remoteConfigModel!!.isAdShow = false
                        remoteConfigModel!!.isOnAdRedirect = false
                        remoteConfigModel!!.isOnboardingAdShow = false
                    }

                    val fbSdk = remoteConfigModel?.facebookSDK
                    if (!fbSdk?.appId.isNullOrEmpty() && !fbSdk.clientToken.isNullOrEmpty()) {
                        setApplication(fbSdk.appId!!, fbSdk.clientToken!!)
                    }

                    MyReferrer.GetCountryDetails(
                        this,
                        remoteConfigModel,
                        object : MyReferrer.ApiIpCallback {
                            override fun onSuccess(response: RemoteConfigModel) {
                                MyApp.ad_preferences.saveRemoteConfig(response)
                                MyApp.ad_preferences.saveIsAppopenShow(response.isResumeShow)

                                preloadAdsIfEnabled()

                                MyApp.getInstance().appOpenAdManager.showAdIfAvailable(
                                    this@NewSplashActivity,
                                    object : AppOpenAdManager.MyAdCallBack {
                                        override fun onAdClose(value: Boolean) {
                                            if (!isFinishing) {
                                                fetchRemoteConfig() // wallpaper data fetch and intent
                                            }
                                        }
                                    },
                                    response.isOpenShow,
                                    response.isAdShow
                                )
                            }

                            override fun onFailure(t: Throwable) {
                                MyApp.ad_preferences.saveRemoteConfig(remoteConfigModel)
                                remoteConfigModel?.let { MyApp.ad_preferences.saveIsAppopenShow(it.isResumeShow) }

                                Log.d(
                                    "TAG123456789",
                                    "fetchAndSetRemoteConfig:22 $remoteConfigModel"
                                )

                                preloadAdsIfEnabled()

                                MyApp.getInstance().appOpenAdManager.showAdIfAvailable(
                                    this@NewSplashActivity,
                                    object : AppOpenAdManager.MyAdCallBack {
                                        override fun onAdClose(value: Boolean) {
                                            if (!isFinishing) {
                                                fetchRemoteConfig() // wallpaper data fetch and intent

                                                if (!value &&
                                                    remoteConfigModel?.openAdType == "Redirect" &&
                                                    remoteConfigModel!!.isOpenShow &&
                                                    remoteConfigModel!!.isAdShow
                                                ) {
                                                    CustomTabLinkOpen.openLink(
                                                        this@NewSplashActivity,
                                                        UtilsClass.getRandomRedirectLink(
                                                            MyApp.ad_preferences.getRemoteConfig()!!.customLinks!!.openRedirectLink
                                                        ),
                                                        "appOpen_click"
                                                    )
                                                } else if (value && remoteConfigModel?.isOnAdRedirect == true) {
                                                    CustomTabLinkOpen.openLink(
                                                        this@NewSplashActivity,
                                                        UtilsClass.getRandomRedirectLink(
                                                            MyApp.ad_preferences.getRemoteConfig()!!.customLinks!!.openRedirectLink
                                                        ),
                                                        "appOpen_click"
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    remoteConfigModel!!.isOpenShow,
                                    remoteConfigModel!!.isAdShow
                                )
                            }
                        })
                } else {
                    val jsonFallback = """{\n" +
                                "  \"PackageName\": \"com.mahadev.shivahd.live.wallpaper\",\n" +
                                "  \"isAdShow\": true,\n" +
                                "  \"isOnAdRedirect\": false,\n" +
                                "  \"AdsType\": \"Custom\",\n" +
                                "  \"secondAdType\": \"Custom\",\n" +
                                "  \"isSecondAd\": false,\n" +
                                "  \"AdsLoadType\": \"Preload\",\n" +
                                "  \"FailAdsType\": \"Admob\",\n" +
                                "  \"NativeAdsType\": \"Custom\",\n" +
                                "  \"NativeLoadType\": \"Preload\",\n" +
                                "  \"BannerAdsType\": \"Custom\",\n" +
                                "  \"AdsClick\": \"1\",\n" +
                                "  \"BackClick\": \"0\",\n" +
                                "  \"NativeByPage\": \"1\",\n" +
                                "  \"isCloseShow\": false,\n" +
                                "  \"isOpenShow\": true,\n" +
                                "  \"isInterShow\": true,\n" +
                                "  \"isNativeShow\": true,\n" +
                                "  \"isBannerShow\": true,\n" +
                                "  \"splashAdType\": \"Open\",\n" +
                                "  \"customAdsType\": \"Redirect\",\n" +
                                "  \"openAdType\": \"Layout\",\n" +
                                "  \"customBannerAdType\": \"Image\",\n" +
                                "  \"isOnboardingShow\": true,\n" +
                                "  \"isOnboardingAdShow\": false,\n" +
                                "  \"isOnboardingAlways\": false,\n" +
                                "  \"medium\": \"organic\",\n" +
                                "  \"isOrganicOnboarding\": false,\n" +
                                "  \"OrganiccustomAdsType\": \"Layout\",\n" +
                                "  \"OrganicAdsClick\": \"3\",\n" +
                                "  \"OrgaincopenAdType\": \"Layout\",\n" +
                                "  \"OrganicResumeAdType\": \"Layout\",\n" +
                                "  \"OrganicBackClick\": \"3\",\n" +
                                "  \"privacyPolicy\": \"https://phonestylelauncher.blogspot.com/2024/10/privacy-policy.html\",\n" +
                                "  \"termsOfService\": \"https://phonestylelauncher.blogspot.com/2024/10/privacy-policy.html\",\n" +
                                "  \"versionName\": \"0\",\n" +
                                "  \"feedbackMail\": \"semicoloneclipse02@gmail.com\",\n" +
                                "  \"installApiCount\": \"https://dashboardapi.uniqcrafts.com/user/\",\n" +
                                "  \"isExtraScreenShow\": false,\n" +
                                "  \"isResumeShow\": true,\n" +
                                "  \"ResumeAdType\": \"Layout\",\n" +
                                "  \"isCountryScreen\": false,\n" +
                                "  \"isGetStartedScreen\": false,\n" +
                                "  \"moreApps\": \"\",\n" +
                                "  \"admobIds\": {\n" +
                                "    \"openAdIds\": [\n" +
                                "      \"ca-app-pub-3940256099942544/9257395921\"\n" +
                                "    ],\n" +
                                "    \"interAdIds\": [\n" +
                                "      \"ca-app-pub-3940256099942544/1033173712\"\n" +
                                "    ],\n" +
                                "    \"nativeAdIds\": [\n" +
                                "      \"ca-app-pub-3940256099942544/2247696110\"\n" +
                                "    ],\n" +
                                "    \"bannerAdIds\": [\n" +
                                "      \"ca-app-pub-3940256099942544/9214589741\"\n" +
                                "    ]\n" +
                                "  },\n" +
                                "  \"adxIds\": {\n" +
                                "    \"openAdIds\": [\n" +
                                "      \"ca-app-pub-3940256099942544/9257395921\"\n" +
                                "    ],\n" +
                                "    \"interAdIds\": [\n" +
                                "      \"ca-app-pub-3940256099942544/1033173712\"\n" +
                                "    ],\n" +
                                "    \"nativeAdIds\": [\n" +
                                "      \"ca-app-pub-3940256099942544/2247696110\"\n" +
                                "    ],\n" +
                                "    \"bannerAdIds\": [\n" +
                                "      \"ca-app-pub-3940256099942544/9214589741\"\n" +
                                "    ]\n" +
                                "  },\n" +
                                "  \"facebookIds\": {\n" +
                                "    \"openAdIds\": [\n" +
                                "      \"IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID\"\n" +
                                "    ],\n" +
                                "    \"interAdIds\": [\n" +
                                "      \"IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID\"\n" +
                                "    ],\n" +
                                "    \"nativeAdIds\": [\n" +
                                "      \"IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID\"\n" +
                                "    ],\n" +
                                "    \"bannerAdIds\": [\n" +
                                "      \"IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID\"\n" +
                                "    ]\n" +
                                "  },\n" +
                                "  \"customLinks\": {\n" +
                                "    \"linkColor\": \"#000000\",\n" +
                                "    \"openRedirectLink\": [\n" +
                                "      \"https://www.google.com/\"\n" +
                                "    ],\n" +
                                "    \"interRedirectLink\": [\n" +
                                "      \"https://www.google.com/\"\n" +
                                "    ],\n" +
                                "    \"nativeRedirectLink\": [\n" +
                                "      \"https://www.google.com/\"\n" +
                                "    ],\n" +
                                "    \"bannerRedirectLink\": [\n" +
                                "      \"https://www.google.com/\"\n" +
                                "    ]\n" +
                                "  },\n" +
                                "  \"customAdsConfig\": {\n" +
                                "    \"mainHeadline\": [\n" +
                                "      \"Play & Win Diamond\uD83D\uDC8E\",\n" +
                                "      \"Play & Win Diamond\uD83D\uDC8E\",\n" +
                                "      \"Play & Win Diamond\uD83D\uDC8E\",\n" +
                                "      \"Play & Win Diamond\uD83D\uDC8E\",\n" +
                                "      \"Play & Win Diamond\uD83D\uDC8E\"\n" +
                                "    ],\n" +
                                "    \"headline\": [\n" +
                                "      \"Play & Win Diamond\uD83D\uDC8E\",\n" +
                                "      \"Play & Win Diamond\uD83D\uDC8E\",\n" +
                                "      \"Play & Win Diamond\uD83D\uDC8E\",\n" +
                                "      \"Play & Win Diamond\uD83D\uDC8E\",\n" +
                                "      \"Play & Win Diamond\uD83D\uDC8E\"\n" +
                                "    ],\n" +
                                "    \"description\": [\n" +
                                "      \"Win 1,00,000 Diamonds\uD83D\uDC8E & More\",\n" +
                                "      \"Win 1,00,000 Diamonds\uD83D\uDC8E & More\",\n" +
                                "      \"Win 1,00,000 Diamonds\uD83D\uDC8E & More\",\n" +
                                "      \"Win 1,00,000 Diamonds\uD83D\uDC8E & More\",\n" +
                                "      \"Win 1,00,000 Diamonds\uD83D\uDC8E & More\"\n" +
                                "    ],\n" +
                                "    \"buttonText\": [\n" +
                                "      \"Play Now\",\n" +
                                "      \"Play Now\",\n" +
                                "      \"Play Now\",\n" +
                                "      \"Play Now\",\n" +
                                "      \"Play Now\"\n" +
                                "    ],\n" +
                                "    \"nativeImageLarge\": [\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/n5.png\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/n4.png\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/n3.png\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/n2.png\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/n1.png\"\n" +
                                "    ],\n" +
                                "    \"nativeImageMedium\": [\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/musicianmagic.gif\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/roadrace2d.gif\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/n3.png\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/n2.png\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/n1.png\"\n" +
                                "    ],\n" +
                                "    \"nativeImageSmall\": [\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/musicianmagic.gif\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/roadrace2d.gif\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/pixelfiller.gif\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/gif.gif\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/gif.gif\"\n" +
                                "    ],\n" +
                                "    \"roundImage\": [\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/musicianmagic.gif\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/roadrace2d.gif\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/pixelfiller.gif\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/gif.gif\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/gif.gif\"\n" +
                                "    ],\n" +
                                "    \"bannerImage\": [\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/Banner3.png\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/Banner1.png\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/Banner2.png\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/Banner4.png\",\n" +
                                "      \"https://fff-apk.s3.us-east-1.amazonaws.com/FF+App+Ad+Assets/Banner3.png\"\n" +
                                "    ]\n" +
                                "  },\n" +
                                "  \"rewardAd\": {\n" +
                                "    \"rewardAdType\": \"custom\",\n" +
                                "    \"googleRewardAdId\": \"ca-app-pub-3940256099942544/5224354917\",\n" +
                                "    \"facebookAdId\": \"\",\n" +
                                "    \"unityAdId\": \"\",\n" +
                                "    \"isRewardShow\": false,\n" +
                                "    \"watch_ad_time\": 5,\n" +
                                "    \"watch_count\": 2,\n" +
                                "    \"auto_watch_ad_time\": 5\n" +
                                "  },\n" +
                                "  \"nativeAdConfig\": {\n" +
                                "    \"nativeTypeList\": \"large\",\n" +
                                "    \"nativeTypeOther\": \"large\",\n" +
                                "    \"backgroundColor\": \"#000000\",\n" +
                                "    \"fontColor\": \"#FFFFFF\",\n" +
                                "    \"buttonColor\": \"#007AFF\",\n" +
                                "    \"buttonColor2\": \"#007AFF\",\n" +
                                "    \"buttonFontColor\": \"#FFFFFF\"\n" +
                                "  },\n" +
                                "  \"facebookSDK\": {\n" +
                                "    \"clientToken\": \"\",\n" +
                                "    \"appId\": \"\"\n" +
                                "  },\n" +
                                "  \"isAppLive\": {\n" +
                                "    \"isAppLive\": true,\n" +
                                "    \"appName\": \"VidLink - Video Downloader\",\n" +
                                "    \"appIcon\": \"\",\n" +
                                "    \"appLink\": \"\",\n" +
                                "    \"appDescription\": \"\"\n" +
                                "  },\n" +
                                "  \"CountryList\": [\n" +
                                "    {\n" +
                                "      \"name\": \"India2\",\n" +
                                "      \"native_link\": [\n" +
                                "        \"https://303.play.pokiigame.com/\"\n" +
                                "      ],\n" +
                                "      \"banner_link\": [\n" +
                                "        \"https://303.play.pokiigame.com/\"\n" +
                                "      ],\n" +
                                "      \"inter_link\": [\n" +
                                "        \"https://303.play.pokiigame.com/\"\n" +
                                "      ],\n" +
                                "      \"appopen_link\": [\n" +
                                "        \"https://303.play.pokiigame.com/\"\n" +
                                "      ]\n" +
                                "    }\n" +
                                "  ]\n" +
                                "}""" // ← keep your full JSON string here or move to assets
//                    val fallbackConfig = Gson().fromJson(jsonFallback, RemoteConfigModel::class.java)

                    try {
                        val fallbackConfig = Gson().fromJson(jsonFallback, RemoteConfigModel::class.java)
                        Log.d("TAG", "Success: $fallbackConfig")
                        MyApp.ad_preferences.saveRemoteConfig(fallbackConfig)
                        fetchRemoteConfig()
                        Log.d("TAG", "fetchAndSetRemoteConfig:33 $fallbackConfig")
                    } catch (e: Exception) {
                        Log.e("TAG", "JSON Error: ${e.message}")
                    }


                    // wallpaper data fetch and intent
                }
            }
    }

    fun goNext() {
        val remoteConfigModel = MyApp.ad_preferences.getRemoteConfig()
        Log.e("checkPos", "isOnboardingAlways : ${remoteConfigModel!!.isOnboardingAlways}")
        Log.e("checkPos", "isOnboardingShow : ${remoteConfigModel!!.isOnboardingShow}")

        Handler(Looper.getMainLooper()).postDelayed({
            mediaPlayer?.apply {
                stop()
                release()
                mediaPlayer = null
            }

            if (!SharePref.isOnboarding(this)) {

                if (remoteConfigModel.isOnboardingAlways || remoteConfigModel.isOnboardingShow) {
                    startActivity(Intent(this, LanguageActivity::class.java))
                } else {
                    startActivity(Intent(this, LanguageActivity::class.java))
                }
            } else {
                if (remoteConfigModel.isOnboardingShow) {
                    startActivity(Intent(this, AppShowAcivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }

            finish()
        }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.apply {
            stop()
            release()
            mediaPlayer = null
        }
    }


    private fun setApplication(applicationId: String, token: String) {
        FacebookSdk.setApplicationId(applicationId)
        FacebookSdk.setClientToken(token)

        FacebookSdk.sdkInitialize(this) {
            FacebookSdk.setAutoLogAppEventsEnabled(true)
            FacebookSdk.setAdvertiserIDCollectionEnabled(true)
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.fullyInitialize()
            FacebookSdk.setAutoLogAppEventsEnabled(true)

            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
            val logger = AppEventsLogger.newLogger(this)
            logger.applicationId
        }
    }

    private fun preloadAdsIfEnabled() {
        val config = MyApp.ad_preferences.getRemoteConfig()

        if (config != null && config.adsLoadType == "Preload") {
            when (config.adsType) {
                "Admob" -> Ads_Interstitial.Admob_InterstitialAd(this)
                "Adx" -> Ads_Interstitial.Adx_InterstitialAd(this)
                "Facebook" -> Ads_Interstitial.Fb_InterstitialAd(this)
            }
        }

        if (config != null && config.nativeLoadType == "Preload") {
            when (config.nativeAdsType) {
                "Admob" -> NativeAds_Class.AdmobNativeFull(this, null, null)
                "Adx" -> NativeAds_Class.AdxNativeFull(this, null, null)
                "Facebook" -> NativeAds_Class.FB_NativeAd(this, null, null)
            }
        }
    }

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    private fun preloadAndPlayLottie() {
        LottieCompositionFactory.fromRawRes(this, R.raw.success)
            .addListener { composition ->
                lottieAnimationView.setComposition(composition)
                lottieAnimationView.setRenderMode(RenderMode.HARDWARE)
                lottieAnimationView.repeatCount = LottieDrawable.INFINITE
                lottieAnimationView.playAnimation()
            }
            .addFailureListener { throwable ->
                throwable.printStackTrace()
            }
    }

    private fun fetchRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hour cache
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                val json = remoteConfig.getString("mahadev_wallpaper")
                parseJson(json)
//          fetchFcmToken() // Uncomment if needed
                fetchBackendToken()
            }
    }

    private fun parseJson(json: String) {
        try {
            val jsonObject = JSONObject(json)
            BaseUrl = jsonObject.optString("base_url")
            categoryIdSpecial = jsonObject.optInt("special_category")
            categoryId = jsonObject.optInt("shiva_wallpaper")
            ImgUri = jsonObject.optString("wall_data")
//            categoryId = jsonObject.optInt("shiva_wallpaper")

            Log.d("RemoteConfig", "Base URL:" + BaseUrl.toString())
            Log.d("RemoteConfig", "Special Category: $categoryIdSpecial")
            Log.d("RemoteConfig", "Shiva Wallpaper: $categoryId")
            Log.d("RemoteConfig", "TokenUrl: $ImgUri")

            // Save base URL as token (example)
            SharedPrefsManager.setToken(this, BaseUrl.toString())
            SharedPrefsManager.setImgUri(this, ImgUri.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun fetchBackendToken() {
        if (BaseUrl.isNullOrEmpty()) {
            goNext()
            return
        }

        if (apiService == null) {
            val retrofit = Retrofit.Builder()
                .baseUrl(BaseUrl!!)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }

        Thread {
            try {
                val response = apiService?.getToken()?.execute()
                if (response != null && response.isSuccessful && response.body() != null) {
                    strToken = response.body()!!.token
                    Log.d("TOKEN", "Saved token: $strToken")
                    SharedPrefsManager.setTokenMain(this, strToken.toString())
                    runOnUiThread { goNext() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }.start()
    }


}


