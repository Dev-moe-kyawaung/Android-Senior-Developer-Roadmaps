package com.yourapp.premium.di

import android.content.Context
import androidx.room.Room
import com.yourapp.premium.billing.BillingClientManager
import com.yourapp.premium.data.local.PremiumDatabase
import com.yourapp.premium.data.remote.PremiumApiService
import com.yourapp.premium.data.repository.PremiumRepository
import com.yourapp.premium.utils.EncryptionUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PremiumModule {

    @Singleton
    @Provides
    fun providePremiumDatabase(
        @ApplicationContext context: Context
    ): PremiumDatabase {
        return Room.databaseBuilder(
            context,
            PremiumDatabase::class.java,
            PremiumDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun providePremiumApiService(): PremiumApiService {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.yourapp.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PremiumApiService::class.java)
    }

    @Singleton
    @Provides
    fun providePremiumRepository(
        database: PremiumDatabase,
        apiService: PremiumApiService
    ): PremiumRepository {
        return PremiumRepository(
            premiumDao = database.premiumDao(),
            featureDao = database.featureDao(),
            subscriptionDao = database.subscriptionDao(),
            purchaseDao = database.purchaseDao(),
            apiService = apiService
        )
    }

    @Singleton
    @Provides
    fun provideBillingClientManager(
        @ApplicationContext context: Context
    ): BillingClientManager {
        return BillingClientManager(context)
    }

    @Singleton
    @Provides
    fun provideEncryptionUtil(
        @ApplicationContext context: Context
    ): EncryptionUtil {
        return EncryptionUtil(context)
    }
}

