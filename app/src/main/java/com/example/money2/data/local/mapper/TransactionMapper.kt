package com.example.money2.data.local.mapper

import com.example.money2.data.local.entity.TransactionEntity
import com.example.money2.domain.model.Transaction
import com.example.money2.domain.model.TransactionType

fun TransactionEntity.toDomain() = Transaction(
    id = id, title = title, amount = amount,
    category = category,
    type = TransactionType.valueOf(type),
    date = date, note = note
)

fun Transaction.toEntity() = TransactionEntity(
    id = id, title = title, amount = amount,
    category = category, type = type.name,
    date = date, note = note
)
