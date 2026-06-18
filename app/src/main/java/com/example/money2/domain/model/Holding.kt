package com.example.money2.domain.model

data class Holding(
    val id: Long = 0,
    val symbol: String,       // e.g. "AAPL", "BTC"
    val name: String,
    val quantity: Double,
    val avgCost: Double,      // 平均成本
    val currentPrice: Double = 0.0,
    val assetType: AssetType
)

enum class AssetType { STOCK, ETF, CRYPTO, FUND, OTHER }
