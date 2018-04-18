package com.robj.billinglibrary;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Rob J on 27/08/17.
 */

class Billing implements BillingClientStateListener {

    private static final String TAG = Billing.class.getSimpleName();

    private static Billing billing;

    private final BillingClient mBillingClient;
    private final Context context;

    private ObservableEmitter<Purchase> purchaseObservableEmitter;

    public static void init(Application context) {
        if(billing != null)
            billing.finish();
        new Billing(context);
    }

    private void finish() {
        mBillingClient.endConnection();
    }

    private Billing(Application context) {
        this.billing = this;
        this.context = context;
        mBillingClient = BillingClient.newBuilder(context)
                .setListener((responseCode, purchases) -> {
                    if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                        for (com.android.billingclient.api.Purchase purchase : purchases) {
                            handlePurchase(purchase);
                            return;
                        }
                        handlePurchaseError(BillingException.ErrorType.UNKNOWN); //TODO: Properly
                    } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
                        handlePurchaseError(BillingException.ErrorType.BILLING_CANCELLED);
                    } else {
                        handleResponse(responseCode, null);
                    }
                }).build();
        mBillingClient.startConnection(this);
    }

    private void handlePurchaseError(BillingException.ErrorType errorType) {
        if(purchaseObservableEmitter != null) {
            purchaseObservableEmitter.onError(new BillingException(errorType));
            purchaseObservableEmitter = null;
        }
    }

    private void handlePurchase(com.android.billingclient.api.Purchase purchase) {
        Log.d(TAG, "Purchase of sku " + purchase.getSku() + " was successful..");
        BillingManager.savePurchase(getContext(), purchase.getSku());
        if(purchaseObservableEmitter != null) {
            purchaseObservableEmitter.onNext(new Purchase(purchase));
            purchaseObservableEmitter = null;
        }
    }

    private Context getContext() {
        return context;
    }

    public void clearFirstAvailablePurchase() {
        getFirstAvailablePurchase()
                .doOnNext(purchaseOptional -> {
                    if(!purchaseOptional.isEmpty()) {
                        Purchase purchase = purchaseOptional.get();
                        mBillingClient.consumeAsync(purchase.getPurchaseToken(), (purchaseToken, resultCode) -> Log.d(TAG, "Consumed : " + resultCode));
                        BillingManager.savePurchase(getContext(), null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(integer -> {

                }, throwable -> throwable.printStackTrace());
    }

    /**
     * Returns true if successfully consumed
     * Returns false if sku wasn't purchased
     * **/
    public Observable<Boolean> consumePurchase(@NonNull String skuType, @NonNull  String sku) {
        return Observable.create(e -> {
            com.android.billingclient.api.Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(skuType);
            if(purchasesResult.getResponseCode() == BillingClient.BillingResponse.OK) {
                for (com.android.billingclient.api.Purchase purchase : purchasesResult.getPurchasesList()) {
                    if (purchase.getSku().equals(sku)) {
                        if(e.isDisposed())
                            return;
                        mBillingClient.consumeAsync(purchase.getPurchaseToken(), (responseCode, purchaseToken) -> {
                            if(e.isDisposed())
                                return;
                            if(responseCode == BillingClient.BillingResponse.OK)
                                e.onNext(true);
                            else
                                e.onError(new BillingException(BillingException.ErrorType.UNKNOWN));
                            e.onComplete();
                        });
                        return;
                    }
                }
                e.onNext(false);
                e.onComplete();
                return;
            }
            Log.e(TAG, "consumePurchase for sku type " + skuType + " with response code: " + purchasesResult.getResponseCode());
            if(!e.isDisposed())
                e.onError(new BillingException(BillingException.ErrorType.UNABLE_TO_CHECK_PURCHASES));
        });
    }

    public Observable<com.robj.billinglibrary.SkuDetails> getSkuInfo(String skuType, String sku) {
        return Observable.create(e -> {
                List<String> skuList = new ArrayList();
                skuList.add(sku);
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                                            .setType(skuType)
                                            .setSkusList(skuList)
                                            .build();
                mBillingClient.querySkuDetailsAsync(params, (responseCode, skuDetailsList) -> {
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        if (skuDetailsList != null) {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                if (skuDetails.getSku().equals(sku)) {
                                    if(!e.isDisposed())
                                        e.onNext(new com.robj.billinglibrary.SkuDetails(skuDetails));
                                    return;
                                }
                            }
                        }
                        if(!e.isDisposed())
                            e.onError(new BillingException(BillingException.ErrorType.NO_SKU_DETAILS));
                    }
                    Log.e(TAG, "getSkuInfo response code: " + responseCode);
                    if(!e.isDisposed())
                        e.onError(new BillingException(BillingException.ErrorType.SKU_DETAILS_ERROR));
                });
        });
    }

    public Observable<Optional<Purchase>> getSpecificSkuPurchase(String skuType, List<String> skus) {
        return Observable.create(e -> {

            com.android.billingclient.api.Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(skuType);
            if(purchasesResult.getResponseCode() == BillingClient.BillingResponse.OK) {
                for (com.android.billingclient.api.Purchase purchase : purchasesResult.getPurchasesList()) {
                    if(skus.contains(purchase.getSku())) {
                        if(e.isDisposed())
                            return;
                        e.onNext(new Optional<>(new Purchase(purchase)));
                        e.onComplete();
                        return;
                    }
                }
                e.onNext(new Optional<>(null));
                e.onComplete();
                return;
            }
            Log.e(TAG, "getSpecificSkuPurchase for sku type " + skuType + " with response code: " + purchasesResult.getResponseCode());
            if(!e.isDisposed())
                e.onError(new BillingException(BillingException.ErrorType.UNABLE_TO_CHECK_PURCHASES));
        });
    }

    public Observable<Optional<Purchase>> getFirstAvailablePurchase() {
        return getFirstAvailablePurchase(BillingClient.SkuType.INAPP)
                .flatMap(purchaseOptional -> {
                    if(!purchaseOptional.isEmpty())
                        return Observable.just(purchaseOptional);
                    else
                        return getFirstAvailablePurchase(BillingClient.SkuType.SUBS);
                });
    }

    private Observable<Optional<Purchase>> getFirstAvailablePurchase(String skuType) {
        return Observable.create(e -> {
            com.android.billingclient.api.Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(skuType);
            if(purchasesResult.getResponseCode() == BillingClient.BillingResponse.OK) {
                for (com.android.billingclient.api.Purchase purchase : purchasesResult.getPurchasesList()) {
                    if(!e.isDisposed())
                        e.onNext(new Optional(new Purchase(purchase)));
                    return;
                }
                if(!e.isDisposed())
                    e.onNext(new Optional(null));
                return;
            }
            Log.e(TAG, "getPurchases for sku type " + skuType + " with response code: " + purchasesResult.getResponseCode());
            if(!e.isDisposed())
                e.onError(new BillingException(BillingException.ErrorType.UNABLE_TO_CHECK_PURCHASES));
        });
    }

    public Observable<Purchase> launchBillingFlow(Activity activity, String skuType, String skuId) {
        if(purchaseObservableEmitter != null)
            return null;
        return Observable.create(e -> {
            purchaseObservableEmitter = e;
            BillingFlowParams.Builder builder = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(skuType);
            int code = mBillingClient.launchBillingFlow(activity, builder.build());
            handleResponse(code, skuId);
        });
    }

    private void handleResponse(int code, String sku) {
        switch (code) {
            case BillingClient.BillingResponse.OK:
                break;
            case BillingClient.BillingResponse.ITEM_ALREADY_OWNED:
                handlePurchaseError(BillingException.ErrorType.ALREADY_OWNED);
                BillingManager.savePurchase(getContext(), sku);
                break;
            case BillingClient.BillingResponse.ITEM_UNAVAILABLE:
                handlePurchaseError(BillingException.ErrorType.ITEM_UNAVAILABLE);
                break;
            case BillingClient.BillingResponse.SERVICE_DISCONNECTED:
//                break;
            case BillingClient.BillingResponse.USER_CANCELED:
                handlePurchaseError(BillingException.ErrorType.BILLING_CANCELLED);
                break;
            case BillingClient.BillingResponse.ITEM_NOT_OWNED:
            case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
            case BillingClient.BillingResponse.DEVELOPER_ERROR:
            case BillingClient.BillingResponse.ERROR:
            case BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED:
            case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
            default:
                handlePurchaseError(BillingException.ErrorType.UNKNOWN);
                break;
        }
    }

    @Override
    public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
        if (billingResponseCode == BillingClient.BillingResponse.OK)
            Log.d(TAG, "Setup finished successfully..");
        else
            Log.e(TAG, "Setup error occurred, response code: " + billingResponseCode);
    }

    @Override
    public void onBillingServiceDisconnected() {
        Log.d(TAG, "Setup finished successfully..");
    }

    public static Billing getInstance() {
        return billing;
    }


}
