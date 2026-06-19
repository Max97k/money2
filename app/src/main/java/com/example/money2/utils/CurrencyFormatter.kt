package com.example.money2.utils

object CurrencyFormatter {
    fun format(amount: Double, targetCurrency: String, exchangeRate: Float, nativeCurrency: String = "USD"): String {
        val convertedAmount = when {
            nativeCurrency == targetCurrency -> amount
            nativeCurrency == "USD" && targetCurrency == "TWD" -> amount * exchangeRate
            nativeCurrency == "TWD" && targetCurrency == "USD" -> amount / exchangeRate
            else -> amount
        }
        
        return if (targetCurrency == "TWD") {
            "NT$ %,.2f".format(convertedAmount)
        } else {
            "$ %,.2f".format(convertedAmount)
        }
    }
}
