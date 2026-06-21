package com.example.money2.di

import androidx.room.Room
import com.example.money2.BuildConfig
import com.example.money2.data.local.AppDatabase
import com.example.money2.data.local.prefs.EncryptedPrefs
import com.example.money2.data.remote.api.MarketApi
import com.example.money2.data.repository.HoldingRepositoryImpl
import com.example.money2.data.repository.MarketRepositoryImpl
import com.example.money2.data.repository.TransactionRepositoryImpl
import com.example.money2.domain.repository.HoldingRepository
import com.example.money2.domain.repository.MarketRepository
import com.example.money2.domain.repository.TransactionRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.money2.domain.usecase.AddHoldingUseCase
import com.example.money2.domain.usecase.AddTransactionUseCase
import com.example.money2.domain.usecase.DeleteTransactionUseCase
import com.example.money2.domain.usecase.GetDashboardStatsUseCase
import com.example.money2.domain.usecase.GetHoldingsUseCase
import com.example.money2.domain.usecase.GetTransactionsUseCase
import com.example.money2.presentation.dashboard.DashboardViewModel

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
        .build()
    }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().holdingDao() }
}

val repositoryModule = module {
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<HoldingRepository> { HoldingRepositoryImpl(get()) }
}

val prefsModule = module {
    single { EncryptedPrefs(androidContext()) }
}

val networkModule = module {
    single {
        val builder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }
        builder.build()
    }
    
    single {
        Retrofit.Builder()
            .baseUrl("https://wealth-manager-proxy.4r099015.workers.dev/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarketApi::class.java)
    }
    
    single<MarketRepository> { MarketRepositoryImpl(get(), get()) }
}

val useCaseModule = module {
    single { GetTransactionsUseCase(get()) }
    single { AddTransactionUseCase(get()) }
    single { DeleteTransactionUseCase(get()) }
    single { GetDashboardStatsUseCase(get(), get<EncryptedPrefs>().selectedCurrencyFlow, get<EncryptedPrefs>().exchangeRateFlow) }
    
    single { GetHoldingsUseCase(get()) }
    single { AddHoldingUseCase(get()) }
}

val viewModelModule = module {
    viewModel { DashboardViewModel(get(), get(), get(), get(), get()) }
    viewModel { com.example.money2.presentation.transactions.TransactionsViewModel(get(), get(), get()) }
    viewModel { com.example.money2.presentation.holdings.HoldingsViewModel(get(), get(), get(), get()) }
    viewModel { com.example.money2.presentation.holdings.detail.HoldingDetailViewModel(get(), get()) }
    viewModel { com.example.money2.presentation.settings.SettingsViewModel(get(), get()) }
}

val appModule = listOf(databaseModule, repositoryModule, useCaseModule, prefsModule, networkModule, viewModelModule)
