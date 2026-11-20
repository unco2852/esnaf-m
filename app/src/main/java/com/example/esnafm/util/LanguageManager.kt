package com.example.esnafm.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

/**
 * Dil Seçimi ve Dinamik Yerelleştirme Yöneticisi (Türkçe & English)
 */
object LanguageManager {
    private const val PREF_NAME = "esnafm_language_prefs"
    private const val KEY_LANG = "selected_language"

    fun getSavedLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "tr") ?: "tr"
    }

    fun setLanguage(context: Context, langCode: String): Context {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, langCode).apply()
        return updateResources(context, langCode)
    }

    fun updateResources(context: Context, langCode: String): Context {
        val locale = Locale(if (langCode == "en") "en" else "tr")
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}