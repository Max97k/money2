package com.example.money2.data.local.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EncryptedPrefs(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _selectedCurrencyFlow = MutableStateFlow(getSelectedCurrency())
    val selectedCurrencyFlow: StateFlow<String> = _selectedCurrencyFlow.asStateFlow()

    private val _exchangeRateFlow = MutableStateFlow(getExchangeRate())
    val exchangeRateFlow: StateFlow<Float> = _exchangeRateFlow.asStateFlow()

    private val _isFirstLaunchFlow = MutableStateFlow(getIsFirstLaunch())
    val isFirstLaunchFlow: StateFlow<Boolean> = _isFirstLaunchFlow.asStateFlow()



    fun saveSelectedCurrency(currency: String) {
        sharedPreferences.edit().putString("SELECTED_CURRENCY", currency).apply()
        _selectedCurrencyFlow.value = currency
    }

    fun getSelectedCurrency(): String {
        return sharedPreferences.getString("SELECTED_CURRENCY", "USD") ?: "USD"
    }

    fun saveExchangeRate(rate: Float) {
        sharedPreferences.edit().putFloat("EXCHANGE_RATE", rate).apply()
        _exchangeRateFlow.value = rate
    }

    fun getExchangeRate(): Float {
        return sharedPreferences.getFloat("EXCHANGE_RATE", 32.5f)
    }

    fun saveIsFirstLaunch(isFirstLaunch: Boolean) {
        sharedPreferences.edit().putBoolean("IS_FIRST_LAUNCH", isFirstLaunch).apply()
        _isFirstLaunchFlow.value = isFirstLaunch
    }

    fun getIsFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("IS_FIRST_LAUNCH", true)
    }
}
