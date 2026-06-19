package com.example.money2.domain.usecase

import com.example.money2.domain.repository.HoldingRepository
import com.example.money2.data.local.prefs.EncryptedPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class DashboardStats(
    val totalValue: Double,
    val todayPnl: Double,
    val totalPnl: Double
)

class GetDashboardStatsUseCase(
    private val repository: HoldingRepository,
    private val prefs: EncryptedPrefs
) {
    operator fun invoke(): Flow<DashboardStats> {
        return combine(
            repository.getAllHoldings(),
            prefs.selectedCurrencyFlow,
            prefs.exchangeRateFlow
        ) { holdings, targetCurrency, exchangeRate ->
            var totalValue = 0.0
            var todayPnl = 0.0
            var totalPnl = 0.0
            
            holdings.forEach { it ->
                val nativeCurrency = if (it.symbol.endsWith(".TW") || it.symbol.endsWith(".TWO")) "TWD" else "USD"
                
                val currentPrice = convertCurrency(it.currentPrice, nativeCurrency, targetCurrency, exchangeRate)
                val prevClose = convertCurrency(it.previousClosePrice, nativeCurrency, targetCurrency, exchangeRate)
                val cost = convertCurrency(it.avgCost, nativeCurrency, targetCurrency, exchangeRate)
                
                totalValue += it.totalQuantity * currentPrice
                todayPnl += (currentPrice - prevClose) * it.totalQuantity
                totalPnl += (currentPrice - cost) * it.totalQuantity
            }
            
            DashboardStats(
                totalValue = totalValue,
                todayPnl = todayPnl,
                totalPnl = totalPnl
            )
        }
    }
    
    private fun convertCurrency(amount: Double, nativeCurrency: String, targetCurrency: String, exchangeRate: Float): Double {
        if (nativeCurrency == targetCurrency) return amount
        return if (nativeCurrency == "USD" && targetCurrency == "TWD") {
            amount * exchangeRate
        } else if (nativeCurrency == "TWD" && targetCurrency == "USD") {
            amount / exchangeRate
        } else {
            amount
        }
    }
}
