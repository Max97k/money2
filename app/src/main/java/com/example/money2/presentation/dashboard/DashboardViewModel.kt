package com.example.money2.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.money2.domain.model.Holding
import com.example.money2.domain.model.TimeRange
import com.example.money2.domain.model.ChartPoint
import com.example.money2.domain.model.ChartUiState
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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import com.example.money2.domain.repository.HoldingRepository

data class HistoryCacheKey(val symbol: String, val range: TimeRange)

class DashboardViewModel(
    getDashboardStatsUseCase: GetDashboardStatsUseCase,
    getHoldingsUseCase: GetHoldingsUseCase,
    prefs: EncryptedPrefs,
    private val marketRepository: MarketRepository,
    private val holdingRepository: HoldingRepository
) : ViewModel() {
    val benchmarkSymbol = "^GSPC"

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
        
    val selectedRange = MutableStateFlow(TimeRange.YEAR_1)
    
    fun selectTimeRange(range: TimeRange) {
        selectedRange.value = range
    }
        
    private val historicalDataCache = MutableStateFlow<Map<HistoryCacheKey, List<Pair<Long, Double>>>>(emptyMap())

    init {
        viewModelScope.launch {
            combine(holdings, selectedRange) { hList, range ->
                Pair(hList, range)
            }.collect { (hList, range) ->
                val currentCache = historicalDataCache.value
                val missingKeys = (hList.map { HistoryCacheKey(it.symbol, range) } + HistoryCacheKey(benchmarkSymbol, range))
                    .filter { !currentCache.containsKey(it) }
                    
                if (missingKeys.isNotEmpty()) {
                    val apiRange = when (range) {
                        TimeRange.DAY_1 -> "1d"
                        TimeRange.DAY_5 -> "5d"
                        TimeRange.MONTH_1 -> "1mo"
                        TimeRange.MONTH_6 -> "6mo"
                        TimeRange.YTD -> "ytd"
                        TimeRange.YEAR_1 -> "1y"
                        TimeRange.YEAR_5 -> "5y"
                        TimeRange.MAX -> "max"
                    }
                    val apiInterval = when (range) {
                        TimeRange.DAY_1 -> "5m"
                        TimeRange.DAY_5 -> "15m"
                        TimeRange.MONTH_1, TimeRange.MONTH_6, TimeRange.YTD, TimeRange.YEAR_1 -> "1d"
                        TimeRange.YEAR_5 -> "1wk"
                        TimeRange.MAX -> "1mo"
                    }
                    
                    missingKeys.map { key ->
                        async {
                            val result = marketRepository.fetchHistoricalPrices(key.symbol, apiRange, apiInterval)
                            result.onSuccess { data -> historicalDataCache.update { it + (key to data) } }
                        }
                    }.awaitAll()
                }
            }
        }
        viewModelScope.launch {
            while(true) {
                delay(10000)
                val currentHoldings = holdings.value
                currentHoldings.map { holding ->
                    async {
                        val result = marketRepository.getLatestPrice(holding.symbol)
                        result.onSuccess { price ->
                            holdingRepository.updateHolding(holding.copy(currentPrice = price))
                        }
                    }
                }.awaitAll()
                delay(60000)
            }
        }
    }

    val chartUiState: StateFlow<ChartUiState> = combine(
        getHoldingsUseCase(),
        prefs.selectedCurrencyFlow,
        prefs.exchangeRateFlow,
        historicalDataCache,
        selectedRange
    ) { holdingsList, targetCurrency, exchangeRate, historyCache, range ->
        if (holdingsList.isEmpty()) return@combine ChartUiState(selectedRange = range)
        
        val earliestTx = holdingsList.flatMap { it.transactions }.minByOrNull { it.dateMillis }
        if (earliestTx == null) return@combine ChartUiState(selectedRange = range)
        
        val todayMillis = System.currentTimeMillis()
        val startTimeLimit = when (range) {
            TimeRange.DAY_1 -> todayMillis - 86400000L
            TimeRange.DAY_5 -> todayMillis - 5L * 86400000L
            TimeRange.MONTH_1 -> todayMillis - 30L * 86400000L
            TimeRange.MONTH_6 -> todayMillis - 180L * 86400000L
            TimeRange.YTD -> java.util.Calendar.getInstance().apply { set(java.util.Calendar.MONTH, 0); set(java.util.Calendar.DAY_OF_MONTH, 1) }.timeInMillis
            TimeRange.YEAR_1 -> todayMillis - 365L * 86400000L
            TimeRange.YEAR_5 -> todayMillis - 5L * 365L * 86400000L
            TimeRange.MAX -> 0L
        }
        
        val startMillis = maxOf(startTimeLimit, earliestTx.dateMillis)
        val totalMillis = (todayMillis - startMillis).coerceAtLeast(1)
        val maxPoints = 100 // Sample 100 points
        
        val points = mutableListOf<ChartPoint>()
        val bPoints = mutableListOf<ChartPoint>()
        var baseAssetValue = 0f
        var baseBenchmarkValue = 0f
        
        for (i in 0..maxPoints) {
            val currentMillis = startMillis + (totalMillis * i / maxPoints.toFloat()).toLong()
            
            var totalValueAtDay = 0f
            
            val bHistory = historyCache[HistoryCacheKey(benchmarkSymbol, range)]
            var benchmarkVal = 0.0
            if (bHistory != null && bHistory.isNotEmpty()) {
                val closestData = bHistory.lastOrNull { it.first <= currentMillis }
                if (closestData != null) {
                    benchmarkVal = closestData.second
                } else {
                    benchmarkVal = bHistory.first().second
                }
            }

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
                val history = historyCache[HistoryCacheKey(h.symbol, range)]
                
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
                    val fraction = if (todayMillis > earliestTx.dateMillis) (currentMillis - earliestTx.dateMillis).toFloat() / (todayMillis - earliestTx.dateMillis) else 1f
                    val finalFraction = fraction.coerceIn(0f, 1f)
                    estimatedPrice = avgCost + (h.currentPrice - avgCost) * finalFraction
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
            
            if (i == 0) {
                baseAssetValue = totalValueAtDay
                baseBenchmarkValue = benchmarkVal.toFloat()
            }
            
            val normalizedAssetValue = if (baseAssetValue != 0f) (totalValueAtDay / baseAssetValue) - 1.0f else 0f
            val normalizedBenchmarkValue = if (baseBenchmarkValue != 0f) (benchmarkVal.toFloat() / baseBenchmarkValue) - 1.0f else 0f
            
            points.add(ChartPoint(timestamp = currentMillis, value = totalValueAtDay, normalizedValue = normalizedAssetValue))
            bPoints.add(ChartPoint(timestamp = currentMillis, value = benchmarkVal.toFloat(), normalizedValue = normalizedBenchmarkValue))
        }
        ChartUiState(selectedRange = range, assetPoints = points, benchmarkPoints = bPoints)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChartUiState()
    )
}
