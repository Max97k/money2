package com.example.money2.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.money2.data.local.prefs.EncryptedPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val encryptedPrefs: EncryptedPrefs
) : ViewModel() {

    private val _apiKey = MutableStateFlow(encryptedPrefs.getApiKey() ?: "")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    fun saveApiKey(key: String) {
        encryptedPrefs.saveApiKey(key)
        _apiKey.value = key
    }
}
