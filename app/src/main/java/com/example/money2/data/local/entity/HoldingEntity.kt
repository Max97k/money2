package com.example.money2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey
    val symbol: String,
    val name: String,
    val currentPrice: Double = 0.0,
    val previousClosePrice: Double = 0.0,
    val assetType: String   // "STOCK" | "ETF" | "CRYPTO" | "FUND" | "OTHER"
)
