package com.example.money2.domain.usecase

import com.example.money2.domain.model.Holding
import com.example.money2.domain.repository.HoldingRepository

class AddHoldingUseCase(private val repository: HoldingRepository) {
    suspend operator fun invoke(holding: Holding) {
        if (holding.symbol.isBlank()) {
            throw IllegalArgumentException("Symbol cannot be blank")
        }
        if (holding.name.isBlank()) {
            throw IllegalArgumentException("Name cannot be blank")
        }
        if (holding.totalQuantity.isNaN() || holding.totalQuantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than zero")
        }
        if (holding.avgCost.isNaN() || holding.avgCost < 0) {
            throw IllegalArgumentException("Average cost cannot be negative")
        }
        repository.insertHolding(holding)
    }
}
