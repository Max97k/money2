package com.example.money2.utils

import androidx.compose.runtime.compositionLocalOf

data class CurrencyInfo(
    val currency: String = "USD",
    val exchangeRate: Float = 32.5f
)

val LocalCurrencyInfo = compositionLocalOf { CurrencyInfo() }
