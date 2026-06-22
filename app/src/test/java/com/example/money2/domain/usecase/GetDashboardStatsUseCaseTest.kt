package com.example.money2.domain.usecase

import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import com.example.money2.domain.model.HoldingTransaction
import com.example.money2.domain.repository.HoldingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetDashboardStatsUseCaseTest {

    private class FakeHoldingRepository(private val holdingsFlow: Flow<List<Holding>>) : HoldingRepository {
        constructor(holdings: List<Holding>) : this(flowOf(holdings))

        override fun getAllHoldings(): Flow<List<Holding>> = holdingsFlow

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

        override suspend fun updateTransaction(
            id: Long,
            type: String,
            quantity: Double,
            price: Double,
            dateMillis: Long
        ) {}

        override suspend fun deleteTransaction(id: Long) {}
    }

    @Test
    fun `invoke with empty holdings returns zero stats`() = runTest {
        val repository = FakeHoldingRepository(emptyList())
        val useCase = GetDashboardStatsUseCase(repository, flowOf("USD"), flowOf(1.0f))

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
        val useCase = GetDashboardStatsUseCase(repository, flowOf("USD"), flowOf(1.0f))

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
        val useCase = GetDashboardStatsUseCase(repository, flowOf("USD"), flowOf(1.0f))

        val stats = useCase().first()

        // Total Value = (10.0 * 160.0) + (2.0 * 32000.0) = 1600.0 + 64000.0 = 65600.0
        assertEquals(65600.0, stats.totalValue, 0.0)
        // Today PnL = ((160.0 - 155.0) * 10.0) + ((32000.0 - 31000.0) * 2.0) = 50.0 + 2000.0 = 2050.0
        assertEquals(2050.0, stats.todayPnl, 0.0)
        // Total PnL = ((160.0 - 150.0) * 10.0) + ((32000.0 - 30000.0) * 2.0) = 100.0 + 4000.0 = 4100.0
        assertEquals(4100.0, stats.totalPnl, 0.0)
    }

    @Test
    fun `invoke with USD holding and TWD target converts correctly`() = runTest {
        val holding = Holding(
            symbol = "AAPL", // Native currency: USD
            name = "Apple Inc.",
            totalQuantity = 10.0,
            avgCost = 150.0,
            currentPrice = 160.0,
            previousClosePrice = 155.0,
            assetType = AssetType.STOCK
        )
        val repository = FakeHoldingRepository(listOf(holding))
        // Target: TWD, Exchange Rate: 30.0
        val useCase = GetDashboardStatsUseCase(repository, flowOf("TWD"), flowOf(30.0f))

        val stats = useCase().first()

        // Exchange rate USD -> TWD: 30.0
        // Current Price in TWD = 160.0 * 30.0 = 4800.0
        // Prev Close in TWD = 155.0 * 30.0 = 4650.0
        // Avg Cost in TWD = 150.0 * 30.0 = 4500.0

        // Total Value = 10.0 * 4800.0 = 48000.0
        assertEquals(48000.0, stats.totalValue, 0.0)
        // Today PnL = (4800.0 - 4650.0) * 10.0 = 150.0 * 10.0 = 1500.0
        assertEquals(1500.0, stats.todayPnl, 0.0)
        // Total PnL = (4800.0 - 4500.0) * 10.0 = 300.0 * 10.0 = 3000.0
        assertEquals(3000.0, stats.totalPnl, 0.0)
    }

    @Test
    fun `invoke with TWD holding and USD target converts correctly`() = runTest {
        val holding = Holding(
            symbol = "2330.TW", // Native currency: TWD
            name = "TSMC",
            totalQuantity = 1000.0,
            avgCost = 500.0,
            currentPrice = 600.0,
            previousClosePrice = 580.0,
            assetType = AssetType.STOCK
        )
        val repository = FakeHoldingRepository(listOf(holding))
        // Target: USD, Exchange Rate: 30.0
        val useCase = GetDashboardStatsUseCase(repository, flowOf("USD"), flowOf(30.0f))

        val stats = useCase().first()

        // Exchange rate TWD -> USD: / 30.0
        // Current Price in USD = 600.0 / 30.0 = 20.0
        // Prev Close in USD = 580.0 / 30.0 = 19.333...
        // Avg Cost in USD = 500.0 / 30.0 = 16.666...

        // Total Value = 1000.0 * 20.0 = 20000.0
        assertEquals(20000.0, stats.totalValue, 0.001)
        // Today PnL = (20.0 - (580.0/30.0)) * 1000.0 = (600.0 - 580.0) / 30.0 * 1000.0 = 20.0 / 30.0 * 1000.0 = 666.666...
        assertEquals(666.666, stats.todayPnl, 0.001)
        // Total PnL = (20.0 - (500.0/30.0)) * 1000.0 = (600.0 - 500.0) / 30.0 * 1000.0 = 100.0 / 30.0 * 1000.0 = 3333.333...
        assertEquals(3333.333, stats.totalPnl, 0.001)
    }

    @Test
    fun `invoke with TWD holding and TWD target applies no conversion`() = runTest {
        val holding = Holding(
            symbol = "2330.TW", // Native currency: TWD
            name = "TSMC",
            totalQuantity = 1000.0,
            avgCost = 500.0,
            currentPrice = 600.0,
            previousClosePrice = 580.0,
            assetType = AssetType.STOCK
        )
        val repository = FakeHoldingRepository(listOf(holding))
        // Target: TWD, Exchange Rate: 30.0
        val useCase = GetDashboardStatsUseCase(repository, flowOf("TWD"), flowOf(30.0f))

        val stats = useCase().first()

        // No conversion
        // Total Value = 1000.0 * 600.0 = 600000.0
        assertEquals(600000.0, stats.totalValue, 0.0)
        // Today PnL = (600.0 - 580.0) * 1000.0 = 20.0 * 1000.0 = 20000.0
        assertEquals(20000.0, stats.todayPnl, 0.0)
        // Total PnL = (600.0 - 500.0) * 1000.0 = 100.0 * 1000.0 = 100000.0
        assertEquals(100000.0, stats.totalPnl, 0.0)
    }

    @Test
    fun `invoke reflects selected currency and exchange rate updates`() = runTest {
        val holding = Holding(
            symbol = "AAPL", // Native: USD
            name = "Apple Inc.",
            totalQuantity = 10.0,
            avgCost = 150.0,
            currentPrice = 160.0,
            previousClosePrice = 155.0,
            assetType = AssetType.STOCK
        )
        val repository = FakeHoldingRepository(listOf(holding))

        val currencyFlow = MutableStateFlow("USD")
        val exchangeRateFlow = MutableStateFlow(1.0f)

        val useCase = GetDashboardStatsUseCase(repository, currencyFlow, exchangeRateFlow)

        val results = mutableListOf<DashboardStats>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase().toList(results)
        }

        // Initial state
        assertEquals(1600.0, results.last().totalValue, 0.0)

        // Update currency and exchange rate
        currencyFlow.value = "TWD"
        exchangeRateFlow.value = 30.0f

        // Should emit new stats reflecting the new currency and rate
        assertEquals(48000.0, results.last().totalValue, 0.0)

        job.cancel()
    }

    @Test
    fun `invoke reflects holdings updates`() = runTest {
        val holding = Holding(
            symbol = "AAPL", // Native: USD
            name = "Apple Inc.",
            totalQuantity = 10.0,
            avgCost = 150.0,
            currentPrice = 160.0,
            previousClosePrice = 155.0,
            assetType = AssetType.STOCK
        )

        val holdingsFlow = MutableStateFlow(listOf(holding))
        val repository = FakeHoldingRepository(holdingsFlow)

        val useCase = GetDashboardStatsUseCase(repository, flowOf("USD"), flowOf(1.0f))

        val results = mutableListOf<DashboardStats>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase().toList(results)
        }

        // Initial state
        assertEquals(1600.0, results.last().totalValue, 0.0)

        // Update holdings
        val holding2 = holding.copy(currentPrice = 170.0)
        holdingsFlow.value = listOf(holding2)

        // Should emit new stats reflecting the updated holding
        assertEquals(1700.0, results.last().totalValue, 0.0)

        job.cancel()
    }

    @Test
    fun `invoke with unknown target currency applies no conversion`() = runTest {
        val holding = Holding(
            symbol = "AAPL", // Native currency: USD
            name = "Apple Inc.",
            totalQuantity = 10.0,
            avgCost = 150.0,
            currentPrice = 160.0,
            previousClosePrice = 155.0,
            assetType = AssetType.STOCK
        )
        val repository = FakeHoldingRepository(listOf(holding))
        // Target: EUR, which is not USD or TWD. Exchange rate should be ignored.
        val useCase = GetDashboardStatsUseCase(repository, flowOf("EUR"), flowOf(0.9f))

        val stats = useCase().first()

        // No conversion applied since target is neither TWD nor USD from the native side
        // Total Value = 10.0 * 160.0 = 1600.0
        assertEquals(1600.0, stats.totalValue, 0.0)
        // Today PnL = (160.0 - 155.0) * 10.0 = 50.0
        assertEquals(50.0, stats.todayPnl, 0.0)
        // Total PnL = (160.0 - 150.0) * 10.0 = 100.0
        assertEquals(100.0, stats.totalPnl, 0.0)
    }

    @Test
    fun `invoke with mixed holdings converts to target currency correctly`() = runTest {
        val usHolding = Holding(
            symbol = "AAPL", // Native: USD
            name = "Apple Inc.",
            totalQuantity = 10.0,
            avgCost = 150.0,
            currentPrice = 160.0,
            previousClosePrice = 155.0,
            assetType = AssetType.STOCK
        )
        val twHolding = Holding(
            symbol = "0050.TW", // Native: TWD
            name = "Yuanta Taiwan 50",
            totalQuantity = 1000.0,
            avgCost = 130.0,
            currentPrice = 140.0,
            previousClosePrice = 135.0,
            assetType = AssetType.STOCK
        )
        val repository = FakeHoldingRepository(listOf(usHolding, twHolding))
        // Target: TWD, Exchange Rate: 30.0
        val useCase = GetDashboardStatsUseCase(repository, flowOf("TWD"), flowOf(30.0f))

        val stats = useCase().first()

        // US Holding in TWD:
        // Value = 160 * 30 * 10 = 48000
        // Today PnL = (160 - 155) * 30 * 10 = 1500
        // Total PnL = (160 - 150) * 30 * 10 = 3000

        // TW Holding in TWD (No conversion):
        // Value = 140 * 1000 = 140000
        // Today PnL = (140 - 135) * 1000 = 5000
        // Total PnL = (140 - 130) * 1000 = 10000

        // Mixed:
        assertEquals(48000.0 + 140000.0, stats.totalValue, 0.0)
        assertEquals(1500.0 + 5000.0, stats.todayPnl, 0.0)
        assertEquals(3000.0 + 10000.0, stats.totalPnl, 0.0)
    }
}
