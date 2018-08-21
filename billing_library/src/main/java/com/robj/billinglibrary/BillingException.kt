package com.robj.billinglibrary

import com.android.billingclient.api.BillingClient

/**
 * Created by Rob J on 05/11/17.
 */
class BillingException internal constructor(val errorType: ErrorType, originalErrorCode: Int) : RuntimeException() {

    val originalErrorCode: String

    enum class ErrorType {
        BILLING_CANCELLED, NO_SKU_DETAILS, SKU_DETAILS_ERROR, UNABLE_TO_CHECK_PURCHASES, ALREADY_OWNED, ITEM_UNAVAILABLE, UNKNOWN
    }

    init {
        this.originalErrorCode = getOriginalErrorCodeAsString(originalErrorCode)
    }

    private fun getOriginalErrorCodeAsString(originalErrorCode: Int): String {
        return when (originalErrorCode) {
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
            BillingClient.BillingResponse.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
            BillingClient.BillingResponse.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
            BillingClient.BillingResponse.USER_CANCELED -> "USER_CANCELED"
            BillingClient.BillingResponse.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
            BillingClient.BillingResponse.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
            BillingClient.BillingResponse.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
            BillingClient.BillingResponse.ERROR -> "ERROR"
            BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
            BillingClient.BillingResponse.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
            else -> originalErrorCode.toString()
        }
    }
}
