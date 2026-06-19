package com.example.money2.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.money2.domain.model.Holding
import com.example.money2.domain.usecase.DashboardStats
import com.example.money2.domain.usecase.GetDashboardStatsUseCase
import com.example.money2.domain.usecase.GetHoldingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.money2.domain.repository.MarketRepository
import com.example.money2.data.local.prefs.EncryptedPrefs
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

import kotlinx.coroutines.delay
import com.example.money2.domain.repository.HoldingRepository

class DashboardViewModel(
    getDashboardStatsUseCase: GetDashboardStatsUseCase,
    getHoldingsUseCase: GetHoldingsUseCase,
    prefs: EncryptedPrefs,
    private val marketRepository: MarketRepository,
    private val holdingRepository: HoldingRepository
) : ViewModel() {
    val stats: StateFlow<DashboardStats?> = getDashboardStatsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        
    val holdings: StateFlow<List<Holding>> = getHoldingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    private val historicalDataCache = MutableStateFlow<Map<String, List<Pair<Long, Double>>>>(emptyMap())

    init {
        viewModelScope.launch {
            holdings.collect { hList ->
                val currentCache = historicalDataCache.value
                val missingSymbols = hList.map { it.symbol }.filter { !currentCache.containsKey(it) }
                for (symbol in missingSymbols) {
                    val result = marketRepository.fetchHistoricalPrices(symbol, "2y", "1d")
                    result.onSuccess { data ->
                        historicalDataCache.update { it + (symbol to data) }
                    }
                }
            }
        }
        viewModelScope.launch {
            while(true) {
                delay(10000)
                val currentHoldings = holdings.value
                for (holding in currentHoldings) {
                    val result = marketRepository.getLatestPrice(holding.symbol)
                    result.onSuccess { price ->
                        holdingRepository.updateHolding(holding.copy(currentPrice = price))
                    }
                }
                delay(60000)
            }
        }
    }

    val trendPoints: StateFlow<List<Float>> = combine(
        getHoldingsUseCase(),
        prefs.selectedCurrencyFlow,
        prefs.exchangeRateFlow,
        historicalDataCache
    ) { holdingsList, targetCurrency, exchangeRate, historyCache ->
        if (holdingsList.isEmpty()) return@combine emptyList()
        
        // Find earliest transaction
        val earliestTx = holdingsList.flatMap { it.transactions }.minByOrNull { it.dateMillis }
        if (earliestTx == null) return@combine emptyList()
        
        val startDay = earliestTx.dateMillis / 86400000L
        val today = System.currentTimeMillis() / 86400000L
        val totalDays = (today - startDay).toInt().coerceAtLeast(1)
        val maxPoints = 100 // Sample 100 points
        
        val points = mutableListOf<Float>()
        
        for (i in 0..maxPoints) {
            val currentDay = startDay + (totalDays * i / maxPoints.toFloat()).toLong()
            val currentMillis = currentDay * 86400000L
            
            var totalValueAtDay = 0f
            
            for (h in holdingsList) {
                var q = 0.0
                var c = 0.0
                for (tx in h.transactions.sortedBy { it.dateMillis }) {
                    if (tx.dateMillis > currentMillis) break
                    if (tx.type.name == "BUY") {
                        q += tx.quantity
                        c += tx.quantity * tx.price
                    } else if (tx.type.name == "SELL") {
                        val avg = if (q > 0) c / q else 0.0
                        q -= tx.quantity
                        c -= tx.quantity * avg
                    }
                }
                
                val avgCost = if (q > 0) c / q else 0.0
                val history = historyCache[h.symbol]
                
                var estimatedPrice = avgCost
                if (history != null && history.isNotEmpty()) {
                    // Find closest historical price before or at currentMillis
                    val closestData = history.lastOrNull { it.first <= currentMillis }
                    if (closestData != null) {
                        estimatedPrice = closestData.second
                    } else {
                        // If currentMillis is before any historical data, use the first available
                        estimatedPrice = history.first().second
                    }
                } else {
                    val fraction = if (today > startDay) (currentDay - startDay).toFloat() / totalDays else 1f
                    estimatedPrice = avgCost + (h.currentPrice - avgCost) * fraction
                }
                
                val nativeCurrency = if (h.symbol.endsWith(".TW") || h.symbol.endsWith(".TWO")) "TWD" else "USD"
                var finalPrice = estimatedPrice
                if (nativeCurrency != targetCurrency) {
                    finalPrice = if (nativeCurrency == "USD" && targetCurrency == "TWD") {
                        estimatedPrice * exchangeRate
                    } else if (nativeCurrency == "TWD" && targetCurrency == "USD") {
                        estimatedPrice / exchangeRate
                    } else estimatedPrice
                }
                
                totalValueAtDay += (q * finalPrice).toFloat()
            }
            points.add(totalValueAtDay)
        }
        points
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
