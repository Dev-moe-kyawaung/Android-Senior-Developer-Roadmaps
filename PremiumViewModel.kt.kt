package com.yourapp.premium.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.premium.data.models.*
import com.yourapp.premium.data.repository.PremiumRepository
import com.yourapp.premium.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val getPremiumPackUseCase: GetPremiumPackUseCase,
    private val checkPremiumStatusUseCase: CheckPremiumStatusUseCase,
    private val purchaseItemUseCase: PurchaseItemUseCase
) : ViewModel() {

    private val _premiumPacks = MutableStateFlow<UiState<List<PremiumPack>>>(UiState.Loading)
    val premiumPacks: StateFlow<UiState<List<PremiumPack>>> = _premiumPacks.asStateFlow()

    private val _selectedPack = MutableStateFlow<PremiumPack?>(null)
    val selectedPack: StateFlow<PremiumPack?> = _selectedPack.asStateFlow()

    private val _premiumFeatures = MutableStateFlow<UiState<List<PremiumFeature>>>(UiState.Loading)
    val premiumFeatures: StateFlow<UiState<List<PremiumFeature>>> = _premiumFeatures.asStateFlow()

    private val _userPremiumStatus = MutableStateFlow<UserPremiumStatus?>(null)
    val userPremiumStatus: StateFlow<UserPremiumStatus?> = _userPremiumStatus.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseUiState>(PurchaseUiState.Idle)
    val purchaseState: StateFlow<PurchaseUiState> = _purchaseState.asStateFlow()

    private val _subscriptionTiers = MutableStateFlow<UiState<List<SubscriptionTier>>>(UiState.Loading)
    val subscriptionTiers: StateFlow<UiState<List<SubscriptionTier>>> = _subscriptionTiers.asStateFlow()

    init {
        loadPremiumPacks()
        loadPremiumFeatures()
    }

    fun loadPremiumPacks() {
        _premiumPacks.value = UiState.Loading
        viewModelScope.launch {
            premiumRepository.refreshPremiumPacks()
            premiumRepository.getAllPremiumPacks()
                .collect { result ->
                    _premiumPacks.value = result.fold(
                        onSuccess = { UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "Unknown error") }
                    )
                }
        }
    }

    fun loadPremiumFeatures() {
        _premiumFeatures.value = UiState.Loading
        viewModelScope.launch {
            premiumRepository.refreshPremiumFeatures()
            premiumRepository.getPremiumFeatures()
                .collect { result ->
                    _premiumFeatures.value = result.fold(
                        onSuccess = { UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "Unknown error") }
                    )
                }
        }
    }

    fun selectPremiumPack(pack: PremiumPack) {
        _selectedPack.value = pack
        loadSubscriptionTiers(pack.id)
    }

    fun loadSubscriptionTiers(packId: String) {
        _subscriptionTiers.value = UiState.Loading
        viewModelScope.launch {
            premiumRepository.getSubscriptionsByPackId(packId)
                .collect { result ->
                    _subscriptionTiers.value = result.fold(
                        onSuccess = { UiState.Success(it) },
                        onFailure = { UiState.Error(it.message ?: "Unknown error") }
                    )
                }
        }
    }

    fun checkPremiumStatus(userId: String) {
        viewModelScope.launch {
            val result = premiumRepository.getUserPremiumStatus(userId)
            result.fold(
                onSuccess = { status ->
                    _userPremiumStatus.value = status
                },
                onFailure = { error ->
                    Timber.e(error, "Error checking premium status")
                }
            )
        }
    }

    fun processPurchase(purchaseToken: String, productId: String, userId: String) {
        _purchaseState.value = PurchaseUiState.Processing
        viewModelScope.launch {
            val result = premiumRepository.verifyPurchase(
                purchaseToken = purchaseToken,
                productId = productId,
                packageName = "com.yourapp",
                userId = userId
            )
            result.fold(
                onSuccess = { purchase ->
                    activatePremium(userId, purchaseToken, productId)
                },
                onFailure = { error ->
                    _purchaseState.value = PurchaseUiState.Error(error.message ?: "Purchase failed")
                }
            )
        }
    }

    private fun activatePremium(userId: String, purchaseToken: String, productId: String) {
        viewModelScope.launch {
            val result = premiumRepository.activatePremium(
                userId = userId,
                purchaseToken = purchaseToken,
                productId = productId,
                deviceName = android.os.Build.DEVICE,
                deviceId = android.os.Build.ID
            )
            result.fold(
                onSuccess = { response ->
                    _purchaseState.value = PurchaseUiState.Success(
                        "Premium activated successfully until ${formatDate(response.expiryDate)}"
                    )
                    checkPremiumStatus(userId)
                },
                onFailure = { error ->
                    _purchaseState.value = PurchaseUiState.Error(error.message ?: "Activation failed")
                }
            )
        }
    }

    fun validatePromoCode(code: String) {
        viewModelScope.launch {
            val result = premiumRepository.validatePromoCode(code)
            result.fold(
                onSuccess = { validation ->
                    if (validation.isValid) {
                        _purchaseState.value = PurchaseUiState.PromoCodeValid(validation)
                    } else {
                        _purchaseState.value = PurchaseUiState.Error("Promo code is not valid or expired")
                    }
                },
                onFailure = { error ->
                    _purchaseState.value = PurchaseUiState.Error(error.message ?: "Validation failed")
                }
            )
        }
    }

    fun cancelSubscription(userId: String) {
        _purchaseState.value = PurchaseUiState.Processing
        viewModelScope.launch {
            val result = premiumRepository.cancelSubscription(userId)
            result.fold(
                onSuccess = { response ->
                    _purchaseState.value = PurchaseUiState.CancellationSuccess(response.message)
                },
                onFailure = { error ->
                    _purchaseState.value = PurchaseUiState.Error(error.message ?: "Cancellation failed")
                }
            )
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
        return sdf.format(java.util.Date(timestamp))
    }
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

sealed class PurchaseUiState {
    object Idle : PurchaseUiState()
    object Processing : PurchaseUiState()
    data class Success(val message: String) : PurchaseUiState()
    data class Error(val message: String) : PurchaseUiState()
    data class PromoCodeValid(val validation: PromoCodeValidation) : PurchaseUiState()
    data class CancellationSuccess(val message: String) : PurchaseUiState()
}
