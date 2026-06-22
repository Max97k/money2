package com.example.money2.presentation.holdings

import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import com.example.money2.domain.model.HoldingTransaction
import com.example.money2.domain.repository.HoldingRepository
import com.example.money2.domain.repository.MarketRepository
import com.example.money2.domain.usecase.AddHoldingUseCase
import com.example.money2.domain.usecase.GetHoldingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.delay
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.runBlocking
import com.example.money2.data.remote.dto.FinnhubSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Before
import org.junit.After

class FakeMarketRepository : MarketRepository {
    override suspend fun getLatestPrice(symbol: String): Result<Double> {
        delay(100) // Simulate network delay
        return Result.success(150.0)
    }
    override suspend fun searchStocks(query: String) = Result.success(emptyList<FinnhubSearchResult>())
    override suspend fun getExchangeRates() = Result.success(emptyMap<String, Double>())
    override suspend fun fetchHistoricalPrices(symbol: String, range: String, interval: String) = Result.success(emptyList<Pair<Long, Double>>())
}

class FakeHoldingRepository(private val holdings: List<Holding>) : HoldingRepository {
    override fun getAllHoldings() = flowOf(holdings)
    override fun getHoldingsByType(type: AssetType) = flowOf(emptyList<Holding>())
    override suspend fun getHoldingBySymbol(symbol: String) = null
    override suspend fun insertHolding(holding: Holding) {}
    override suspend fun updateHolding(holding: Holding) {
        delay(10) // Simulate DB delay
    }
    override suspend fun deleteHolding(holding: Holding) {}
    override fun getTransactions(symbol: String) = flowOf(emptyList<HoldingTransaction>())
    override suspend fun addTransaction(symbol: String, name: String, type: String, quantity: Double, price: Double, assetType: AssetType, dateMillis: Long) {}
    override suspend fun updateTransaction(id: Long, type: String, quantity: Double, price: Double, dateMillis: Long) {}
    override suspend fun deleteTransaction(id: Long) {}
}

class HoldingsViewModelBenchmark {
    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun benchmarkRefreshPrices() = runBlocking {
        val numHoldings = 10
        val holdings = (1..numHoldings).map {
            Holding(
                symbol = "SYM$it",
                name = "Symbol $it",
                totalQuantity = 10.0,
                avgCost = 100.0,
                assetType = AssetType.STOCK
            )
        }

        val getHoldingsUseCase = GetHoldingsUseCase(FakeHoldingRepository(holdings))
        val addHoldingUseCase = AddHoldingUseCase(FakeHoldingRepository(holdings))
        val marketRepository = FakeMarketRepository()
        val holdingRepository = FakeHoldingRepository(holdings)

        val viewModel = HoldingsViewModel(
            getHoldingsUseCase,
            addHoldingUseCase,
            marketRepository,
            holdingRepository
        )

        delay(100) // Wait for Flow to initialize

        val time = measureTimeMillis {
            viewModel.refreshPrices()
            // Wait for it to finish
            while (viewModel.isRefreshing.value) {
                delay(10)
            }
        }
        println("MEASUREMENT_RESULT: Refresh time for $numHoldings holdings: $time ms")
    }
}
