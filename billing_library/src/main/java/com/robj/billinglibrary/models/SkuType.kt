package com.robj.billinglibrary.models

import com.android.billingclient.api.BillingClient

/**
 * Created by jj on 10/02/18.
 */

enum class SkuType private constructor(val type: String) {
    IN_APP(BillingClient.SkuType.INAPP), SUBSCRIPTION(BillingClient.SkuType.SUBS)
}
