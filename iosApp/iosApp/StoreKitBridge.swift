import Foundation
import StoreKit
import ComposeApp

/// StoreKit implementation that bridges to the Kotlin IosStoreKitDelegate interface.
@MainActor
class StoreKitDelegate: NSObject, IosStoreKitDelegate {

    private var products: [Product] = []
    private var onReadyCallback: (() -> Void)?
    private var onProductLoadedCallback: ((String, String, String, String) -> Void)?
    private var onPurchaseCompletedCallback: (() -> Void)?
    private var purchaseTask: Task<Void, Never>?

    func initialize(
        onReady: @escaping () -> Void,
        onProductLoaded: @escaping (String, String, String, String) -> Void,
        onPurchaseCompleted: @escaping () -> Void
    ) {
        self.onReadyCallback = onReady
        self.onProductLoadedCallback = onProductLoaded
        self.onPurchaseCompletedCallback = onPurchaseCompleted

        // Start listening for transactions
        purchaseTask = Task {
            for await result in Transaction.updates {
                await handleTransaction(result)
            }
        }

        onReady()
    }

    func queryProducts(productId: String) {
        Task {
            do {
                let storeProducts = try await Product.products(for: [productId])
                self.products = storeProducts

                if let product = storeProducts.first {
                    onProductLoadedCallback?(
                        product.id,
                        product.displayName,
                        product.description,
                        product.displayPrice
                    )
                }
            } catch {
                print("Failed to load products: \(error)")
            }
        }
    }

    func purchase(productId: String) -> BillingResult {
        // Find the product
        guard let product = products.first(where: { $0.id == productId }) else {
            return BillingResult.NotAvailable()
        }

        // Note: This is synchronous for the Kotlin interface, but we handle
        // the actual async purchase via Transaction.updates
        Task {
            do {
                let result = try await product.purchase()

                switch result {
                case .success(let verification):
                    let transaction = try checkVerified(verification)
                    await transaction.finish()
                    onPurchaseCompletedCallback?()

                case .userCancelled:
                    break

                case .pending:
                    break

                @unknown default:
                    break
                }
            } catch {
                print("Purchase failed: \(error)")
            }
        }

        return BillingResult.Success()
    }

    func restorePurchases() -> BillingResult {
        Task {
            do {
                try await AppStore.sync()

                // Check for existing purchases
                for await result in Transaction.currentEntitlements {
                    if case .verified(let transaction) = result {
                        if transaction.productID == "homesteader_remove_ads" {
                            onPurchaseCompletedCallback?()
                            return
                        }
                    }
                }
            } catch {
                print("Restore failed: \(error)")
            }
        }

        return BillingResult.Success()
    }

    private func handleTransaction(_ result: VerificationResult<Transaction>) async {
        guard case .verified(let transaction) = result else {
            return
        }

        if transaction.productID == "homesteader_remove_ads" {
            onPurchaseCompletedCallback?()
        }

        await transaction.finish()
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified:
            throw StoreError.failedVerification
        case .verified(let safe):
            return safe
        }
    }
}

enum StoreError: Error {
    case failedVerification
}

/// Non-actor wrapper for initializing from non-async context
class StoreKitDelegateWrapper: IosStoreKitDelegate {
    private let delegate = StoreKitDelegate()

    func initialize(
        onReady: @escaping () -> Void,
        onProductLoaded: @escaping (String, String, String, String) -> Void,
        onPurchaseCompleted: @escaping () -> Void
    ) {
        Task { @MainActor in
            delegate.initialize(
                onReady: onReady,
                onProductLoaded: onProductLoaded,
                onPurchaseCompleted: onPurchaseCompleted
            )
        }
    }

    func queryProducts(productId: String) {
        Task { @MainActor in
            delegate.queryProducts(productId: productId)
        }
    }

    func purchase(productId: String) -> BillingResult {
        // Launch on main actor
        var result: BillingResult = BillingResult.Success()
        let semaphore = DispatchSemaphore(value: 0)

        Task { @MainActor in
            result = delegate.purchase(productId: productId)
            semaphore.signal()
        }

        // Don't actually wait - return immediately and handle via callbacks
        return BillingResult.Success()
    }

    func restorePurchases() -> BillingResult {
        Task { @MainActor in
            _ = delegate.restorePurchases()
        }
        return BillingResult.Success()
    }
}
