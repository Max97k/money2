package com.example.money2.domain.repository

interface MarketRepository {
    suspend fun getLatestPrice(symbol: String): Result<Double>
}
