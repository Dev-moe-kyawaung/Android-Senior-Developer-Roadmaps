package com.yourapp.premium.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class BillingClientManager(private val context: Context) : PurchasesUpdatedListener {
    
    private lateinit var billingClient: BillingClient
    
    private val _purchaseState = MutableStateFlow<BillingState>(BillingState.Initializing)
    val purchaseState: StateFlow<BillingState> = _purchaseState

    private val _skuDetails = MutableStateFlow<List<SkuDetails>>(emptyList())
    val skuDetails: StateFlow<List<SkuDetails>> = _skuDetails

    fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing client connected successfully")
                    _purchaseState.value = BillingState.Ready
                    querySkuDetails()
                } else {
                    Timber.e("Billing setup failed: ${billingResult.debugMessage}")
                    _purchaseState.value = BillingState.Error(billingResult.debugMessage)
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.d("Billing service disconnected")
                _purchaseState.value = BillingState.Disconnected
            }
        })
    }

    private fun querySkuDetails() {
        val subscriptionSkuList = listOf(
            "premium_monthly",
            "premium_quarterly",
            "premium_annual",
            "premium_lifetime"
        )

        val params = SkuDetailsParams.newBuilder()
            .setSkusList(subscriptionSkuList)
            .setType(BillingClient.SkuType.SUBS)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _skuDetails.value = skuDetailsList?.toList() ?: emptyList()
                Timber.d("SKU details retrieved: ${skuDetailsList?.size}")
            } else {
                Timber.e("Failed to query SKU details: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchBillingFlow(activity: Activity, skuDetails: SkuDetails) {
        val params = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        billingClient.launchBillingFlow(activity, params)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else {
            Timber.e("Purchase failed: ${billingResult.debugMessage}")
            _purchaseState.value = BillingState.PurchaseFailed(billingResult.debugMessage)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Timber.d("Purchase acknowledged successfully")
                        _purchaseState.value = BillingState.PurchaseSuccessful(purchase)
                    }
                }
            } else {
                _purchaseState.value = BillingState.PurchaseSuccessful(purchase)
            }
        }
    }

    fun queryPurchases(): List<Purchase> {
        val result = billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS) { _, purchases ->
            Timber.d("Active purchases: ${purchases.size}")
        }
        return result.purchasesList?.toList() ?: emptyList()
    }

    fun endConnection() {
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}

sealed class BillingState {
    object Initializing : BillingState()
    object Ready : BillingState()
    object Disconnected : BillingState()
    data class Error(val message: String) : BillingState()
    data class PurchaseSuccessful(val purchase: Purchase) : BillingState()
    data class PurchaseFailed(val message: String) : BillingState()
}
