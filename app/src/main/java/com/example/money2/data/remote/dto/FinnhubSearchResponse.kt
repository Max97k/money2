package com.example.money2.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FinnhubSearchResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("result") val result: List<FinnhubSearchResult>
)

data class FinnhubSearchResult(
    @SerializedName("description") val description: String,
    @SerializedName("displaySymbol") val displaySymbol: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("type") val type: String
)
