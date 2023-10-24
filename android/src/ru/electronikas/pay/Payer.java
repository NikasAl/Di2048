package ru.electronikas.pay;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

import kotlin.Unit;
import ru.electronikas.ads.AdController;
import ru.electronikas.diagonal.AndroidLauncher;
import ru.electronikas.diagonal.R;
import ru.electronikas.diagonal.settings.Storage;
import ru.rustore.sdk.billingclient.RuStoreBillingClient;
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory;
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult;
import ru.rustore.sdk.billingclient.model.purchase.Purchase;
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState;
import ru.rustore.sdk.billingclient.model.purchase.response.PurchasesResponse;
import ru.rustore.sdk.billingclient.provider.BillingClientThemeProvider;
import ru.rustore.sdk.billingclient.provider.logger.ExternalPaymentLoggerFactory;
import ru.rustore.sdk.billingclient.usecase.PurchasesUseCase;
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult;
import ru.rustore.sdk.core.tasks.OnCompleteListener;

public class Payer {

    public Context context;
    final String consoleApplicationId = "1354784703";
    final String deeplinkScheme = "dlscheme";

//    final BillingClientThemeProvider themeProvider = null;
//    final boolean debugLogs = false;
//    final ExternalPaymentLoggerFactory externalPaymentLoggerFactory = null;

    final public RuStoreBillingClient billingClient;
    private AdController adController;

    public Payer(Context context, AdController adController) {
        this.context = context;
        this.adController = adController;

        billingClient = RuStoreBillingClientFactory.INSTANCE.create(
                context,
                consoleApplicationId,
                deeplinkScheme
                // Опциональные параметры
//                themeProvider,
//                debugLogs,
//                externalPaymentLoggerFactory
        );

        billingClient.getPurchases().checkPurchasesAvailability().addOnCompleteListener(new OnCompleteListener<FeatureAvailabilityResult>() {
            @Override
            public void onFailure(@NonNull Throwable throwable) {
                // Process error
            }

            @Override
            public void onSuccess(FeatureAvailabilityResult featureAvailabilityResult) {
                if (featureAvailabilityResult instanceof FeatureAvailabilityResult.Available) {
                    Log.i("RuStoreBillingClient", "FeatureAvailabilityResult.Available");
//                    Toast.makeText(context,"FeatureAvailabilityResult.Available", Toast.LENGTH_LONG).show();
                } else if (featureAvailabilityResult instanceof FeatureAvailabilityResult.Unavailable) {
                    Log.i("RuStoreBillingClient", "FeatureAvailabilityResult.Unavailable");
                }
            }
        });

        restorePurchases();
    }

    private void restorePurchases() {
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();
        purchasesUseCase.getPurchases().addOnSuccessListener(purchases -> {
            for (Purchase purchase: purchases) {
                if (purchase.getPurchaseId() != null) {
                    if (purchase.getPurchaseState() == PurchaseState.CREATED || purchase.getPurchaseState() == PurchaseState.INVOICE_CREATED) {
                        purchasesUseCase.deletePurchase(purchase.getPurchaseId());
                    } else if (purchase.getPurchaseState() == PurchaseState.PAID) {
                        purchasesUseCase.confirmPurchase(purchase.getPurchaseId());

                    }
                }
            }
        });

        purchasesUseCase.getPurchases().addOnCompleteListener(new OnCompleteListener<List<Purchase>>() {
            @Override
            public void onFailure(@NonNull Throwable throwable) {

            }

            @Override
            public void onSuccess(List<Purchase> purchases) {
                if(purchases.size()==0) {
                    restoreAds();
                    return;
                }
                for (Purchase purchase: purchases) {
                    Log.i("RuStoreBillingClient", purchase.getPurchaseState().name());
                    if(purchase.getPurchaseState().equals(PurchaseState.CONFIRMED)) {
                        removeAds();
                    } else {
                        restoreAds();
                    }
                }
            }
        });

    }

    private void removeAds() {
        Storage.setShowAds(false);
        adController.stopAds();
    }

    private void restoreAds() {
        Storage.setShowAds(true);
    }

    public void purchaseProduct(String productId) {
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();

        purchasesUseCase.purchaseProduct(productId)
                .addOnCompleteListener(new OnCompleteListener<PaymentResult>() {
                    @Override
                    public void onFailure(@NonNull Throwable throwable) {

                    }

                    @Override
                    public void onSuccess(PaymentResult paymentResult) {
                        handlePaymentResult(paymentResult);
                    }
                });
    }

    private void handlePaymentResult(PaymentResult paymentResult) {
        if (paymentResult instanceof PaymentResult.Success) {
            confirmPurchase(((PaymentResult.Success) paymentResult).getPurchaseId());
        } else if (paymentResult instanceof PaymentResult.Failure) {
            deletePurchase(((PaymentResult.Failure) paymentResult).getPurchaseId());
        }
    }

    public void confirmPurchase(String purchaseId){
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();
        purchasesUseCase.confirmPurchase(purchaseId)
                .addOnCompleteListener(new OnCompleteListener<Unit>() {
                    @Override
                    public void onFailure(@NonNull Throwable throwable){
                    }
                    @Override
                    public void onSuccess(Unit unit){
                        Log.i("RuStoreBillingClient", "!!!Confirm");
                        removeAds();
                    }
                });
    }

    public void deletePurchase(String purchaseId){
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();
        purchasesUseCase.deletePurchase(purchaseId)
                .addOnCompleteListener(new OnCompleteListener<Unit>() {
                    @Override
                    public void onFailure(@NonNull Throwable throwable){
                    }
                    @Override
                    public void onSuccess(Unit unit){
                        Log.i("RuStoreBillingClient", "!!!Delete");
                        restoreAds();
                    }
                });
    }

}
