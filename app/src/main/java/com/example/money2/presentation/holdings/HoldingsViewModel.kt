package com.example.money2.presentation.holdings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch

class HoldingsViewModel(
    private val getHoldingsUseCase: GetHoldingsUseCase,
    private val addHoldingUseCase: AddHoldingUseCase,
    private val marketRepository: MarketRepository,
    private val holdingRepository: HoldingRepository
) : ViewModel() {

    val holdings: StateFlow<List<Holding>> = getHoldingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun addHolding(symbol: String, name: String, quantity: Double, avgCost: Double, assetType: AssetType) {
        viewModelScope.launch {
            try {
                addHoldingUseCase(
                    Holding(
                        symbol = symbol.uppercase(),
                        name = name,
                        quantity = quantity,
                        avgCost = avgCost,
                        assetType = assetType
                    )
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
            currentHoldings.forEach { holding ->
                val result = marketRepository.getLatestPrice(holding.symbol)
                result.onSuccess { price ->
                    val updatedHolding = holding.copy(currentPrice = price)
                    holdingRepository.updateHolding(updatedHolding)
                }
            }
            _isRefreshing.value = false
        }
    }
}
