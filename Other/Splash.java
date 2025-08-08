package com.mahadev.shivahd.live.wallpaper.Other;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.addsdemo.mysdk.ADPrefrences.Ads_Interstitial;
import com.addsdemo.mysdk.ADPrefrences.AppOpenAdManager;
import com.addsdemo.mysdk.ADPrefrences.MyApp;
import com.addsdemo.mysdk.ADPrefrences.NativeAds_Class;
import com.addsdemo.mysdk.model.RemoteConfigModel;
import com.addsdemo.mysdk.retrofit.InstallerID;
import com.addsdemo.mysdk.retrofit.MyReferrer;
import com.addsdemo.mysdk.utils.CustomTabLinkOpen;
import com.addsdemo.mysdk.utils.UtilsClass;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieListener;
import com.airbnb.lottie.RenderMode;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.magic.hd.wallpapers.remote.ApiService;
import com.mahadev.shivahd.live.wallpaper.Activity.AppShowAcivity;
import com.mahadev.shivahd.live.wallpaper.Activity.MainActivity;
import com.mahadev.shivahd.live.wallpaper.Language.LanguageActivity;
import com.mahadev.shivahd.live.wallpaper.Model.TokenDto;
import com.mahadev.shivahd.live.wallpaper.R;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.onesignal.notifications.INotificationClickEvent;
import com.onesignal.notifications.INotificationClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Splash extends AppCompatActivity {

    private ApiService apiService;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    FirebaseRemoteConfig mFirebaseRemoteConfig;

    RemoteConfigModel remoteConfigModel;
    private MediaPlayer mediaPlayer;
    LottieAnimationView lottieAnimationView;

    public  String BaseUrl;
    public  String strToken;
    public  int categoryIdSpecial;
    public  int categoryId;
    String ONESIGNAL_APP_ID = "f95387a8-0a67-4317-83a8-0b6a44f7e107";

    public static boolean checkConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        return isConnected;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String langCode = new PreferencesHelper11(newBase).getSelectedLanguage();
        if (langCode == null) langCode = "en";
        super.attachBaseContext(setLocale(newBase, langCode));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        lottieAnimationView = findViewById(R.id.lottieView);
//        lottieAnimationView.playAnimation();
        preloadAndPlayLottie();
        mediaPlayer = MediaPlayer.create(this, R.raw.shivsong);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();



//        fetchRemoteData();
        initviews();
//        fetchTokenIfNeeded();

    }


    private void initviews() {

        if (checkConnection(Splash.this)) {

            OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

            // Initialize OneSignal
            OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

            // Add click listener for push notifications
            OneSignal.getNotifications().addClickListener(new INotificationClickListener() {
                @Override
                public void onClick(@NonNull INotificationClickEvent event) {
                    JSONObject data = event.getNotification().getAdditionalData();
                    if (data != null) {
                        String actionType = data.optString("action_type");
                        String url = data.optString("url");

                        if ("open_url".equals(actionType) && url != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } else {
                        Log.d("OneSignal", "No additional data in notification");
                    }
                }
            });


            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(5)
                    .build();
            mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
            new InstallerID().callInstallerID(Splash.this);
            fetchAndSetRemoteConfig();
        } else {
            checkConnectivity();
        }
    }

    private void checkConnectivity() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

        if (activeNetwork == null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);


            dialogBuilder.setMessage("Make sure that WI-FI or mobile data is turned on, then try again")

                    .setCancelable(false)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            recreate();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });


            AlertDialog alert = dialogBuilder.create();

            alert.setTitle("No Internet Connection..");
            alert.setIcon(R.drawable.s_logo);

            alert.show();
        }
    }


    private void fetchAndSetRemoteConfig() {


        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {


                        Log.e("InstallerID", "InstallerID " + InstallerID.referrerUrl);

                        if (InstallerID.referrerUrl != null &&
                                (InstallerID.referrerUrl.contains("organic") || InstallerID.referrerUrl.contains("not%20set"))) {

                            remoteConfigModel = new Gson().fromJson(mFirebaseRemoteConfig.getString("app_orgdata"), RemoteConfigModel.class);

                            Log.e("InstallerID", "InstallerID   app_orgdata");

                        } else {
                            remoteConfigModel = new Gson().fromJson(mFirebaseRemoteConfig.getString("app_data"), RemoteConfigModel.class);

                            Log.e("InstallerID", "InstallerID   app_data");
                        }


                        Log.d("TAG", "fetchAndSetRemoteConfig: 11 " + remoteConfigModel);
                        String versiocode = "0";


                        try {
                            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            versiocode = pInfo.versionName;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (Objects.equals(versiocode, remoteConfigModel.getVersionName())) {
                            remoteConfigModel.setAdShow(false);
                            remoteConfigModel.setOnAdRedirect(false);
                            remoteConfigModel.setOnboardingAdShow(false);
                        }

                        if (!remoteConfigModel.getFacebookSDK().getAppId().isEmpty() &&
                                !remoteConfigModel.getFacebookSDK().getClientToken().isEmpty()) {
                            SetApplication(remoteConfigModel.getFacebookSDK().getAppId(),
                                    remoteConfigModel.getFacebookSDK().getClientToken());
                        }

                        MyReferrer.GetCountryDetails(this, remoteConfigModel, new MyReferrer.ApiIpCallback() {
                            @Override
                            public void onSuccess(RemoteConfigModel response) {

                                MyApp.ad_preferences.saveRemoteConfig(response);
                                MyApp.ad_preferences.saveIsAppopenShow(response.isResumeShow());

                                preloadAdsIfEnabled();

                                MyApp.getInstance().getAppOpenAdManager().showAdIfAvailable(Splash.this, new AppOpenAdManager.MyAdCallBack() {
                                    @Override
                                    public void onAdClose(boolean value) {
                                        if (!isFinishing()) {
//                                            goNext();
                                            fetchRemoteConfig(); //todo for wallpaper data get and intent
                                        }
                                    }
                                }, response.isOpenShow(), response.isAdShow());
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                MyApp.ad_preferences.saveRemoteConfig(remoteConfigModel);
                                MyApp.ad_preferences.saveIsAppopenShow(remoteConfigModel.isResumeShow());

                                Log.d("TAG123456789", "fetchAndSetRemoteConfig:22 " + remoteConfigModel);

                                preloadAdsIfEnabled();

                                MyApp.getInstance().getAppOpenAdManager().showAdIfAvailable(Splash.this, new AppOpenAdManager.MyAdCallBack() {
                                    @Override
                                    public void onAdClose(boolean value) {
                                        if (!isFinishing()) {
//                                            goNext();
                                            fetchRemoteConfig(); //todo for wallpaper data get and intent
                                            if (!value && remoteConfigModel.getOpenAdType().equals("Redirect") && remoteConfigModel.isOpenShow() && remoteConfigModel.isAdShow()) {
                                                CustomTabLinkOpen.openLink(Splash.this, UtilsClass.getRandomRedirectLink(MyApp.ad_preferences.getRemoteConfig().getCustomLinks().getOpenRedirectLink()), "appOpen_click");
                                            } else if (value && remoteConfigModel.isOnAdRedirect()) {
                                                CustomTabLinkOpen.openLink(Splash.this, UtilsClass.getRandomRedirectLink(MyApp.ad_preferences.getRemoteConfig().getCustomLinks().getOpenRedirectLink()), "appOpen_click");
                                            }


                                        }
                                    }
                                }, remoteConfigModel.isOpenShow(), remoteConfigModel.isAdShow());
                            }
                        });

                    } else {

                        RemoteConfigModel remoteConfigModel = new Gson().fromJson("{\n" +
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
                                "}", RemoteConfigModel.class);
                        MyApp.ad_preferences.saveRemoteConfig(remoteConfigModel);
                        Log.d("TAG", "fetchAndSetRemoteConfig:33 " + remoteConfigModel);
//                        goNext();
                        fetchRemoteConfig(); //todo for wallpaper data get and intent
                    }
                });


    }

    public void goNext() {


        RemoteConfigModel remoteConfigModel = MyApp.ad_preferences.getRemoteConfig();
        Log.e("checkPos", "isOnboardingAlways : " + remoteConfigModel.isOnboardingAlways());
        Log.e("checkPos", "isOnboardingShow : " + remoteConfigModel.isOnboardingShow());

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }

                if (!SharePref.INSTANCE.isOnboarding(Splash.this) || remoteConfigModel.isOnboardingAlways()) {

                    startActivity(new Intent(Splash.this, LanguageActivity.class));

                } else {

                    if (remoteConfigModel.isOnboardingShow()) {
                        startActivity(new Intent(Splash.this, AppShowAcivity.class));

                    } else {
                        startActivity(new Intent(Splash.this, MainActivity.class));
                    }

                }


                finish();
            }
        }, 2000);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    private void SetApplication(String application_id, String token) {

        FacebookSdk.setApplicationId(application_id);
        FacebookSdk.setClientToken(token);

        FacebookSdk.sdkInitialize(Splash.this, new FacebookSdk.InitializeCallback() {
            @Override
            public void onInitialized() {
                FacebookSdk.setAutoLogAppEventsEnabled(true);
                FacebookSdk.setAdvertiserIDCollectionEnabled(true);
                FacebookSdk.setAutoInitEnabled(true);
                FacebookSdk.fullyInitialize();
                FacebookSdk.setAutoLogAppEventsEnabled(true);

                FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
                AppEventsLogger logger = AppEventsLogger.newLogger(Splash.this);

                logger.getApplicationId();

            }
        });
    }

    private void preloadAdsIfEnabled() {
        if (MyApp.ad_preferences.getRemoteConfig() != null && "Preload".equals(MyApp.ad_preferences.getRemoteConfig().getAdsLoadType())) {
            String adsType = MyApp.ad_preferences.getRemoteConfig().getAdsType();

            if ("Admob".equals(adsType)) {
                Ads_Interstitial.Admob_InterstitialAd(this);
            } else if ("Adx".equals(adsType)) {
                Ads_Interstitial.Adx_InterstitialAd(this);
            } else if ("Facebook".equals(adsType)) {
                Ads_Interstitial.Fb_InterstitialAd(this);
            }
        }

        if (MyApp.ad_preferences.getRemoteConfig() != null && "Preload".equals(MyApp.ad_preferences.getRemoteConfig().getNativeLoadType())) {
            String nativeAdsType = MyApp.ad_preferences.getRemoteConfig().getNativeAdsType();

            if ("Admob".equals(nativeAdsType)) {
                NativeAds_Class.AdmobNativeFull(this, null, null);
            } else if ("Adx".equals(nativeAdsType)) {
                NativeAds_Class.AdxNativeFull(this, null, null);
            } else if ("Facebook".equals(nativeAdsType)) {
                NativeAds_Class.FB_NativeAd(this, null, null);
            }
        }
    }

    public Context setLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }


    private void preloadAndPlayLottie() {
        LottieCompositionFactory.fromRawRes(this, R.raw.success)
                .addListener(new LottieListener<LottieComposition>() {
                    @Override
                    public void onResult(LottieComposition composition) {
                        lottieAnimationView.setComposition(composition);
                        lottieAnimationView.setRenderMode(RenderMode.HARDWARE);
                        lottieAnimationView.setRepeatCount(LottieDrawable.INFINITE);
                        lottieAnimationView.playAnimation();

                    }
                })
                .addFailureListener(new LottieListener<Throwable>() {
                    @Override
                    public void onResult(Throwable throwable) {
                        throwable.printStackTrace(); // log error
                    }
                });
    }


    private void fetchRemoteConfig() {

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
//
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 hour cache
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    String json = remoteConfig.getString("mahadev_wallpaper");
                    parseJson(json);
//                    fetchFcmToken();

                    fetchBackendToken();
                });
    }

    private void parseJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            BaseUrl = jsonObject.optString("base_url");
            categoryIdSpecial = jsonObject.optInt("special_category");
            categoryId = jsonObject.optInt("shiva_wallpaper");
            Log.d("RemoteConfig", "Base URL: " + BaseUrl);
            Log.d("RemoteConfig", "Special Category: " + categoryIdSpecial);
            Log.d("RemoteConfig", "Shiva Wallpaper: " + categoryId);
// Example: Save full JSON or individual values
            SharedPrefsManager.setToken(Splash.this, BaseUrl); // Just an example
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void fetchBackendToken() {
        if (BaseUrl == null || BaseUrl.isEmpty()) {
            goNext();
            return;
        }
        if (apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }

        new Thread(() -> {
            try {
                Response<TokenDto> response = apiService.getToken().execute();
                if (response.isSuccessful() && response.body() != null) {
                    strToken = response.body().getToken();
                    Log.d("TOKEN", "Saved token: " + strToken);
                    SharedPrefsManager.setTokenMain(Splash.this, strToken);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(this::goNext);
        }).start();
    }



}
