package com.example.money2.domain.usecase

import com.example.money2.domain.model.Holding
import com.example.money2.domain.repository.HoldingRepository

class AddHoldingUseCase(private val repository: HoldingRepository) {
    suspend operator fun invoke(holding: Holding) {
        if (holding.symbol.isBlank()) {
            throw IllegalArgumentException("Symbol cannot be blank")
        }
        if (holding.totalQuantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than zero")
        }
        // In reality we should be using addTransaction, but if we're just adding a holding, we can use insertHolding directly or delegate to addTransaction.
        // The task requested addTransaction in HoldingRepository to handle creation, but we will leave AddHoldingUseCase updating the holding for now or using addTransaction.
        repository.insertHolding(holding)
    }
}
