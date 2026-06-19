package com.example.money2.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun format_withTWD_multipliesByExchangeRateAndRemovesDecimals() {
        val amount = 100.0
        val exchangeRate = 30.5f
        val result = CurrencyFormatter.format(amount, "TWD", exchangeRate)

        assertEquals("NT$ 3050", result)
    }

    @Test
    fun format_withTWDRounding_roundsToNearestInteger() {
        val amount = 10.0
        val exchangeRate = 30.56f
        val result = CurrencyFormatter.format(amount, "TWD", exchangeRate)

        assertEquals("NT$ 306", result)
    }

    @Test
    fun format_withNonTWD_formatsToTwoDecimalsAndIgnoresExchangeRate() {
        val amount = 100.5
        val exchangeRate = 30.5f
        val result = CurrencyFormatter.format(amount, "USD", exchangeRate)

        assertEquals("$ 100.50", result)
    }

    @Test
    fun format_withNonTWDRounding_roundsToTwoDecimalPlaces() {
        val amount = 99.999
        val exchangeRate = 1.0f
        val result = CurrencyFormatter.format(amount, "EUR", exchangeRate)

        assertEquals("$ 100.00", result)
    }
}
