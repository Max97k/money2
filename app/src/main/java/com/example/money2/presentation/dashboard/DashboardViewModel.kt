package com.example.money2.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.money2.domain.usecase.DashboardStats
import com.example.money2.domain.usecase.GetDashboardStatsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    getDashboardStatsUseCase: GetDashboardStatsUseCase
) : ViewModel() {
    val stats: StateFlow<DashboardStats?> = getDashboardStatsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
