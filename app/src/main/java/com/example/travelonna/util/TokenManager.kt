package com.example.travelonna.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object TokenManager {
    private const val PREF_NAME = "AuthPrefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val TAG = "TokenManager"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        Log.d(TAG, "Saving tokens. Access token length: ${accessToken.length}")
        getPrefs(context).edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    fun getAccessToken(context: Context): String? {
        val token = getPrefs(context).getString(KEY_ACCESS_TOKEN, null)
        Log.d(TAG, "Getting access token: ${token?.substring(0, minOf(10, token.length))}...")
        return token
    }

    fun getRefreshToken(context: Context): String? {
        return getPrefs(context).getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens(context: Context) {
        Log.d(TAG, "Clearing tokens")
        getPrefs(context).edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        val isLoggedIn = getAccessToken(context) != null
        Log.d(TAG, "isLoggedIn check: $isLoggedIn")
        return isLoggedIn
    }
} 