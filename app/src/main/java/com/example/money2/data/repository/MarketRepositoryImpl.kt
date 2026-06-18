package com.example.money2.data.repository

import com.example.money2.data.local.prefs.EncryptedPrefs
import com.example.money2.data.remote.api.MarketApi
import com.example.money2.domain.repository.MarketRepository

class MarketRepositoryImpl(
    private val api: MarketApi,
    private val prefs: EncryptedPrefs
) : MarketRepository {
    override suspend fun getLatestPrice(symbol: String): Result<Double> {
        return try {
            val response = api.getGlobalQuote(symbol)
            val price = response.currentPrice
            if (price > 0.0) {
                Result.success(price)
            } else {
                Result.failure(Exception("Invalid response or missing price for $symbol"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
