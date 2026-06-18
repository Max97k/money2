package com.example.money2.domain.usecase

import com.example.money2.domain.model.Holding
import com.example.money2.domain.repository.HoldingRepository
import kotlinx.coroutines.flow.Flow

class GetHoldingsUseCase(private val repository: HoldingRepository) {
    operator fun invoke(): Flow<List<Holding>> {
        return repository.getAllHoldings()
    }
}
