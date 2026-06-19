package com.example.money2.presentation.holdings.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding
import com.example.money2.domain.model.HoldingTransaction
import com.example.money2.domain.repository.HoldingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HoldingDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val holdingRepository: HoldingRepository
) : ViewModel() {

    private val symbol: String = checkNotNull(savedStateHandle["symbol"])

    val holding: StateFlow<Holding?> = flow {
        // Just emitting it or using another way. Actually holding is better passed or re-fetched.
        // Let's refetch it or collect from a flow if available.
        val h = holdingRepository.getHoldingBySymbol(symbol)
        emit(h)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val transactions: StateFlow<List<HoldingTransaction>> = holdingRepository.getTransactions(symbol)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTransaction(type: String, quantity: Double, price: Double, dateMillis: Long) {
        viewModelScope.launch {
            val h = holding.value
            val name = h?.name ?: symbol
            val assetType = h?.assetType ?: AssetType.STOCK
            holdingRepository.addTransaction(symbol, name, type, quantity, price, assetType, dateMillis)
        }
    }

    fun updateTransaction(id: Long, type: String, quantity: Double, price: Double, dateMillis: Long) {
        viewModelScope.launch {
            holdingRepository.updateTransaction(id, type, quantity, price, dateMillis)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            holdingRepository.deleteTransaction(id)
        }
    }
}
