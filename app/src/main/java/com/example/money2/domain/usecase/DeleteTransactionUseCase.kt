package com.example.money2.domain.usecase

import com.example.money2.domain.model.Transaction
import com.example.money2.domain.repository.TransactionRepository

class DeleteTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }
}
