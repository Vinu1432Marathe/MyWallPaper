package com.mahadev.shivahd.live.wallpaper.Other

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.core.content.edit
import com.addsdemo.mysdk.ADPrefrences.MyApp
import com.mahadev.shivahd.live.wallpaper.Language.Model_Language
import com.mahadev.shivahd.live.wallpaper.R

object SharePref {

    private const val PREF_NAME = "ShivaWallApp"
    private const val VIDEO_LIST_KEY = "video_list"

    private val KEY_SWITCH_STATE = "isSwitchOn"
    //todo Function to save a video in SharedPreferences


    private const val KEY_AppShow = "AppShow"
    private const val KEY_Rate = "AppRate"
    internal fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setOnboarding(context: Context, accepted: Boolean) {
        getSharedPreferences(context).edit()
            .putBoolean(KEY_AppShow, accepted)
            .apply()
    }

    // Check if the terms are accepted
    fun isOnboarding(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_AppShow, false)
    }

    fun setSwitch(context: Context, accepted: Boolean) {
        getSharedPreferences(context).edit()
            .putBoolean(KEY_SWITCH_STATE, accepted)
            .apply()
    }

    // Check if the terms are accepted
    fun isSwitch(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_SWITCH_STATE, false)
    }

    fun setRate(context: Context, accepted: Boolean) {
        getSharedPreferences(context).edit() {
            putBoolean(KEY_Rate, accepted)
        }
    }

    // Check if the terms are accepted
    fun isRate(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_Rate, false)
    }


    fun shareApp(context: Context) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Hey, check out this awesome app! ${
                context.packageManager.getPackageInfo(
                    context.packageName,
                    0
                ).applicationInfo?.loadLabel(context.packageManager)
            } https://play.google.com/store/apps/details?id=${context.packageName}"
        )
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    fun rateUs(context: Context) {
        val packageName = context.packageName
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }


    fun openPrivacy(context: Context) {

        val intent = Intent(Intent.ACTION_VIEW)
        val configPref = MyApp.ad_preferences.getRemoteConfig()

        if (configPref?.privacyPolicy?.isNotEmpty() == true) {

            intent.data = Uri.parse(configPref.privacyPolicy)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
            }
        } else {
            Toast.makeText(context, "Unable to load!", Toast.LENGTH_SHORT).show()
        }


    }

    val languages_list = listOf(
        Model_Language("हिंदी", "hi", R.drawable.hindi),
        Model_Language("বাংলা", "bn", R.drawable.us),
        Model_Language("English", "en", R.drawable.united_kingdom),
        Model_Language("తెలుగు", "te", R.drawable.ic_thailand),
        Model_Language("मराठी", "mr", R.drawable.ic_vietnam),
        Model_Language("தமிழ்", "ta", R.drawable.ic_indonesia),
        Model_Language("ગુજરાતી", "gu", R.drawable.arabic),
//        Model_Language("اردو", "ur", R.drawable.ic_nepal)
    )


}

object SharedPrefsManager {

    private const val KEY_FIRST_LAUNCH = "first_launch"
    private const val KEY_SELECTED_CATEGORY = "selected_category"
    private const val USER_TOKEN = "user_token"
    private const val IMG_URI = "img_uri"
    private const val USER_TOKEN_Main = "Main_token"


    private const val APP_PREFERENCES = "Tokeen"
    private val Context.prefs: SharedPreferences
        get() = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)

    @JvmStatic
    fun isFirstLaunch(context: Context): Boolean {
        return context.prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(context: Context) {
        context.prefs.edit() { putBoolean(KEY_FIRST_LAUNCH, false) }
    }

    @JvmStatic
    fun setToken(context: Context, token: String) {
        context.prefs.edit { putString(USER_TOKEN, token) }
    }


    fun getToken(context: Context): String? {
        return context.prefs.getString(USER_TOKEN, null)
    }
    @JvmStatic
    fun setImgUri(context: Context, token: String) {
        context.prefs.edit { putString(IMG_URI, token) }
    }


    fun getImgUri(context: Context): String? {
        return context.prefs.getString(IMG_URI, null)
    }

    @JvmStatic
    fun setTokenMain(context: Context, token: String) {
        context.prefs.edit { putString(USER_TOKEN_Main, token) }
    }


    fun getTokenMain(context: Context): String? {
        return context.prefs.getString(USER_TOKEN_Main, null)
    }

    // Save category
    fun saveUserCategories(context: Context, categories: List<Int>) {
        context.prefs.edit {
            // Convert List<Int> to Set<String> as putStringSet requires Set<String>
            putStringSet(KEY_SELECTED_CATEGORY, categories.map { it.toString() }.toSet())
        }
    }

    fun getUserCategories(context: Context): List<Int> {
        // Retrieve the Set<String>, defaulting to an empty set if not found
        val idSet = context.prefs.getStringSet(KEY_SELECTED_CATEGORY, emptySet()) ?: emptySet()

        // Convert Set<String> back to List<Int>
        return idSet.mapNotNull { it.toIntOrNull() }
    }


}