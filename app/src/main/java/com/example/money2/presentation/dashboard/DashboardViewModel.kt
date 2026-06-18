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

class DashboardViewModel(
    getDashboardStatsUseCase: GetDashboardStatsUseCase,
    getHoldingsUseCase: GetHoldingsUseCase
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
}
