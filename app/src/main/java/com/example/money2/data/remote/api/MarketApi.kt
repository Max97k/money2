package com.example.money2.data.remote.api

import com.example.money2.BuildConfig
import com.example.money2.data.remote.dto.FinnhubQuoteResponse
import com.example.money2.data.remote.dto.FinnhubSearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MarketApi {
    @GET("finnhub/quote")
    suspend fun getGlobalQuote(
        @Query("symbol") symbol: String,
        @Header("x-proxy-secret") proxySecret: String = BuildConfig.PROXY_SECRET
    ): FinnhubQuoteResponse

    @GET("finnhub/search")
    suspend fun searchStocks(
        @Query("q") query: String,
        @Header("x-proxy-secret") proxySecret: String = BuildConfig.PROXY_SECRET
    ): FinnhubSearchResponse

    @GET("exchangerate/latest/USD")
    suspend fun getExchangeRates(
        @Header("x-proxy-secret") proxySecret: String = BuildConfig.PROXY_SECRET
    ): com.example.money2.data.remote.dto.ExchangeRateResponse
}
