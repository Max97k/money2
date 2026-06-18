package com.example.money2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holding_transactions")
data class HoldingTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val type: String,
    val quantity: Double,
    val price: Double,
    val dateMillis: Long
)
