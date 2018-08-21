package com.robj.billinglibrary

import android.app.Application
import android.content.Context
import android.text.TextUtils
import android.util.Log
import java.util.concurrent.TimeUnit

/**
 * Created by Rob J on 05/11/17.
 */

class BillingManager protected constructor(context: Application) {

    private val billing: Billing

    init {
        Billing.init(context)
        billing = Billing.getInstance(context) //Keep billing alive
    }

    protected fun setTrialLengthInDays(context: Context, trialLengthInDays: Int) {
        PrefsUtils.writeLongPref(context, TRIAL_LENGTH, TimeUnit.DAYS.toMillis(trialLengthInDays.toLong()))
    }

    class Builder(private val context: Application) {
        private var trialLengthInDays = 14
        private var resetTrial = false

        fun setTrialLength(trialLengthInDays: Int): Builder {
            if (trialLengthInDays <= 0)
                throw RuntimeException("Trial length needs to be 1 day or more - default is 14")
            this.trialLengthInDays = trialLengthInDays
            return this
        }

        fun resetTrial(resetTrial: Boolean): Builder {
            this.resetTrial = resetTrial
            return this
        }

        fun build(): BillingManager {
            val billingManager = BillingManager(context)
            billingManager.setTrialLengthInDays(context, trialLengthInDays)
            if (resetTrial) {
                BillingManager.setTrialStartDate(context, System.currentTimeMillis())
                Log.d(TAG, "Trial reset..")
            }
            return billingManager
        }

    }

    companion object {

        private val TAG = BillingManager::class.java.simpleName

        private val TRIAL_START_DATE = "TRIAL_START_DATE"
        private val TRIAL_LENGTH = "TRIAL_LENGTH"
        private val PURCHASED_SKU = "PURCHASED_SKU"
        private val LAST_PURCHASED_CHECK_DATE = "LAST_PURCHASED_CHECK_DATE"

        private fun getTrialLengthInMillis(context: Context): Long {
            return PrefsUtils.readLongPref(context, TRIAL_LENGTH)
        }

        @JvmStatic
        fun isTrialStarted(context: Context): Boolean {
            return getTrialLengthInMillis(context) > 0 && getTrialStartDate(context) > 0
        }

        @JvmStatic
        fun isTrialPeriod(context: Context): Boolean {
            if (isPurchased(context) || !isTrialStarted(context))
                return false
            val diff = System.currentTimeMillis() - getTrialStartDate(context)
            return diff < getTrialLengthInMillis(context)
        }

        @JvmStatic
        fun hasPaidFeatures(context: Context): Boolean {
            return isPurchased(context) || isTrialPeriod(context)
        }

        @JvmStatic
        fun getTrialPeriodLeft(context: Context): Int {
            if (!isTrialPeriod(context))
                return 0
            var diff = System.currentTimeMillis() - getTrialStartDate(context)
            diff = getTrialLengthInMillis(context) - diff
            return Math.ceil(diff.toDouble() / 86400000).toInt()
        }

        @JvmStatic
        fun getTrialStartDate(context: Context): Long {
            return PrefsUtils.readLongPref(context, TRIAL_START_DATE)
        }

        internal fun setTrialStartDate(context: Context, installDateInMillis: Long) {
            PrefsUtils.writeLongPref(context, TRIAL_START_DATE, installDateInMillis)
        }

        @JvmStatic
        fun isPurchased(context: Context): Boolean {
            val sku = PrefsUtils.readStringPref(context, PURCHASED_SKU)
            return !TextUtils.isEmpty(sku)
        }

        @JvmStatic
        fun savePurchase(context: Context, sku: String?) {
            if (sku == null) {
                PrefsUtils.clearPref(context, PURCHASED_SKU)
                PrefsUtils.clearPref(context, LAST_PURCHASED_CHECK_DATE)
            } else
                PrefsUtils.writeStringPref(context, PURCHASED_SKU, sku)
        }

        @JvmStatic
        fun getPurchasedSku(context: Context): String {
            return PrefsUtils.readStringPref(context, PURCHASED_SKU)
        }

        @JvmStatic
        fun getLastPurchasedCheckDate(context: Context): Long {
            return PrefsUtils.readLongPref(context, LAST_PURCHASED_CHECK_DATE)
        }

        internal fun setLastPurchaseCheckedDate(context: Context, dateInMillis: Long) {
            PrefsUtils.writeLongPref(context, LAST_PURCHASED_CHECK_DATE, dateInMillis)
        }

        @JvmStatic
        fun startTrial(context: Context) {
            if (BillingManager.getTrialStartDate(context) == 0L) {
                BillingManager.setTrialStartDate(context, System.currentTimeMillis())
                Log.d(TAG, "Starting trial..")
            } else
                Log.d(TAG, "Trial already started..")
        }

        @JvmStatic
        fun forceTrialExpiry(context: Context) {
            setTrialStartDate(context, 1)
            Log.d(TAG, "Trial forcefully expired..")
        }
    }

}
