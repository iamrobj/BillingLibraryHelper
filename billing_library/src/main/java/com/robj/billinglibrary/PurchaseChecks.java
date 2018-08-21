package com.robj.billinglibrary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.robj.billinglibrary.models.Optional;
import com.robj.billinglibrary.models.Purchase;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Rob J on 27/08/17.
 */

class PurchaseChecks {

    private static final String TAG = PurchaseChecks.class.getSimpleName();

    public static void clearFirstAvailablePurchase(Context context, BillingClient mBillingClient) {
        getFirstAvailablePurchase(mBillingClient)
                .doOnNext(purchaseOptional -> {
                    if(!purchaseOptional.isEmpty()) {
                        Purchase purchase = purchaseOptional.get();
                        mBillingClient.consumeAsync(purchase.getPurchaseToken(), (purchaseToken, resultCode) -> Log.d(TAG, "Consumed : " + resultCode));
                    }
                    BillingManager.savePurchase(context, null);
                })
                .subscribeOn(Schedulers.io())
                .subscribe(integer -> {

                }, throwable -> throwable.printStackTrace());
    }

    /**
     * Returns true if successfully consumed
     * Returns false if sku wasn't purchased
     * **/
    public static Observable<Boolean> consumePurchase(Context context, BillingClient mBillingClient, @NonNull String skuType, @NonNull  String sku) {
        return Observable.create(e -> {
            com.android.billingclient.api.Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(skuType);
            if(purchasesResult.getResponseCode() == BillingClient.BillingResponse.OK) {
                for (com.android.billingclient.api.Purchase purchase : purchasesResult.getPurchasesList()) {
                    if (purchase.getSku().equals(sku)) {
                        if(e.isDisposed())
                            return;
                        mBillingClient.consumeAsync(purchase.getPurchaseToken(), (responseCode, purchaseToken) -> {
                            if(responseCode == BillingClient.BillingResponse.OK && context != null)
                                BillingManager.savePurchase(context, null);
                            if(e.isDisposed())
                                return;
                            if(responseCode == BillingClient.BillingResponse.OK)
                                e.onNext(true);
                            else
                                e.onError(new BillingException(BillingException.ErrorType.UNKNOWN, responseCode));
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
                e.onError(new BillingException(BillingException.ErrorType.UNABLE_TO_CHECK_PURCHASES, purchasesResult.getResponseCode()));
        });
    }

    public static Observable<com.robj.billinglibrary.models.SkuDetails> getSkuInfo(BillingClient mBillingClient, String skuType, String sku) {
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
                                        e.onNext(new com.robj.billinglibrary.models.SkuDetails(skuDetails));
                                    return;
                                }
                            }
                        }
                        if(!e.isDisposed())
                            e.onError(new BillingException(BillingException.ErrorType.NO_SKU_DETAILS, responseCode));
                    }
                    Log.e(TAG, "getSkuInfo response code: " + responseCode);
                    if(!e.isDisposed())
                        e.onError(new BillingException(BillingException.ErrorType.SKU_DETAILS_ERROR, responseCode));
                });
        });
    }

    public static Observable<List<com.robj.billinglibrary.models.SkuDetails>> getSkuInfos(BillingClient mBillingClient, String skuType, List<String> skuList) {
        return Observable.create(e -> {
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setType(skuType)
                    .setSkusList(skuList)
                    .build();
            mBillingClient.querySkuDetailsAsync(params, (responseCode, skuDetailsList) -> {
                if(e.isDisposed())
                    return;
                if (responseCode == BillingClient.BillingResponse.OK) {
                    if (skuDetailsList != null && !skuDetailsList.isEmpty()) {
                        List<com.robj.billinglibrary.models.SkuDetails> skuDetails = new ArrayList<>();
                        for (SkuDetails skuDetail : skuDetailsList)
                            skuDetails.add(new com.robj.billinglibrary.models.SkuDetails(skuDetail));

                        if(e.isDisposed())
                            return;
                        e.onNext(skuDetails);
                        e.onComplete();
                        return;
                    }
                    if(!e.isDisposed()) {
                        e.onError(new BillingException(BillingException.ErrorType.NO_SKU_DETAILS, responseCode));
                        e.onComplete();
                        return;
                    }
                }
                Log.e(TAG, "getSkuInfo response code: " + responseCode);
                if(!e.isDisposed()) {
                    e.onError(new BillingException(BillingException.ErrorType.SKU_DETAILS_ERROR, responseCode));
                    e.onComplete();
                    return;
                }
            });
        });
    }

    public static Observable<Optional<Purchase>> getSpecificSkuPurchase(BillingClient mBillingClient, String skuType, List<String> skus) {
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
                e.onError(new BillingException(BillingException.ErrorType.UNABLE_TO_CHECK_PURCHASES, purchasesResult.getResponseCode()));
        });
    }

    public static Observable<Optional<Purchase>> getFirstAvailablePurchase(BillingClient mBillingClient) {
        return getFirstAvailablePurchase(mBillingClient, BillingClient.SkuType.INAPP)
                .flatMap(purchaseOptional -> {
                    if(!purchaseOptional.isEmpty())
                        return Observable.just(purchaseOptional);
                    else
                        return getFirstAvailablePurchase(mBillingClient, BillingClient.SkuType.SUBS);
                });
    }

    public static Observable<Optional<Purchase>> getFirstAvailablePurchase(BillingClient mBillingClient, String skuType) {
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
                e.onError(new BillingException(BillingException.ErrorType.UNABLE_TO_CHECK_PURCHASES, purchasesResult.getResponseCode()));
        });
    }

}
