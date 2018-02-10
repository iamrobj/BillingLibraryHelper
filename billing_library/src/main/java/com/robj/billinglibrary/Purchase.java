package com.robj.billinglibrary;

/**
 * Created by Rob J on 10/02/18.
 */

public class Purchase {

    private final com.android.billingclient.api.Purchase purchase;
    private final String orderId;
    private final String packageName;
    private final String sku;
    private final long purchaseTime;
    private final String purchaseToken;
    private final boolean autoRenewing;

    Purchase(com.android.billingclient.api.Purchase purchase) {
        this.purchase = purchase;
        this.orderId = purchase.getOrderId();
        this.packageName = purchase.getPackageName();
        this.sku = purchase.getSku();
        this.purchaseTime = purchase.getPurchaseTime();
        this.purchaseToken = purchase.getPurchaseToken();
        this.autoRenewing = purchase.isAutoRenewing();
    }

    public com.android.billingclient.api.Purchase getOriginalPurchase() {
        return purchase;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getSku() {
        return sku;
    }

    public long getPurchaseTime() {
        return purchaseTime;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public boolean isAutoRenewing() {
        return autoRenewing;
    }
}
