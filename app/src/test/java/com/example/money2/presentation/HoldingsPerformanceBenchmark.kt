package com.example.money2.presentation

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.system.measureTimeMillis

class HoldingsPerformanceBenchmark {

    suspend fun getLatestPrice(symbol: String): Result<Double> {
        delay(100) // simulate 100ms network latency
        return Result.success(100.0)
    }

    suspend fun updateHolding(symbol: String, price: Double) {
        delay(10) // simulate 10ms DB latency
    }

    @Test
    fun benchmarkSequentialVsConcurrent() = runBlocking {
        val holdings = (1..10).map { "SYM$it" }

        // Baseline: Sequential (N+1 problem)
        val sequentialTime = measureTimeMillis {
            holdings.forEach { symbol ->
                val result = getLatestPrice(symbol)
                result.onSuccess { price ->
                    updateHolding(symbol, price)
                }
            }
        }

        // Optimized: Concurrent using async/awaitAll
        val concurrentTime = measureTimeMillis {
            holdings.map { symbol ->
                async {
                    val result = getLatestPrice(symbol)
                    result.onSuccess { price ->
                        updateHolding(symbol, price)
                    }
                }
            }.awaitAll()
        }

        println("MEASUREMENT_RESULT_BASELINE: $sequentialTime ms")
        println("MEASUREMENT_RESULT_OPTIMIZED: $concurrentTime ms")
        println("MEASUREMENT_RESULT_IMPROVEMENT: ${(sequentialTime - concurrentTime)} ms")
    }
}
