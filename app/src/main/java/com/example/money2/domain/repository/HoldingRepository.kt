package com.example.money2.domain.repository

import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import kotlinx.coroutines.flow.Flow

interface HoldingRepository {
    fun getAllHoldings(): Flow<List<Holding>>
    fun getHoldingsByType(type: AssetType): Flow<List<Holding>>
    suspend fun getHoldingById(id: Long): Holding?
    suspend fun insertHolding(holding: Holding): Long
    suspend fun updateHolding(holding: Holding)
    suspend fun deleteHolding(holding: Holding)
}
