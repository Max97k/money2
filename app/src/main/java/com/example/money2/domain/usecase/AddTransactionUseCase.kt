package com.example.money2.domain.usecase

import com.example.money2.domain.model.Transaction
import com.example.money2.domain.repository.TransactionRepository

class AddTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) {
        if (transaction.title.isBlank()) {
            throw IllegalArgumentException("Title cannot be blank")
        }
        if (transaction.amount <= 0) {
            throw IllegalArgumentException("Amount must be greater than zero")
        }
        repository.insertTransaction(transaction)
    }
}
