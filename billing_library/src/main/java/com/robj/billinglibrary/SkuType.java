package com.robj.billinglibrary;

import com.android.billingclient.api.BillingClient;

/**
 * Created by jj on 10/02/18.
 */

public enum SkuType {
    IN_APP(BillingClient.SkuType.INAPP), SUBSCRIPTION(BillingClient.SkuType.SUBS);
    private final String type;
    SkuType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
}
