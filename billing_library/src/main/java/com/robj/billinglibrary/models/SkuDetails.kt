package com.robj.billinglibrary.models

/**
 * Created by Rob J on 10/02/18.
 */

class SkuDetails internal constructor(val originalSkuDetails: com.android.billingclient.api.SkuDetails) {

    val sku: String
    val type: String
    val price: String
    val priceAmountMicros: Long
    val priceCurrencyCode: String
    val title: String
    val description: String
    val subscriptionPeriod: String
    val freeTrialPeriod: String
    val introductoryPrice: String
    val introductoryPriceAmountMicros: String
    val introductoryPricePeriod: String
    val introductoryPriceCycles: String

    init {
        sku = originalSkuDetails.sku
        type = originalSkuDetails.type
        price = originalSkuDetails.price
        priceAmountMicros = originalSkuDetails.priceAmountMicros
        priceCurrencyCode = originalSkuDetails.priceCurrencyCode
        title = originalSkuDetails.title
        description = originalSkuDetails.description
        subscriptionPeriod = originalSkuDetails.subscriptionPeriod
        freeTrialPeriod = originalSkuDetails.freeTrialPeriod
        introductoryPrice = originalSkuDetails.introductoryPrice
        introductoryPriceAmountMicros = originalSkuDetails.introductoryPriceAmountMicros
        introductoryPricePeriod = originalSkuDetails.introductoryPricePeriod
        introductoryPriceCycles = originalSkuDetails.introductoryPriceCycles
    }

    fun getSkuDetails(): com.android.billingclient.api.SkuDetails {
        return originalSkuDetails
    }
}
