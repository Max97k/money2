package com.example.money2.domain.usecase

import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import com.example.money2.domain.model.HoldingTransaction
import com.example.money2.domain.repository.HoldingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDashboardStatsUseCaseTest {

    private class FakeHoldingRepository(private val holdings: List<Holding>) : HoldingRepository {
        override fun getAllHoldings(): Flow<List<Holding>> = flowOf(holdings)

        override fun getHoldingsByType(type: AssetType): Flow<List<Holding>> = flowOf(emptyList())

        override suspend fun getHoldingBySymbol(symbol: String): Holding? = null

        override suspend fun insertHolding(holding: Holding) {}

        override suspend fun updateHolding(holding: Holding) {}

        override suspend fun deleteHolding(holding: Holding) {}

        override fun getTransactions(symbol: String): Flow<List<HoldingTransaction>> = flowOf(emptyList())

        override suspend fun addTransaction(
            symbol: String,
            name: String,
            type: String,
            quantity: Double,
            price: Double,
            assetType: AssetType,
            dateMillis: Long
        ) {}
    }

    @Test
    fun `invoke with empty holdings returns zero stats`() = runTest {
        val repository = FakeHoldingRepository(emptyList())
        val useCase = GetDashboardStatsUseCase(repository)

        val stats = useCase().first()

        assertEquals(0.0, stats.totalValue, 0.0)
        assertEquals(0.0, stats.todayPnl, 0.0)
        assertEquals(0.0, stats.totalPnl, 0.0)
    }

    @Test
    fun `invoke with single holding calculates correctly`() = runTest {
        val holding = Holding(
            symbol = "AAPL",
            name = "Apple Inc.",
            totalQuantity = 10.0,
            avgCost = 150.0,
            currentPrice = 160.0,
            previousClosePrice = 155.0,
            assetType = AssetType.STOCK
        )
        val repository = FakeHoldingRepository(listOf(holding))
        val useCase = GetDashboardStatsUseCase(repository)

        val stats = useCase().first()

        // Total Value = 10.0 * 160.0 = 1600.0
        assertEquals(1600.0, stats.totalValue, 0.0)
        // Today PnL = (160.0 - 155.0) * 10.0 = 50.0
        assertEquals(50.0, stats.todayPnl, 0.0)
        // Total PnL = (160.0 - 150.0) * 10.0 = 100.0
        assertEquals(100.0, stats.totalPnl, 0.0)
    }

    @Test
    fun `invoke with multiple holdings calculates correctly`() = runTest {
        val holding1 = Holding(
            symbol = "AAPL",
            name = "Apple Inc.",
            totalQuantity = 10.0,
            avgCost = 150.0,
            currentPrice = 160.0,
            previousClosePrice = 155.0,
            assetType = AssetType.STOCK
        )
        val holding2 = Holding(
            symbol = "BTC",
            name = "Bitcoin",
            totalQuantity = 2.0,
            avgCost = 30000.0,
            currentPrice = 32000.0,
            previousClosePrice = 31000.0,
            assetType = AssetType.CRYPTO
        )
        val repository = FakeHoldingRepository(listOf(holding1, holding2))
        val useCase = GetDashboardStatsUseCase(repository)

        val stats = useCase().first()

        // Total Value = (10.0 * 160.0) + (2.0 * 32000.0) = 1600.0 + 64000.0 = 65600.0
        assertEquals(65600.0, stats.totalValue, 0.0)
        // Today PnL = ((160.0 - 155.0) * 10.0) + ((32000.0 - 31000.0) * 2.0) = 50.0 + 2000.0 = 2050.0
        assertEquals(2050.0, stats.todayPnl, 0.0)
        // Total PnL = ((160.0 - 150.0) * 10.0) + ((32000.0 - 30000.0) * 2.0) = 100.0 + 4000.0 = 4100.0
        assertEquals(4100.0, stats.totalPnl, 0.0)
    }
}
