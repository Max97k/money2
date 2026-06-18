package com.example.money2.domain.usecase

import com.example.money2.domain.repository.HoldingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DashboardStats(
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double
)

class GetDashboardStatsUseCase(private val repository: HoldingRepository) {
    operator fun invoke(): Flow<DashboardStats> {
        return repository.getAllHoldings().map { holdings ->
            val totalValue = holdings.sumOf { it.quantity * it.currentPrice }
            val totalCost = holdings.sumOf { it.quantity * it.avgCost }
            DashboardStats(
                totalIncome = totalValue,
                totalExpense = totalCost,
                netBalance = totalValue - totalCost
            )
        }
    }
}
