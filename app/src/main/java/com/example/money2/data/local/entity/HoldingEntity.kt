package com.example.money2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val name: String,
    val quantity: Double,
    val avgCost: Double,
    val currentPrice: Double = 0.0,
    val assetType: String   // "STOCK" | "ETF" | "CRYPTO" | "FUND" | "OTHER"
)
