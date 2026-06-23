package com.example.money2.domain.model

enum class TimeRange {
    DAY_1, DAY_5, MONTH_1, MONTH_6, YTD, YEAR_1, YEAR_5, MAX
}

data class ChartPoint(val timestamp: Long, val value: Float, val normalizedValue: Float = 0f)

data class ChartUiState(
    val selectedRange: TimeRange = TimeRange.YEAR_1,
    val assetPoints: List<ChartPoint> = emptyList(),
    val benchmarkPoints: List<ChartPoint>? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
