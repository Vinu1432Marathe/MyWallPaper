package com.mahadev.shivahd.live.wallpaper.Other

import android.content.Context
import android.content.res.Configuration
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mahadev.shivahd.live.wallpaper.Model.ModelWallpaper
import java.util.Locale

object PrefsManager {

    private const val PREF_NAME = "wallpaper_prefs"
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_SAVEWALL = "WallSave"

    private const val KEY_WALLPAPER_LIST = "wallpaper_list"

    fun saveFavoriteList(context: Context, list: List<ModelWallpaper>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(list)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

    fun getFavoriteList(context: Context): MutableList<ModelWallpaper> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_FAVORITES, null)
        return if (json != null) {
            val type = object : TypeToken<List<ModelWallpaper>>() {}.type
            Gson().fromJson<List<ModelWallpaper>>(json, type).toMutableList()
        } else {
            mutableListOf()
        }
    }

    fun saveSevaWallPaperList(context: Context, list: List<ModelWallpaper>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(list)
        prefs.edit().putString(KEY_SAVEWALL, json).apply()
    }

    fun getSevaWallPaperList(context: Context): MutableList<ModelWallpaper> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SAVEWALL, null)
        return if (json != null) {
            val type = object : TypeToken<List<ModelWallpaper>>() {}.type
            Gson().fromJson<List<ModelWallpaper>>(json, type).toMutableList()
        } else {
            mutableListOf()
        }
    }


    fun saveWallpaperList(context: Context, list: List<ModelWallpaper>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(list)
        editor.putString(KEY_WALLPAPER_LIST, json)
        editor.apply()
    }

    fun getWallpaperList(context: Context): List<ModelWallpaper> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_WALLPAPER_LIST, null)
        return if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<ModelWallpaper>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }

}


// PreferencesHelper.kt
class PreferencesHelper11(context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var selectedLanguage: String?
        get() = prefs.getString("app_lang", "en")
        set(value) = prefs.edit().putString("app_lang", value).apply()

    var isLangSetOnce: Boolean
        get() = prefs.getBoolean("lang_set", false)
        set(value) = prefs.edit().putBoolean("lang_set", value).apply()


}

// LocaleHelper.kt
object LocaleHelper {
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}