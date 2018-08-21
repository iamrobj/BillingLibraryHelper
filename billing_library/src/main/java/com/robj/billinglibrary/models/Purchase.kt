package com.robj.billinglibrary.models

/**
 * Created by Rob J on 10/02/18.
 */

class Purchase internal constructor(val originalPurchase: com.android.billingclient.api.Purchase) {
    val orderId: String
    val packageName: String
    val sku: String
    val purchaseTime: Long
    val purchaseToken: String
    val isAutoRenewing: Boolean

    init {
        this.orderId = originalPurchase.orderId
        this.packageName = originalPurchase.packageName
        this.sku = originalPurchase.sku
        this.purchaseTime = originalPurchase.purchaseTime
        this.purchaseToken = originalPurchase.purchaseToken
        this.isAutoRenewing = originalPurchase.isAutoRenewing
    }
}
