package com.yourapp.premium.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson

class EncryptionUtil(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        "premium_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val gson = Gson()

    fun <T> saveEncrypted(key: String, data: T, clazz: Class<T>) {
        val json = gson.toJson(data)
        encryptedSharedPreferences.edit().putString(key, json).apply()
    }

    fun <T> getEncrypted(key: String, clazz: Class<T>): T? {
        return try {
            val json = encryptedSharedPreferences.getString(key, null)
            json?.let { gson.fromJson(it, clazz) }
        } catch (e: Exception) {
            null
        }
    }

    fun saveToken(token: String) {
        encryptedSharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? {
        return encryptedSharedPreferences.getString("auth_token", null)
    }

    fun clearAll() {
        encryptedSharedPreferences.edit().clear().apply()
    }
}

