package com.example.money2.domain.usecase

import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import com.example.money2.domain.model.HoldingTransaction
import com.example.money2.domain.repository.HoldingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddHoldingUseCaseTest {

    private lateinit var useCase: AddHoldingUseCase
    private lateinit var fakeRepository: FakeHoldingRepository

    @Before
    fun setUp() {
        fakeRepository = FakeHoldingRepository()
        useCase = AddHoldingUseCase(fakeRepository)
    }

    @Test
    fun `invoke with blank symbol throws exception`() = runBlocking {
        val holding = Holding(
            symbol = "   ",
            name = "Test",
            totalQuantity = 10.0,
            avgCost = 100.0,
            assetType = AssetType.STOCK
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(holding) }
        }

        assertEquals("Symbol cannot be blank", exception.message)
        assertTrue(fakeRepository.insertedHoldings.isEmpty())
    }

    @Test
    fun `invoke with zero quantity throws exception`() = runBlocking {
        val holding = Holding(
            symbol = "AAPL",
            name = "Apple Inc.",
            totalQuantity = 0.0,
            avgCost = 100.0,
            assetType = AssetType.STOCK
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(holding) }
        }

        assertEquals("Quantity must be greater than zero", exception.message)
        assertTrue(fakeRepository.insertedHoldings.isEmpty())
    }

    @Test
    fun `invoke with negative quantity throws exception`() = runBlocking {
        val holding = Holding(
            symbol = "AAPL",
            name = "Apple Inc.",
            totalQuantity = -5.0,
            avgCost = 100.0,
            assetType = AssetType.STOCK
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(holding) }
        }

        assertEquals("Quantity must be greater than zero", exception.message)
        assertTrue(fakeRepository.insertedHoldings.isEmpty())
    }

    @Test
    fun `invoke with valid holding inserts holding`() = runBlocking {
        val holding = Holding(
            symbol = "AAPL",
            name = "Apple Inc.",
            totalQuantity = 10.0,
            avgCost = 150.0,
            assetType = AssetType.STOCK
        )

        useCase(holding)

        assertEquals(1, fakeRepository.insertedHoldings.size)
        assertEquals(holding, fakeRepository.insertedHoldings.first())
    }
}

class FakeHoldingRepository : HoldingRepository {

    val insertedHoldings = mutableListOf<Holding>()

    override fun getAllHoldings(): Flow<List<Holding>> {
        return flowOf(insertedHoldings)
    }

    override fun getHoldingsByType(type: AssetType): Flow<List<Holding>> {
        return flowOf(insertedHoldings.filter { it.assetType == type })
    }

    override suspend fun getHoldingBySymbol(symbol: String): Holding? {
        return insertedHoldings.find { it.symbol == symbol }
    }

    override suspend fun insertHolding(holding: Holding) {
        insertedHoldings.add(holding)
    }

    override suspend fun updateHolding(holding: Holding) {
        val index = insertedHoldings.indexOfFirst { it.symbol == holding.symbol }
        if (index != -1) {
            insertedHoldings[index] = holding
        }
    }

    override suspend fun deleteHolding(holding: Holding) {
        insertedHoldings.removeIf { it.symbol == holding.symbol }
    }

    override fun getTransactions(symbol: String): Flow<List<HoldingTransaction>> {
        return flowOf(emptyList())
    }

    override suspend fun addTransaction(
        symbol: String,
        name: String,
        type: String,
        quantity: Double,
        price: Double,
        assetType: AssetType,
        dateMillis: Long
    ) {
        // No op for this test
    }

    override suspend fun updateTransaction(
        id: Long,
        type: String,
        quantity: Double,
        price: Double,
        dateMillis: Long
    ) {}

    override suspend fun deleteTransaction(id: Long) {}
}
