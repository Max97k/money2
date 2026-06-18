package com.example.money2.domain.usecase

import com.example.money2.domain.model.Holding
import com.example.money2.domain.repository.HoldingRepository

class AddHoldingUseCase(private val repository: HoldingRepository) {
    suspend operator fun invoke(holding: Holding) {
        if (holding.symbol.isBlank()) {
            throw IllegalArgumentException("Symbol cannot be blank")
        }
        if (holding.quantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than zero")
        }
        repository.insertHolding(holding)
    }
}
