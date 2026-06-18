package com.example.money2.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class HoldingWithTransactions(
    @Embedded val holding: HoldingEntity,
    @Relation(
        parentColumn = "symbol",
        entityColumn = "symbol"
    )
    val transactions: List<HoldingTransactionEntity>
)
