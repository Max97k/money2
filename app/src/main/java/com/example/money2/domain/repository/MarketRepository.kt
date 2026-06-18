package com.example.money2.domain.repository

import com.example.money2.data.remote.dto.FinnhubSearchResult

interface MarketRepository {
    suspend fun getLatestPrice(symbol: String): Result<Double>
    suspend fun searchStocks(query: String): Result<List<FinnhubSearchResult>>
    suspend fun getExchangeRates(): Result<Map<String, Double>>
}
