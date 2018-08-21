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
        return Billing.getInstance(context).getSpecificSkuPurchase(skuType.type, skus)
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
        return Billing.getInstance(context).firstAvailablePurchase
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
        return Billing.getInstance(activity).launchBillingFlow(activity, skuType.type, sku)!!
                .doOnNext { purchase ->
                    Log.d(TAG, "Purchase success..")
                    BillingManager.savePurchase(activity, purchase.sku)
                }
    }

    @JvmStatic
    fun getSkuInfo(context: Context, skuType: SkuType, sku: String): Observable<SkuDetails> {
        return Billing.getInstance(context).getSkuInfo(skuType.type, sku)
    }

    @JvmStatic
    fun getSkuInfos(context: Context, skuType: SkuType, skus: List<String>): Observable<List<SkuDetails>> {
        return Billing.getInstance(context).getSkuInfos(skuType.type, skus)
    }

    @JvmStatic
    fun consumeSkuPurchase(context: Context, skuType: SkuType, sku: String): Observable<Boolean> {
        return Billing.getInstance(context).consumePurchase(skuType.type, sku)
    }

    @JvmStatic
    fun clearFirstAvailablePurchase(context: Context) {
        Billing.getInstance(context).clearFirstAvailablePurchase()
    }
}
