package com.example.money2.data.local.dao

import androidx.room.*
import com.example.money2.data.local.entity.HoldingEntity
import com.example.money2.data.local.entity.HoldingTransactionEntity
import com.example.money2.data.local.entity.HoldingWithTransactions
import kotlinx.coroutines.flow.Flow

@Dao
interface HoldingDao {
    @Transaction
    @Query("SELECT * FROM holdings ORDER BY symbol ASC")
    fun getAllHoldingsWithTransactions(): Flow<List<HoldingWithTransactions>>

    @Transaction
    @Query("SELECT * FROM holdings WHERE assetType = :type ORDER BY symbol ASC")
    fun getHoldingsWithTransactionsByType(type: String): Flow<List<HoldingWithTransactions>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingEntity)

    @Update
    suspend fun updateHolding(holding: HoldingEntity)

    @Delete
    suspend fun deleteHolding(holding: HoldingEntity)

    @Query("SELECT * FROM holdings WHERE symbol = :symbol LIMIT 1")
    suspend fun getHoldingBySymbol(symbol: String): HoldingEntity?

    @Transaction
    @Query("SELECT * FROM holdings WHERE symbol = :symbol")
    suspend fun getHoldingWithTransactionsBySymbol(symbol: String): HoldingWithTransactions?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoldingTransaction(transaction: HoldingTransactionEntity): Long

    @Query("SELECT * FROM holding_transactions WHERE symbol = :symbol ORDER BY dateMillis DESC")
    fun getTransactionsBySymbol(symbol: String): Flow<List<HoldingTransactionEntity>>
}
