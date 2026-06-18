package com.example.money2.domain.model

data class HoldingTransaction(
    val id: Long = 0,
    val symbol: String,
    val type: HoldingTransactionType,
    val quantity: Double,
    val price: Double,
    val dateMillis: Long
)

enum class HoldingTransactionType { BUY, SELL }
