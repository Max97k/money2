package com.example.money2.data.repository

import com.example.money2.data.local.dao.HoldingDao
import com.example.money2.data.local.entity.HoldingEntity
import com.example.money2.data.local.entity.HoldingTransactionEntity
import com.example.money2.data.local.mapper.toDomain
import com.example.money2.data.local.mapper.toEntity
import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import com.example.money2.domain.model.HoldingTransaction
import com.example.money2.domain.repository.HoldingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HoldingRepositoryImpl(
    private val dao: HoldingDao
) : HoldingRepository {

    override fun getAllHoldings(): Flow<List<Holding>> =
        dao.getAllHoldingsWithTransactions().map { list -> list.map { it.toDomain() } }

    override fun getHoldingsByType(type: AssetType): Flow<List<Holding>> =
        dao.getHoldingsWithTransactionsByType(type.name).map { list -> list.map { it.toDomain() } }

    override suspend fun getHoldingBySymbol(symbol: String): Holding? =
        dao.getHoldingWithTransactionsBySymbol(symbol)?.toDomain()

    override suspend fun insertHolding(holding: Holding) =
        dao.insertHolding(holding.toEntity())

    override suspend fun updateHolding(holding: Holding) =
        dao.updateHolding(holding.toEntity())

    override suspend fun deleteHolding(holding: Holding) =
        dao.deleteHolding(holding.toEntity())

    override fun getTransactions(symbol: String): Flow<List<HoldingTransaction>> =
        dao.getTransactionsBySymbol(symbol).map { list -> list.map { it.toDomain() } }

    override suspend fun addTransaction(
        symbol: String,
        name: String,
        type: String,
        quantity: Double,
        price: Double,
        assetType: AssetType,
        dateMillis: Long
    ) {
        val existingHolding = dao.getHoldingBySymbol(symbol)
        if (existingHolding == null) {
            dao.insertHolding(
                HoldingEntity(
                    symbol = symbol,
                    name = name,
                    currentPrice = price, // default to purchase price initially
                    previousClosePrice = price,
                    assetType = assetType.name
                )
            )
        }
        dao.insertHoldingTransaction(
            HoldingTransactionEntity(
                symbol = symbol,
                type = type,
                quantity = quantity,
                price = price,
                dateMillis = dateMillis
            )
        )
    }
}
