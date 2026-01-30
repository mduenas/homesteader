package com.markduenas.homesteader.feature.premium

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.domain.monetization.PremiumManager
import com.markduenas.homesteader.domain.monetization.PremiumProduct
import com.markduenas.homesteader.domain.monetization.PurchaseResult
import com.markduenas.homesteader.domain.monetization.PurchaseState
import com.markduenas.homesteader.domain.monetization.RestoreResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PremiumState(
    val isPremium: Boolean = false,
    val isProcessing: Boolean = false,
    val product: PremiumProduct? = null,
    val error: String? = null
)

sealed class PremiumIntent {
    data object Purchase : PremiumIntent()
    data object RestorePurchases : PremiumIntent()
    data object ClearError : PremiumIntent()
}

sealed class PremiumEffect {
    data class ShowMessage(val message: String) : PremiumEffect()
    data object PurchaseSuccess : PremiumEffect()
}

class PremiumViewModel(
    private val premiumManager: PremiumManager
) : ScreenModel {

    private val _error = MutableStateFlow<String?>(null)

    val state: StateFlow<PremiumState> = combine(
        premiumManager.isPremium,
        premiumManager.purchaseState,
        _error
    ) { isPremium, purchaseState, error ->
        PremiumState(
            isPremium = isPremium,
            isProcessing = purchaseState is PurchaseState.Processing ||
                          purchaseState is PurchaseState.Restoring,
            product = premiumManager.getPremiumProductInfo(),
            error = error ?: (purchaseState as? PurchaseState.Error)?.message
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PremiumState(product = premiumManager.getPremiumProductInfo())
    )

    private val _effects = Channel<PremiumEffect>()
    val effects = _effects.receiveAsFlow()

    fun handleIntent(intent: PremiumIntent) {
        when (intent) {
            PremiumIntent.Purchase -> purchase()
            PremiumIntent.RestorePurchases -> restorePurchases()
            PremiumIntent.ClearError -> _error.value = null
        }
    }

    private fun purchase() {
        screenModelScope.launch {
            val result = premiumManager.purchasePremium()
            when (result) {
                PurchaseResult.Success -> {
                    _effects.send(PremiumEffect.PurchaseSuccess)
                    _effects.send(PremiumEffect.ShowMessage("Thank you for your purchase!"))
                }
                PurchaseResult.Cancelled -> {
                    // User cancelled, no message needed
                }
                PurchaseResult.AlreadyOwned -> {
                    premiumManager.setPremiumStatus(true)
                    _effects.send(PremiumEffect.ShowMessage("You already own premium!"))
                }
                PurchaseResult.NotImplemented -> {
                    _error.value = "In-app purchases will be available in the full release"
                }
                is PurchaseResult.Error -> {
                    _error.value = result.message
                }
            }
        }
    }

    private fun restorePurchases() {
        screenModelScope.launch {
            val result = premiumManager.restorePurchases()
            when (result) {
                is RestoreResult.Success -> {
                    if (result.premiumRestored) {
                        _effects.send(PremiumEffect.ShowMessage("Premium restored successfully!"))
                    } else {
                        _effects.send(PremiumEffect.ShowMessage("No purchases to restore"))
                    }
                }
                RestoreResult.NothingToRestore -> {
                    _effects.send(PremiumEffect.ShowMessage("No purchases to restore"))
                }
                RestoreResult.NotImplemented -> {
                    _error.value = "Restore will be available in the full release"
                }
                is RestoreResult.Error -> {
                    _error.value = result.message
                }
            }
        }
    }
}
