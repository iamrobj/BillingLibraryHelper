package com.robj.billinglibrary

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.robj.billinglibrary.models.Optional
import com.robj.billinglibrary.models.Purchase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Rob J on 27/08/17.
 */

internal class Billing private constructor(private val context: Context) : BillingClientStateListener {

    private val mBillingClient: BillingClient
    private lateinit var purchaseSubject: BehaviorSubject<Purchase>
    private lateinit var publicPurchaseSubject: Observable<Purchase>

    val firstAvailablePurchase: Observable<Optional<Purchase>>
        get() = PurchaseChecks.getFirstAvailablePurchase(mBillingClient)

    private fun finish() {
        mBillingClient.endConnection()
    }

    private fun initSubject() {
        purchaseSubject = BehaviorSubject.create<Purchase>()
        publicPurchaseSubject = purchaseSubject
                .doOnSubscribe { Log.d(TAG, "Something subscribed..") }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose {
                    Log.d(TAG, "Something unsubscribed, still has observers: ${purchaseSubject.hasObservers()}..")
                }
    }

    init {
        initSubject();
        mBillingClient = BillingClient.newBuilder(context)
                .setListener { responseCode, purchases ->
                    if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                            return@setListener
                        }
                        handlePurchaseError(-100, BillingException.ErrorType.UNKNOWN) //TODO: Properly
                    } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
                        handlePurchaseError(BillingClient.BillingResponse.USER_CANCELED, BillingException.ErrorType.BILLING_CANCELLED)
                    } else {
                        handleResponse(responseCode, null)
                    }
                }.build()
        mBillingClient.startConnection(this)
    }

    private fun handlePurchaseError(originalErrorCode: Int, errorType: BillingException.ErrorType) {
        purchaseSubject.onError(BillingException(errorType, originalErrorCode));
    }

    private fun handlePurchase(purchase: com.android.billingclient.api.Purchase) {
        Log.d(TAG, "Purchase of sku " + purchase.sku + " was successful..")
        BillingManager.savePurchase(context, purchase.sku)
        purchaseSubject.onNext(Purchase(purchase));
    }

    fun launchBillingFlow(activity: Activity, skuType: String, skuId: String): Observable<Purchase> {
        val builder = BillingFlowParams.newBuilder()
                .setSku(skuId)
                .setType(skuType)
        val code = mBillingClient.launchBillingFlow(activity, builder.build())
        handleResponse(code, skuId)
        return publicPurchaseSubject;
    }

    private fun handleResponse(code: Int, sku: String?) {
        when (code) {
            BillingClient.BillingResponse.OK -> {
            }
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> {
                handlePurchaseError(code, BillingException.ErrorType.ALREADY_OWNED)
                BillingManager.savePurchase(context, sku)
            }
            BillingClient.BillingResponse.ITEM_UNAVAILABLE -> handlePurchaseError(code, BillingException.ErrorType.ITEM_UNAVAILABLE)
            BillingClient.BillingResponse.USER_CANCELED -> handlePurchaseError(code, BillingException.ErrorType.BILLING_CANCELLED)
            BillingClient.BillingResponse.ITEM_NOT_OWNED, BillingClient.BillingResponse.BILLING_UNAVAILABLE, BillingClient.BillingResponse.DEVELOPER_ERROR, BillingClient.BillingResponse.ERROR, BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED, BillingClient.BillingResponse.SERVICE_UNAVAILABLE, BillingClient.BillingResponse.SERVICE_DISCONNECTED -> handlePurchaseError(code, BillingException.ErrorType.UNKNOWN)
            else -> handlePurchaseError(code, BillingException.ErrorType.UNKNOWN)
        }
    }

    fun clearFirstAvailablePurchase() {
        PurchaseChecks.clearFirstAvailablePurchase(context, mBillingClient)
    }

    /**
     * Returns true if successfully consumed
     * Returns false if sku wasn't purchased
     */
    fun consumePurchase(skuType: String, sku: String): Observable<Boolean> {
        return PurchaseChecks.consumePurchase(context, mBillingClient, skuType, sku)
    }

    fun getSkuInfo(skuType: String, sku: String): Observable<com.robj.billinglibrary.models.SkuDetails> {
        return PurchaseChecks.getSkuInfo(mBillingClient, skuType, sku)
    }

    fun getSkuInfos(skuType: String, skuList: List<String>): Observable<List<com.robj.billinglibrary.models.SkuDetails>> {
        return PurchaseChecks.getSkuInfos(mBillingClient, skuType, skuList)
    }

    fun getSpecificSkuPurchase(skuType: String, skus: List<String>): Observable<Optional<Purchase>> {
        return PurchaseChecks.getSpecificSkuPurchase(mBillingClient, skuType, skus)
    }

    private fun getFirstAvailablePurchase(skuType: String): Observable<Optional<Purchase>> {
        return PurchaseChecks.getFirstAvailablePurchase(mBillingClient, skuType)
    }

    override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
        if (billingResponseCode == BillingClient.BillingResponse.OK)
            Log.d(TAG, "Setup finished successfully..")
        else
            Log.e(TAG, "Setup error occurred, response code: $billingResponseCode")
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "Setup finished successfully..")
    }

    companion object {

        private val TAG = Billing::class.java.simpleName

        private var instance: Billing? = null

        fun getInstance(context: Context): Billing {
            if(instance == null)
                instance = Billing(context)
            return instance!!
        }

        fun init(context: Context) {
            instance?.finish()
            Billing(context)
        }
    }


}
