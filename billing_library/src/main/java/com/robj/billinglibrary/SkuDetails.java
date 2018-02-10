package com.robj.billinglibrary;

/**
 * Created by Rob J on 10/02/18.
 */

public class SkuDetails {

    private final String sku;
    private final String type;
    private final String price;
    private final long priceAmountMicros;
    private final String priceCurrencyCode;
    private final String title;
    private final String description;
    private final String subscriptionPeriod;
    private final String freeTrialPeriod;
    private final String introductoryPrice;
    private final String introductoryPriceAmountMicros;
    private final String introductoryPricePeriod;
    private final String introductoryPriceCycles;
    private final com.android.billingclient.api.SkuDetails skuDetails;

    SkuDetails(com.android.billingclient.api.SkuDetails skuDetails) {
        this.skuDetails = skuDetails;
        sku = skuDetails.getSku();
        type = skuDetails.getType();
        price = skuDetails.getPrice();
        priceAmountMicros = skuDetails.getPriceAmountMicros();
        priceCurrencyCode = skuDetails.getPriceCurrencyCode();
        title = skuDetails.getTitle();
        description = skuDetails.getDescription();
        subscriptionPeriod = skuDetails.getSubscriptionPeriod();
        freeTrialPeriod = skuDetails.getFreeTrialPeriod();
        introductoryPrice = skuDetails.getIntroductoryPrice();
        introductoryPriceAmountMicros = skuDetails.getIntroductoryPriceAmountMicros();
        introductoryPricePeriod = skuDetails.getIntroductoryPricePeriod();
        introductoryPriceCycles = skuDetails.getIntroductoryPriceCycles();
    }

    public com.android.billingclient.api.SkuDetails getOriginalSkuDetails() {
        return skuDetails;
    }

    public String getSku() {
        return sku;
    }

    public String getType() {
        return type;
    }

    public String getPrice() {
        return price;
    }

    public long getPriceAmountMicros() {
        return priceAmountMicros;
    }

    public String getPriceCurrencyCode() {
        return priceCurrencyCode;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSubscriptionPeriod() {
        return subscriptionPeriod;
    }

    public String getFreeTrialPeriod() {
        return freeTrialPeriod;
    }

    public String getIntroductoryPrice() {
        return introductoryPrice;
    }

    public String getIntroductoryPriceAmountMicros() {
        return introductoryPriceAmountMicros;
    }

    public String getIntroductoryPricePeriod() {
        return introductoryPricePeriod;
    }

    public String getIntroductoryPriceCycles() {
        return introductoryPriceCycles;
    }

    public com.android.billingclient.api.SkuDetails getSkuDetails() {
        return skuDetails;
    }
}
