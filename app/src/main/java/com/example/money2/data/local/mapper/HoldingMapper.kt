package com.example.money2.data.local.mapper

import com.example.money2.data.local.entity.HoldingEntity
import com.example.money2.domain.model.AssetType
import com.example.money2.domain.model.Holding

fun HoldingEntity.toDomain() = Holding(
    id = id, symbol = symbol, name = name,
    quantity = quantity, avgCost = avgCost,
    currentPrice = currentPrice,
    assetType = AssetType.valueOf(assetType)
)

fun Holding.toEntity() = HoldingEntity(
    id = id, symbol = symbol, name = name,
    quantity = quantity, avgCost = avgCost,
    currentPrice = currentPrice, assetType = assetType.name
)
