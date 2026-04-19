package com.yourapp.premium.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yourapp.premium.data.models.*
import com.yourapp.premium.utils.Converters

@Database(
    entities = [
        PremiumPack::class,
        PremiumFeature::class,
        SubscriptionTier::class,
        PurchaseItem::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PremiumDatabase : RoomDatabase() {
    abstract fun premiumDao(): PremiumDao
    abstract fun featureDao(): FeatureDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun purchaseDao(): PurchaseDao

    companion object {
        const val DATABASE_NAME = "premium_database.db"
    }
}

// DAOs
package com.yourapp.premium.data.local

import androidx.room.*
import com.yourapp.premium.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PremiumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPremiumPack(pack: PremiumPack)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiplePacks(packs: List<PremiumPack>)

    @Query("SELECT * FROM premium_packs")
    fun getAllPremiumPacks(): Flow<List<PremiumPack>>

    @Query("SELECT * FROM premium_packs WHERE id = :packId")
    fun getPremiumPackById(packId: String): Flow<PremiumPack>

    @Query("SELECT * FROM premium_packs WHERE isPopular = 1")
    fun getPopularPacks(): Flow<List<PremiumPack>>

    @Update
    suspend fun updatePremiumPack(pack: PremiumPack)

    @Delete
    suspend fun deletePremiumPack(pack: PremiumPack)

    @Query("DELETE FROM premium_packs")
    suspend fun deleteAllPacks()
}

@Dao
interface FeatureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeature(feature: PremiumFeature)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleFeatures(features: List<PremiumFeature>)

    @Query("SELECT * FROM premium_features WHERE isEnabled = 1 ORDER BY priority DESC")
    fun getEnabledFeatures(): Flow<List<PremiumFeature>>

    @Query("SELECT * FROM premium_features WHERE category = :category")
    fun getFeaturesByCategory(category: String): Flow<List<PremiumFeature>>

    @Update
    suspend fun updateFeature(feature: PremiumFeature)

    @Delete
    suspend fun deleteFeature(feature: PremiumFeature)
}

@Dao
interface SubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(tier: SubscriptionTier)

    @Query("SELECT * FROM subscription_tiers WHERE packId = :packId")
    fun getSubscriptionsByPackId(packId: String): Flow<List<SubscriptionTier>>

    @Query("SELECT * FROM subscription_tiers WHERE billingCycle = :cycle")
    fun getSubscriptionsByBillingCycle(cycle: String): Flow<List<SubscriptionTier>>

    @Update
    suspend fun updateSubscription(tier: SubscriptionTier)
}

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(item: PurchaseItem)

    @Query("SELECT * FROM purchase_items ORDER BY purchaseTime DESC")
    fun getAllPurchases(): Flow<List<PurchaseItem>>

    @Query("SELECT * FROM purchase_items WHERE userId = :userId ORDER BY purchaseTime DESC")
    fun getUserPurchases(userId: String): Flow<List<PurchaseItem>>

    @Query("SELECT * FROM purchase_items WHERE purchaseState = 'PURCHASED'")
    fun getActivePurchases(): Flow<List<PurchaseItem>>

    @Update
    suspend fun updatePurchase(item: PurchaseItem)
}

