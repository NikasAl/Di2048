package ru.electronikas.pay;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kotlin.Unit;
import ru.electronikas.ads.AdController;
import ru.electronikas.diagonal.model.ActiveRes;
import ru.electronikas.diagonal.model.Product;
import ru.electronikas.diagonal.settings.Storage;
import ru.rustore.sdk.billingclient.RuStoreBillingClient;
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory;
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult;
import ru.rustore.sdk.billingclient.model.purchase.Purchase;
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState;
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
        if(Storage.isNoAdTimeOver()) {
            Log.i("RuStoreBillingClient", "NoAdTime is over. Restore Ads.");
            restoreAds();
        }

        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();
        purchasesUseCase.getPurchases().addOnSuccessListener(purchases -> {
            for (Purchase purchase: purchases) {
                Log.i("RuStoreBillingClient", purchase.toString());
                if (purchase.getPurchaseId() != null) {
                    if (purchase.getPurchaseState() == PurchaseState.CREATED || purchase.getPurchaseState() == PurchaseState.INVOICE_CREATED) {
                        purchasesUseCase.deletePurchase(purchase.getPurchaseId());
                    } else if (purchase.getPurchaseState() == PurchaseState.PAID) {
                        purchasesUseCase.confirmPurchase(purchase.getPurchaseId()).addOnCompleteListener(confirmListener);
                    }
//                    else if (purchase.getPurchaseState() == PurchaseState.CONFIRMED) {
                        //removeAds();
//                    }
                }
            }
        });
    }


    private void removeAds() {
        Storage.setNoAdTime();
        Storage.setShowAds(false);
        adController.stopAds();
    }

    private void restoreAds() {
        Storage.setShowAds(true);
        adController.initAd();
    }

    public void purchaseProduct(Product productId) {
        PurchasesUseCase purchasesUseCase = billingClient.getPurchases();

        purchasesUseCase.purchaseProduct(productId.name())
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
        purchasesUseCase.confirmPurchase(purchaseId).addOnCompleteListener(confirmListener);
    }

    private final OnCompleteListener<Unit> confirmListener = new OnCompleteListener<Unit>() {
        @Override
        public void onFailure(@NonNull Throwable throwable){
        }
        @Override
        public void onSuccess(Unit unit){
            Log.i("RuStoreBillingClient", "!!!Confirm");
            removeAds();
        }
    };

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
