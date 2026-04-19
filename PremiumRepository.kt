package com.yourapp.premium.data.repository

import com.yourapp.premium.data.local.*
import com.yourapp.premium.data.models.*
import com.yourapp.premium.data.remote.PremiumApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class PremiumRepository @Inject constructor(
    private val premiumDao: PremiumDao,
    private val featureDao: FeatureDao,
    private val subscriptionDao: SubscriptionDao,
    private val purchaseDao: PurchaseDao,
    private val apiService: PremiumApiService
) {

    // Premium Packs
    fun getAllPremiumPacks(): Flow<Result<List<PremiumPack>>> {
        return premiumDao.getAllPremiumPacks()
            .map { packs ->
                Result.success(packs)
            }
            .catch { error ->
                Timber.e(error, "Error fetching premium packs")
                emit(Result.failure(error))
            }
    }

    suspend fun refreshPremiumPacks(): Result<Unit> {
        return try {
            val response = apiService.getAllPremiumPacks()
            if (response.success && response.data != null) {
                premiumDao.insertMultiplePacks(response.data)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing premium packs")
            Result.failure(e)
        }
    }

    fun getPremiumPackById(packId: String): Flow<Result<PremiumPack>> {
        return premiumDao.getPremiumPackById(packId)
            .map { pack ->
                Result.success(pack)
            }
            .catch { error ->
                Timber.e(error, "Error fetching pack by id")
                emit(Result.failure(error))
            }
    }

    // Premium Features
    fun getPremiumFeatures(): Flow<Result<List<PremiumFeature>>> {
        return featureDao.getEnabledFeatures()
            .map { features ->
                Result.success(features)
            }
            .catch { error ->
                Timber.e(error, "Error fetching premium features")
                emit(Result.failure(error))
            }
    }

    suspend fun refreshPremiumFeatures(): Result<Unit> {
        return try {
            val response = apiService.getPremiumFeatures()
            if (response.success && response.data != null) {
                response.data.forEach { feature ->
                    featureDao.insertFeature(feature)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing features")
            Result.failure(e)
        }
    }

    // Subscriptions
    fun getSubscriptionsByPackId(packId: String): Flow<Result<List<SubscriptionTier>>> {
        return subscriptionDao.getSubscriptionsByPackId(packId)
            .map { subscriptions ->
                Result.success(subscriptions)
            }
            .catch { error ->
                Timber.e(error, "Error fetching subscriptions")
                emit(Result.failure(error))
            }
    }

    // Purchases
    suspend fun insertPurchase(purchase: PurchaseItem): Result<Unit> {
        return try {
            purchaseDao.insertPurchase(purchase)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error inserting purchase")
            Result.failure(e)
        }
    }

    fun getActivePurchases(): Flow<Result<List<PurchaseItem>>> {
        return purchaseDao.getActivePurchases()
            .map { purchases ->
                Result.success(purchases)
            }
            .catch { error ->
                Timber.e(error, "Error fetching active purchases")
                emit(Result.failure(error))
            }
    }

    // User Premium Status
    suspend fun getUserPremiumStatus(userId: String): Result<UserPremiumStatus> {
        return try {
            val response = apiService.getUserPremiumStatus(userId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching premium status")
            Result.failure(e)
        }
    }

    // Verify Purchase
    suspend fun verifyPurchase(
        purchaseToken: String,
        productId: String,
        packageName: String,
        userId: String
    ): Result<PurchaseItem> {
        return try {
            val response = apiService.verifyPurchase(
                PurchaseVerificationRequest(
                    purchaseToken = purchaseToken,
                    productId = productId,
                    packageName = packageName,
                    userId = userId
                )
            )
            if (response.success && response.data != null) {
                purchaseDao.insertPurchase(response.data.purchase)
                Result.success(response.data.purchase)
            } else {
                Result.failure(Exception(response.message ?: "Purchase verification failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error verifying purchase")
            Result.failure(e)
        }
    }

    // Activate Premium
    suspend fun activatePremium(
        userId: String,
        purchaseToken: String,
        productId: String,
        deviceName: String,
        deviceId: String
    ): Result<ActivatePremiumResponse> {
        return try {
            val response = apiService.activatePremium(
                userId,
                ActivatePremiumRequest(
                    purchaseToken = purchaseToken,
                    productId = productId,
                    deviceName = deviceName,
                    deviceId = deviceId
                )
            )
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Activation failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error activating premium")
            Result.failure(e)
        }
    }

    // Validate Promo Code
    suspend fun validatePromoCode(code: String): Result<PromoCodeValidation> {
        return try {
            val response = apiService.validatePromoCode(code)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Invalid promo code"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error validating promo code")
            Result.failure(e)
        }
    }

    // Cancel Subscription
    suspend fun cancelSubscription(userId: String): Result<CancelSubscriptionResponse> {
        return try {
            val response = apiService.cancelSubscription(userId)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Cancellation failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling subscription")
            Result.failure(e)
        }
    }
}
