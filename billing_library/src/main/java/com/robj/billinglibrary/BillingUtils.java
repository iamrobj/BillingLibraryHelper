package com.robj.billinglibrary;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.billingclient.api.Purchase;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Rob J on 05/11/17.
 */

public class BillingUtils {

    private static final String TAG = BillingUtils.class.getSimpleName();

    public static Observable<Optional<com.robj.billinglibrary.Purchase>> reevaluateSpecificPurchasedStatus(Context context, SkuType skuType, String sku) {
        List<String> skus = new ArrayList();
        skus.add(sku);
        return reevaluateSpecificPurchasedStatus(context, skuType, skus);
    }

    public static Observable<Optional<com.robj.billinglibrary.Purchase>> reevaluateSpecificPurchasedStatus(Context context, SkuType skuType, List<String> skus) {
        return Billing.getInstance().getSpecificSkuPurchase(skuType.getType(), skus)
                .doOnNext(purchaseOptional -> {
                    if (purchaseOptional.isEmpty()) {
                        BillingManager.savePurchase(context, null);
                    } else {
                        com.robj.billinglibrary.Purchase purchase = purchaseOptional.get();
                        BillingManager.savePurchase(context, purchase.getSku());
                    }
                    BillingManager.setLastPurchaseCheckedDate(context, System.currentTimeMillis());
                });
    }

    public static Observable<Optional<com.robj.billinglibrary.Purchase>> reevaluatePurchasedStatus(Context context) {
        return Billing.getInstance().getFirstAvailablePurchase()
                .doOnNext(purchaseOptional -> {
                    if(purchaseOptional.isEmpty()) {
                        BillingManager.savePurchase(context, null);
                    } else {
                        com.robj.billinglibrary.Purchase purchase = purchaseOptional.get();
                        BillingManager.savePurchase(context, purchase.getSku());
                    }
                    BillingManager.setLastPurchaseCheckedDate(context, System.currentTimeMillis());
                });
    }

    public static Observable<com.robj.billinglibrary.Purchase> makePurchase(Activity activity, SkuType skuType, String sku) {
        return Billing.getInstance().launchBillingFlow(activity, skuType.getType(), sku)
                .doOnNext(purchase -> {
                    Log.d(TAG, "Purchase success..");
                    BillingManager.savePurchase(activity, purchase.getSku());
                });
    }

    public static Observable<SkuDetails> getSkuInfo(SkuType skuType, String sku) {
        return Billing.getInstance().getSkuInfo(skuType.getType(), sku);
    }

    public static Observable<Boolean> consumeSkuPurchase(SkuType skuType, String sku) {
        return Billing.getInstance().consumePurchase(skuType.getType(), sku);
    }

    public static void clearFirstAvailablePurchase() {
        Billing.getInstance().clearFirstAvailablePurchase();
    }
}
