package com.example.money2.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.money2.data.local.dao.HoldingDao
import com.example.money2.data.local.dao.TransactionDao
import com.example.money2.data.local.entity.HoldingEntity
import com.example.money2.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, HoldingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun holdingDao(): HoldingDao

    companion object {
        const val DATABASE_NAME = "money_db"
    }
}
