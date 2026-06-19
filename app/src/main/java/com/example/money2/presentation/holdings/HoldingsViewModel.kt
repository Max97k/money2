package com.example.money2.presentation.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.money2.data.remote.dto.FinnhubSearchResult
import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import com.example.money2.domain.repository.HoldingRepository
import com.example.money2.domain.repository.MarketRepository
import com.example.money2.domain.usecase.AddHoldingUseCase
import com.example.money2.domain.usecase.GetHoldingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

import kotlinx.coroutines.delay

class HoldingsViewModel(
    private val getHoldingsUseCase: GetHoldingsUseCase,
    private val addHoldingUseCase: AddHoldingUseCase,
    private val marketRepository: MarketRepository,
    private val holdingRepository: HoldingRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            while (true) {
                delay(10000) // initial delay, or every 10s wait, let's do 60s
                refreshPrices()
                delay(60000)
            }
        }
    }

    val holdings: StateFlow<List<Holding>> = getHoldingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _searchResults = MutableStateFlow<List<FinnhubSearchResult>>(emptyList())
    val searchResults: StateFlow<List<FinnhubSearchResult>> = _searchResults

    fun searchStocks(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            val result = marketRepository.searchStocks(query)
            result.onSuccess { list ->
                _searchResults.value = list
            }
        }
    }

    fun addHolding(symbol: String, name: String, quantity: Double, avgCost: Double, assetType: AssetType, dateMillis: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            try {
                holdingRepository.addTransaction(
                    symbol = symbol.uppercase(),
                    name = name,
                    type = "BUY",
                    quantity = quantity,
                    price = avgCost,
                    assetType = assetType,
                    dateMillis = dateMillis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshPrices() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val currentHoldings = holdings.value

            // Fixed N+1 API call by running getLatestPrice concurrently
            val jobs = currentHoldings.map { holding ->
                launch {
                    val result = marketRepository.getLatestPrice(holding.symbol)
                    result.onSuccess { price ->
                        val updatedHolding = holding.copy(currentPrice = price)
                        holdingRepository.updateHolding(updatedHolding)
                    }
                }
            }

            // Wait for all concurrent price updates to finish
            jobs.forEach { it.join() }

            _isRefreshing.value = false
        }
    }
}
