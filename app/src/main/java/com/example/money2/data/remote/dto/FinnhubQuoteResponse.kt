package com.example.money2.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FinnhubQuoteResponse(
    @SerializedName("c") val currentPrice: Double,
    @SerializedName("d") val change: Double,
    @SerializedName("dp") val changePercent: Double,
    @SerializedName("h") val highPrice: Double,
    @SerializedName("l") val lowPrice: Double,
    @SerializedName("o") val openPrice: Double,
    @SerializedName("pc") val previousClosePrice: Double,
    @SerializedName("t") val timestamp: Long
)
