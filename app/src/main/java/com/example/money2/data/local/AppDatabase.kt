package com.example.money2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.money2.data.local.dao.HoldingDao
import com.example.money2.data.local.entity.HoldingEntity
import com.example.money2.data.local.entity.HoldingTransactionEntity

@Database(
    entities = [HoldingEntity::class, HoldingTransactionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun holdingDao(): HoldingDao

    companion object {
        const val DATABASE_NAME = "money_db"
    }
}
