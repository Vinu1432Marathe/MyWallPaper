package com.mahadev.shivahd.live.wallpaper.Language

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.preference.PreferenceManager
import java.util.Locale

class PreferencesHelper(context: Context)  {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            "VideoDownloadLanguage",
            Context.MODE_PRIVATE
        )


    companion object {

        const val KEY_SELECTED_LANGUAGE = "video_language"
        const val KEY_IS_LANG = "video_lang"

    }

    var selectedLanguage: String
        get() = sharedPreferences.getString(KEY_SELECTED_LANGUAGE, "en")!!
        set(value) {
            sharedPreferences.edit().putString(KEY_SELECTED_LANGUAGE, value).apply()
        }

    var isLangSetOnce: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_LANG, false)
        set(value) {
            sharedPreferences.edit().putBoolean(KEY_IS_LANG, value).apply()
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

