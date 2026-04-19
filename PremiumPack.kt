package com.yourapp.premium.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "premium_packs")
data class PremiumPack(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String = "USD",
    val duration: Int, // in days
    val features: List<String>,
    val discount: Int = 0, // percentage
    val isPopular: Boolean = false,
    val purchaseCount: Int = 0,
    val rating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "premium_features")
data class PremiumFeature(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val icon: String? = null,
    val category: String,
    val isEnabled: Boolean = true,
    val priority: Int = 0
) : Serializable

@Entity(tableName = "subscription_tiers")
data class SubscriptionTier(
    @PrimaryKey
    val tierId: String,
    val tierName: String,
    val packId: String,
    val basePrice: Double,
    val discountedPrice: Double? = null,
    val billingCycle: BillingCycle,
    val trialDays: Int = 0,
    val maxDevices: Int = 1,
    val features: List<String>
) : Serializable

enum class BillingCycle {
    MONTHLY, QUARTERLY, ANNUALLY, LIFETIME
}

@Entity(tableName = "purchase_items")
data class PurchaseItem(
    @PrimaryKey
    val purchaseId: String,
    val skuId: String,
    val orderId: String,
    val packageName: String,
    val purchaseTime: Long,
    val purchaseState: PurchaseState,
    val purchaseToken: String,
    val isAcknowledged: Boolean = false,
    val signature: String? = null,
    val autoRenewing: Boolean = false
) : Serializable

enum class PurchaseState {
    PURCHASED, PENDING, CANCELLED, REFUNDED, EXPIRED
}

data class UserPremiumStatus(
    val userId: String,
    val isPremium: Boolean,
    val packId: String? = null,
    val expirationDate: Long? = null,
    val autoRenew: Boolean = false,
    val deviceCount: Int = 0,
    val purchaseHistory: List<PurchaseItem> = emptyList()
) : Serializable

