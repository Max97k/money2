package com.example.money2.data.local.mapper

import com.example.money2.data.local.entity.HoldingEntity
import com.example.money2.data.local.entity.HoldingTransactionEntity
import com.example.money2.data.local.entity.HoldingWithTransactions
import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import com.example.money2.domain.model.HoldingTransaction
import com.example.money2.domain.model.HoldingTransactionType

fun HoldingWithTransactions.toDomain(): Holding {
    var q = 0.0
    var c = 0.0

    var isSorted = true
    var lastDate = Long.MIN_VALUE
    for (i in 0 until transactions.size) {
        val d = transactions[i].dateMillis
        if (d < lastDate) {
            isSorted = false
            break
        }
        lastDate = d
    }

    val mappedTransactions = ArrayList<HoldingTransaction>(transactions.size)

    if (isSorted) {
        for (i in 0 until transactions.size) {
            val t = transactions[i]
            if (t.type == "BUY") {
                q += t.quantity
                c += t.quantity * t.price
            } else if (t.type == "SELL") {
                val avg = if (q > 0) c / q else 0.0
                q -= t.quantity
                c -= t.quantity * avg
            }
            mappedTransactions.add(HoldingTransaction(
                id = t.id,
                symbol = t.symbol,
                type = HoldingTransactionType.valueOf(t.type),
                quantity = t.quantity,
                price = t.price,
                dateMillis = t.dateMillis
            ))
        }
    } else {
        val sortedEntities = transactions.sortedBy { it.dateMillis }
        for (i in 0 until sortedEntities.size) {
            val t = sortedEntities[i]
            if (t.type == "BUY") {
                q += t.quantity
                c += t.quantity * t.price
            } else if (t.type == "SELL") {
                val avg = if (q > 0) c / q else 0.0
                q -= t.quantity
                c -= t.quantity * avg
            }
            mappedTransactions.add(HoldingTransaction(
                id = t.id,
                symbol = t.symbol,
                type = HoldingTransactionType.valueOf(t.type),
                quantity = t.quantity,
                price = t.price,
                dateMillis = t.dateMillis
            ))
        }
    }

    val avgCost = if (q > 0) c / q else 0.0

    return Holding(
        symbol = holding.symbol,
        name = holding.name,
        totalQuantity = q,
        avgCost = avgCost,
        currentPrice = holding.currentPrice,
        previousClosePrice = holding.previousClosePrice,
        assetType = AssetType.valueOf(holding.assetType),
        transactions = mappedTransactions
    )
}

fun HoldingEntity.toDomain() = Holding(
    symbol = symbol, name = name,
    totalQuantity = 0.0, avgCost = 0.0,
    currentPrice = currentPrice,
    previousClosePrice = previousClosePrice,
    assetType = AssetType.valueOf(assetType)
)

fun Holding.toEntity() = HoldingEntity(
    symbol = symbol, name = name,
    currentPrice = currentPrice, 
    previousClosePrice = previousClosePrice,
    assetType = assetType.name
)

fun HoldingTransactionEntity.toDomain() = HoldingTransaction(
    id = id,
    symbol = symbol,
    type = HoldingTransactionType.valueOf(type),
    quantity = quantity,
    price = price,
    dateMillis = dateMillis
)

fun HoldingTransaction.toEntity() = HoldingTransactionEntity(
    id = id,
    symbol = symbol,
    type = type.name,
    quantity = quantity,
    price = price,
    dateMillis = dateMillis
)
