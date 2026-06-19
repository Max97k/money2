package com.example.money2.domain.repository

import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import com.example.money2.domain.model.HoldingTransaction
import kotlinx.coroutines.flow.Flow

interface HoldingRepository {
    fun getAllHoldings(): Flow<List<Holding>>
    fun getHoldingsByType(type: AssetType): Flow<List<Holding>>
    suspend fun getHoldingBySymbol(symbol: String): Holding?
    suspend fun insertHolding(holding: Holding)
    suspend fun updateHolding(holding: Holding)
    suspend fun deleteHolding(holding: Holding)
    
    fun getTransactions(symbol: String): Flow<List<HoldingTransaction>>
    suspend fun addTransaction(symbol: String, name: String, type: String, quantity: Double, price: Double, assetType: AssetType, dateMillis: Long = System.currentTimeMillis())
    suspend fun updateTransaction(id: Long, type: String, quantity: Double, price: Double, dateMillis: Long)
    suspend fun deleteTransaction(id: Long)
}
