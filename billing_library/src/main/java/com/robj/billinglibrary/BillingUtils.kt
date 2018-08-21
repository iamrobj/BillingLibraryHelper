package com.robj.billinglibrary

import android.app.Activity
import android.content.Context
import android.util.Log
import com.robj.billinglibrary.models.Optional
import com.robj.billinglibrary.models.Purchase
import com.robj.billinglibrary.models.SkuDetails
import com.robj.billinglibrary.models.SkuType
import io.reactivex.Observable
import java.util.*

/**
 * Created by Rob J on 05/11/17.
 */

object BillingUtils {

    private val TAG = BillingUtils::class.java.simpleName

    @JvmStatic
    fun reevaluateSpecificPurchasedStatus(context: Context, skuType: SkuType, sku: String): Observable<Optional<Purchase>> {
        val skus = ArrayList<String>()
        skus.add(sku)
        return reevaluateSpecificPurchasedStatus(context, skuType, skus)
    }

    @JvmStatic
    fun reevaluateSpecificPurchasedStatus(context: Context, skuType: SkuType, skus: List<String>): Observable<Optional<Purchase>> {
        return Billing.instance.getSpecificSkuPurchase(skuType.type, skus)
                .doOnNext { purchaseOptional ->
                    if (purchaseOptional.isEmpty) {
                        BillingManager.savePurchase(context, null)
                    } else {
                        val purchase = purchaseOptional.get()
                        BillingManager.savePurchase(context, purchase.sku)
                    }
                    BillingManager.setLastPurchaseCheckedDate(context, System.currentTimeMillis())
                }
    }

    @JvmStatic
    fun reevaluatePurchasedStatus(context: Context): Observable<Optional<Purchase>> {
        return Billing.instance.firstAvailablePurchase
                .doOnNext { purchaseOptional ->
                    if (purchaseOptional.isEmpty) {
                        BillingManager.savePurchase(context, null)
                    } else {
                        val purchase = purchaseOptional.get()
                        BillingManager.savePurchase(context, purchase.sku)
                    }
                    BillingManager.setLastPurchaseCheckedDate(context, System.currentTimeMillis())
                }
    }

    @JvmStatic
    fun makePurchase(activity: Activity, skuType: SkuType, sku: String): Observable<Purchase> {
        return Billing.instance.launchBillingFlow(activity, skuType.type, sku)!!
                .doOnNext { purchase ->
                    Log.d(TAG, "Purchase success..")
                    BillingManager.savePurchase(activity, purchase.sku)
                }
    }

    @JvmStatic
    fun getSkuInfo(skuType: SkuType, sku: String): Observable<SkuDetails> {
        return Billing.instance.getSkuInfo(skuType.type, sku)
    }

    @JvmStatic
    fun getSkuInfos(skuType: SkuType, skus: List<String>): Observable<List<SkuDetails>> {
        return Billing.instance.getSkuInfos(skuType.type, skus)
    }

    @JvmStatic
    fun consumeSkuPurchase(skuType: SkuType, sku: String): Observable<Boolean> {
        return Billing.instance.consumePurchase(skuType.type, sku)
    }

    @JvmStatic
    fun clearFirstAvailablePurchase() {
        Billing.instance.clearFirstAvailablePurchase()
    }
}
