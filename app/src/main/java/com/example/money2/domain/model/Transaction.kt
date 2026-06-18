package com.example.money2.domain.model

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,       // 正數=收入, 負數=支出
    val category: String,
    val type: TransactionType,
    val date: Long,           // timestamp in millis
    val note: String = ""
)

enum class TransactionType { INCOME, EXPENSE }
