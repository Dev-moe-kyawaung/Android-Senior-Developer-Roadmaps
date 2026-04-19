package com.yourapp.premium.data.remote

import com.yourapp.premium.data.models.*
import retrofit2.http.*

interface PremiumApiService {
    
    @GET("api/v1/premium-packs")
    suspend fun getAllPremiumPacks(): ApiResponse<List<PremiumPack>>

    @GET("api/v1/premium-packs/{packId}")
    suspend fun getPremiumPackById(@Path("packId") packId: String): ApiResponse<PremiumPack>

    @GET("api/v1/premium-features")
    suspend fun getPremiumFeatures(): ApiResponse<List<PremiumFeature>>

    @GET("api/v1/subscriptions")
    suspend fun getSubscriptionTiers(): ApiResponse<List<SubscriptionTier>>

    @POST("api/v1/purchases")
    suspend fun verifyPurchase(
        @Body purchaseData: PurchaseVerificationRequest
    ): ApiResponse<PurchaseVerificationResponse>

    @GET("api/v1/users/{userId}/premium-status")
    suspend fun getUserPremiumStatus(
        @Path("userId") userId: String
    ): ApiResponse<UserPremiumStatus>

    @POST("api/v1/users/{userId}/activate-premium")
    suspend fun activatePremium(
        @Path("userId") userId: String,
        @Body request: ActivatePremiumRequest
    ): ApiResponse<ActivatePremiumResponse>

    @POST("api/v1/users/{userId}/cancel-subscription")
    suspend fun cancelSubscription(
        @Path("userId") userId: String
    ): ApiResponse<CancelSubscriptionResponse>

    @GET("api/v1/promotional-codes/{code}")
    suspend fun validatePromoCode(
        @Path("code") code: String
    ): ApiResponse<PromoCodeValidation>

    @POST("api/v1/refund-request")
    suspend fun requestRefund(
        @Body refundRequest: RefundRequest
    ): ApiResponse<RefundResponse>
}

// Request/Response Models
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class PurchaseVerificationRequest(
    val purchaseToken: String,
    val productId: String,
    val packageName: String,
    val userId: String
)

data class PurchaseVerificationResponse(
    val isValid: Boolean,
    val purchase: PurchaseItem,
    val expiryDate: Long? = null
)

data class ActivatePremiumRequest(
    val purchaseToken: String,
    val productId: String,
    val deviceName: String,
    val deviceId: String
)

data class ActivatePremiumResponse(
    val activationId: String,
    val expiryDate: Long,
    val autoRenew: Boolean,
    val features: List<String>
)

data class CancelSubscriptionResponse(
    val cancellationId: String,
    val refundAmount: Double,
    val cancellationDate: Long,
    val message: String
)

data class PromoCodeValidation(
    val isValid: Boolean,
    val discountPercentage: Int,
    val maxUsage: Int,
    val currentUsage: Int,
    val expiryDate: Long
)

data class RefundRequest(
    val purchaseId: String,
    val reason: String,
    val userId: String
)

data class RefundResponse(
    val refundId: String,
    val amount: Double,
    val status: String,
    val estimatedDate: Long
)
