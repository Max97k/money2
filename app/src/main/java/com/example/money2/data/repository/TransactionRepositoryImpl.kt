package com.example.money2.data.repository

import com.example.money2.data.local.dao.TransactionDao
import com.example.money2.data.local.mapper.toDomain
import com.example.money2.data.local.mapper.toEntity
import com.example.money2.domain.model.Transaction
import com.example.money2.domain.model.TransactionType
import com.example.money2.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        dao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> =
        dao.getTransactionsByType(type.name).map { list -> list.map { it.toDomain() } }

    override fun getTotalIncome(): Flow<Double> =
        dao.getTotalIncome().map { it ?: 0.0 }

    override fun getTotalExpense(): Flow<Double> =
        dao.getTotalExpense().map { it ?: 0.0 }

    override fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        dao.getTransactionsByDateRange(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override suspend fun insertTransaction(transaction: Transaction): Long =
        dao.insertTransaction(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction) =
        dao.updateTransaction(transaction.toEntity())

    override suspend fun deleteTransaction(transaction: Transaction) =
        dao.deleteTransaction(transaction.toEntity())
}
