package com.example.money2.utils

object CurrencyFormatter {
    fun format(amount: Double, currency: String, exchangeRate: Float): String {
        return if (currency == "TWD") {
            val converted = amount * exchangeRate
            "NT$ %.0f".format(converted)
        } else {
            "$ %.2f".format(amount)
        }
    }
}
