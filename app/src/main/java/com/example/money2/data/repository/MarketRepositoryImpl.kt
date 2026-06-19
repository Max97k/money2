package com.example.money2.data.repository

import com.example.money2.data.local.prefs.EncryptedPrefs
import com.example.money2.data.remote.api.MarketApi
import com.example.money2.data.remote.dto.FinnhubSearchResult
import com.example.money2.domain.repository.MarketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MarketRepositoryImpl(
    private val api: MarketApi,
    private val prefs: EncryptedPrefs
) : MarketRepository {
    private val client = OkHttpClient()

    override suspend fun getLatestPrice(symbol: String): Result<Double> {
        return try {
            if (symbol.endsWith(".TW") || symbol.endsWith(".TWO")) {
                val yahooPrice = fetchYahooFinancePrice(symbol)
                if (yahooPrice > 0.0) return Result.success(yahooPrice)
            }
            
            val response = api.getGlobalQuote(symbol)
            val price = response.currentPrice
            if (price > 0.0) {
                Result.success(price)
            } else {
                val fallbackPrice = fetchYahooFinancePrice(symbol)
                if (fallbackPrice > 0.0) Result.success(fallbackPrice)
                else Result.failure(Exception("Invalid response or missing price for $symbol"))
            }
        } catch (e: Exception) {
            val fallbackPrice = fetchYahooFinancePrice(symbol)
            if (fallbackPrice > 0.0) Result.success(fallbackPrice)
            else Result.failure(e)
        }
    }

    private suspend fun fetchYahooFinancePrice(symbol: String): Double = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://query2.finance.yahoo.com/v8/finance/chart/$symbol")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string() ?: return@withContext 0.0
                val json = JSONObject(jsonStr)
                val chart = json.getJSONObject("chart")
                val result = chart.getJSONArray("result").getJSONObject(0)
                val meta = result.getJSONObject("meta")
                return@withContext meta.getDouble("regularMarketPrice")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext 0.0
    }

    override suspend fun fetchHistoricalPrices(symbol: String, range: String, interval: String): Result<List<Pair<Long, Double>>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://query2.finance.yahoo.com/v8/finance/chart/$symbol?interval=$interval&range=$range")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                val json = JSONObject(jsonStr)
                val chart = json.getJSONObject("chart")
                val result = chart.getJSONArray("result").getJSONObject(0)
                val timestamps = result.getJSONArray("timestamp")
                val quotes = result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0)
                val closePrices = quotes.getJSONArray("close")
                
                val list = mutableListOf<Pair<Long, Double>>()
                for (i in 0 until timestamps.length()) {
                    if (!closePrices.isNull(i)) {
                        list.add(Pair(timestamps.getLong(i) * 1000L, closePrices.getDouble(i)))
                    }
                }
                return@withContext Result.success(list)
            } else {
                return@withContext Result.failure(Exception("Failed to fetch historical prices"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    override suspend fun searchStocks(query: String): Result<List<FinnhubSearchResult>> {
        return try {
            val response = api.searchStocks(query)
            Result.success(response.result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExchangeRates(): Result<Map<String, Double>> {
        return try {
            val response = api.getExchangeRates()
            if (response.result == "success") {
                Result.success(response.conversion_rates)
            } else {
                Result.failure(Exception("Failed to fetch exchange rates"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
