package com.example.money2.data.repository

import com.example.money2.data.local.dao.HoldingDao
import com.example.money2.data.local.mapper.toDomain
import com.example.money2.data.local.mapper.toEntity
import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import com.example.money2.domain.repository.HoldingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HoldingRepositoryImpl(
    private val dao: HoldingDao
) : HoldingRepository {

    override fun getAllHoldings(): Flow<List<Holding>> =
        dao.getAllHoldings().map { list -> list.map { it.toDomain() } }

    override fun getHoldingsByType(type: AssetType): Flow<List<Holding>> =
        dao.getHoldingsByType(type.name).map { list -> list.map { it.toDomain() } }

    override suspend fun getHoldingById(id: Long): Holding? =
        dao.getHoldingById(id)?.toDomain()

    override suspend fun insertHolding(holding: Holding): Long =
        dao.insertHolding(holding.toEntity())

    override suspend fun updateHolding(holding: Holding) =
        dao.updateHolding(holding.toEntity())

    override suspend fun deleteHolding(holding: Holding) =
        dao.deleteHolding(holding.toEntity())
}
