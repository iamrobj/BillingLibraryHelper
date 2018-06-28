package com.robj.billinglibrary;

import com.android.billingclient.api.BillingClient;

/**
 * Created by Rob J on 05/11/17.
 */
public class BillingException extends RuntimeException {

    public enum ErrorType {
        BILLING_CANCELLED, NO_SKU_DETAILS, SKU_DETAILS_ERROR, UNABLE_TO_CHECK_PURCHASES, ALREADY_OWNED, ITEM_UNAVAILABLE, UNKNOWN
    }

    public final ErrorType errorType;
    public final String originalErrorCode;

    BillingException(ErrorType errorType, int originalErrorCode) {
        this.errorType = errorType;
        this.originalErrorCode = getOriginalErrorCodeAsString(originalErrorCode);
    }

    private String getOriginalErrorCodeAsString(int originalErrorCode) {
        switch (originalErrorCode) {
            case BillingClient.BillingResponse.ITEM_ALREADY_OWNED:
                return "ITEM_ALREADY_OWNED";
            case BillingClient.BillingResponse.ITEM_UNAVAILABLE:
                return "ITEM_UNAVAILABLE";
            case BillingClient.BillingResponse.SERVICE_DISCONNECTED:
                return "SERVICE_DISCONNECTED";
            case BillingClient.BillingResponse.USER_CANCELED:
                return "USER_CANCELED";
            case BillingClient.BillingResponse.ITEM_NOT_OWNED:
                return "ITEM_NOT_OWNED";
            case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
                return "BILLING_UNAVAILABLE";
            case BillingClient.BillingResponse.DEVELOPER_ERROR:
                return "DEVELOPER_ERROR";
            case BillingClient.BillingResponse.ERROR:
                return "ERROR";
            case BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED:
                return "FEATURE_NOT_SUPPORTED";
            case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
                return "SERVICE_UNAVAILABLE";
            default:
                return String.valueOf(originalErrorCode);
        }
    }
}
