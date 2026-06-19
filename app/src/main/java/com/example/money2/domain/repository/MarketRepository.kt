package com.example.money2.domain.repository

import com.example.money2.data.remote.dto.FinnhubSearchResult

interface MarketRepository {
    suspend fun getLatestPrice(symbol: String): Result<Double>
    suspend fun searchStocks(query: String): Result<List<FinnhubSearchResult>>
    suspend fun getExchangeRates(): Result<Map<String, Double>>
    suspend fun fetchHistoricalPrices(symbol: String, range: String = "2y", interval: String = "1d"): Result<List<Pair<Long, Double>>>
}
