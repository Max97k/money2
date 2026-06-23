package com.example.money2.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.money2.data.local.prefs.EncryptedPrefs
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.money2.domain.repository.MarketRepository

class SettingsViewModel(
    private val encryptedPrefs: EncryptedPrefs,
    private val marketRepository: MarketRepository
) : ViewModel() {
    val selectedCurrency: StateFlow<String> = encryptedPrefs.selectedCurrencyFlow
    val exchangeRate: StateFlow<Float> = encryptedPrefs.exchangeRateFlow

    init {
        viewModelScope.launch {
            val result = marketRepository.getExchangeRates()
            result.getOrNull()?.let { rates ->
                rates["TWD"]?.let { twdRate ->
                    encryptedPrefs.saveExchangeRate(twdRate.toFloat())
                }
            }
        }
    }

    fun toggleCurrency() {
        val newCurrency = if (selectedCurrency.value == "USD") "TWD" else "USD"
        saveSelectedCurrency(newCurrency)
    }

    fun saveSelectedCurrency(currency: String) {
        encryptedPrefs.saveSelectedCurrency(currency)
    }

    fun saveExchangeRate(rate: Float) {
        encryptedPrefs.saveExchangeRate(rate)
    }
}
