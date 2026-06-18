package com.example.money2.data.remote.dto

data class ExchangeRateResponse(
    val result: String,
    val conversion_rates: Map<String, Double>
)
