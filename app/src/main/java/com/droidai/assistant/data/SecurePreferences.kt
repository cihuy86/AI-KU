package com.droidai.assistant.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePreferences {

    private const val PREFS_FILE_NAME = "droidai_secure_prefs"
    private const val KEY_GEMINI_API_KEY = "encrypted_gemini_api_key"
    private const val KEY_AUTO_ANSWER_ENABLED = "pref_auto_answer_enabled"

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveApiKey(context: Context, apiKey: String) {
        getEncryptedPrefs(context).edit()
            .putString(KEY_GEMINI_API_KEY, apiKey.trim())
            .apply()
    }

    fun getApiKey(context: Context): String? {
        return getEncryptedPrefs(context).getString(KEY_GEMINI_API_KEY, null)
    }

    fun clearApiKey(context: Context) {
        getEncryptedPrefs(context).edit().remove(KEY_GEMINI_API_KEY).apply()
    }

    fun isApiKeyConfigured(context: Context): Boolean {
        val key = getApiKey(context)
        return !key.isNullOrBlank() && key.length > 10
    }

    fun setAutoAnswerEnabled(context: Context, enabled: Boolean) {
        getEncryptedPrefs(context).edit().putBoolean(KEY_AUTO_ANSWER_ENABLED, enabled).apply()
    }

    fun isAutoAnswerEnabled(context: Context): Boolean {
        return getEncryptedPrefs(context).getBoolean(KEY_AUTO_ANSWER_ENABLED, false)
    }
}