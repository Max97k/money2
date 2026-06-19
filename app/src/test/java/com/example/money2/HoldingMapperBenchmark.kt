package com.example.money2

import com.example.money2.data.local.entity.HoldingEntity
import com.example.money2.data.local.entity.HoldingTransactionEntity
import com.example.money2.data.local.entity.HoldingWithTransactions
import com.example.money2.data.local.mapper.toDomain
import org.junit.Test
import kotlin.system.measureTimeMillis

class HoldingMapperBenchmark {

    @Test
    fun benchmarkToDomain() {
        val holding = HoldingEntity(
            symbol = "AAPL",
            name = "Apple",
            currentPrice = 150.0,
            previousClosePrice = 145.0,
            assetType = "STOCK"
        )
        val transactions = (1..10000).map {
            HoldingTransactionEntity(
                id = it.toLong(),
                symbol = "AAPL",
                type = if (it % 2 == 0) "BUY" else "SELL",
                quantity = 10.0,
                price = 100.0 + (it % 50),
                dateMillis = it.toLong()
            )
        }
        val holdingWithTransactions = HoldingWithTransactions(
            holding = holding,
            transactions = transactions
        )

        // warmup
        for (i in 1..100) {
            holdingWithTransactions.toDomain()
        }

        // measure
        val time = measureTimeMillis {
            for (i in 1..1000) {
                holdingWithTransactions.toDomain()
            }
        }
        println("MEASUREMENT_RESULT: Time taken: $time ms")
        assert(time >= 0)
    }
}
