package com.example.money2.domain.usecase

import com.example.money2.domain.repository.HoldingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DashboardStats(
    val totalValue: Double,
    val todayPnl: Double,
    val totalPnl: Double
)

class GetDashboardStatsUseCase(private val repository: HoldingRepository) {
    operator fun invoke(): Flow<DashboardStats> {
        return repository.getAllHoldings().map { holdings ->
            val totalValue = holdings.sumOf { it.totalQuantity * it.currentPrice }
            val todayPnl = holdings.sumOf { (it.currentPrice - it.previousClosePrice) * it.totalQuantity }
            val totalPnl = holdings.sumOf { (it.currentPrice - it.avgCost) * it.totalQuantity }
            
            DashboardStats(
                totalValue = totalValue,
                todayPnl = todayPnl,
                totalPnl = totalPnl
            )
        }
    }
}
