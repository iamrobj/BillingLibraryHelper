package com.robj.billinglibrary;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import static com.robj.billinglibrary.PrefsUtils.clearPref;
import static com.robj.billinglibrary.PrefsUtils.readLongPref;
import static com.robj.billinglibrary.PrefsUtils.readStringPref;
import static com.robj.billinglibrary.PrefsUtils.writeLongPref;
import static com.robj.billinglibrary.PrefsUtils.writeStringPref;

/**
 * Created by Rob J on 05/11/17.
 */

public class BillingManager {

    private static final String TAG = BillingManager.class.getSimpleName();

    private static final String TRIAL_START_DATE = "TRIAL_START_DATE";
    private static final String TRIAL_LENGTH = "TRIAL_LENGTH";
    private static final String PURCHASED_SKU = "PURCHASED_SKU";
    private static final String LAST_PURCHASED_CHECK_DATE = "LAST_PURCHASED_CHECK_DATE";

    protected BillingManager(Application context) {
        Billing.init(context);
    }

    private static long getTrialLengthInMillis(Context context) {
        return readLongPref(context, TRIAL_LENGTH);
    }

    protected void setTrialLengthInDays(Context context, int trialLengthInDays) {
        writeLongPref(context, TRIAL_LENGTH, TimeUnit.DAYS.toMillis(trialLengthInDays));
    }

    public static boolean isTrialStarted(Context context) {
        return getTrialLengthInMillis(context) > 0 && getTrialStartDate(context) > 0;
    }

    public static boolean isTrialPeriod(Context context) {
        if(isPurchased(context) || !isTrialStarted(context))
            return false;
        long diff = System.currentTimeMillis() - getTrialStartDate(context);
        return diff < getTrialLengthInMillis(context);
    }

    public static boolean hasPaidFeatures(Context context) {
        return isPurchased(context) || isTrialPeriod(context);
    }

    public static int getTrialPeriodLeft(Context context) {
        if(!isTrialPeriod(context))
            return 0;
        long diff = System.currentTimeMillis() - getTrialStartDate(context);
        diff = getTrialLengthInMillis(context) - diff;
        return (int) Math.ceil((double) diff/86400000);
    }

    public static long getTrialStartDate(Context context) {
        return readLongPref(context, TRIAL_START_DATE);
    }

    static void setTrialStartDate(Context context, long installDateInMillis) {
        writeLongPref(context, TRIAL_START_DATE, installDateInMillis);
    }

    public static boolean isPurchased(Context context) {
        String sku = readStringPref(context, PURCHASED_SKU);
        return !TextUtils.isEmpty(sku);
    }

    static void savePurchase(Context context, String sku) {
        if(sku == null) {
            clearPref(context, PURCHASED_SKU);
            clearPref(context, LAST_PURCHASED_CHECK_DATE);
        } else
            writeStringPref(context, PURCHASED_SKU, sku);
    }

    public static String getPurchasedSku(Context context) {
        return readStringPref(context, PURCHASED_SKU);
    }

    public static long getLastPurchasedCheckDate(Context context) {
        return readLongPref(context, LAST_PURCHASED_CHECK_DATE);
    }

    static void setLastPurchaseCheckedDate(Context context, long dateInMillis) {
        writeLongPref(context, LAST_PURCHASED_CHECK_DATE, dateInMillis);
    }

    public static void startTrial(Context context) {
        if(BillingManager.getTrialStartDate(context) == 0) {
            BillingManager.setTrialStartDate(context, System.currentTimeMillis());
            Log.d(TAG, "Starting trial..");
        } else
            Log.d(TAG, "Trial already started..");
    }

    public static void forceTrialExpiry(Context context) {
        setTrialStartDate(context, 1);
        Log.d(TAG, "Trial forcefully expired..");
    }

    public static class Builder {

        private final Application context;
        private int trialLengthInDays = 14;
        private boolean resetTrial = false;

        public Builder(Application context) {
            this.context = context;
        }

        public Builder setTrialLength(int trialLengthInDays) {
            if(trialLengthInDays <= 0)
                throw new RuntimeException("Trial length needs to be 1 day or more - default is 14");
            this.trialLengthInDays = trialLengthInDays;
            return this;
        }

        public Builder resetTrial(boolean resetTrial) {
            this.resetTrial = resetTrial;
            return this;
        }

        public BillingManager build() {
            BillingManager billingManager = new BillingManager(context);
            billingManager.setTrialLengthInDays(context, trialLengthInDays);
            if(resetTrial) {
                BillingManager.setTrialStartDate(context, System.currentTimeMillis());
                Log.d(TAG, "Trial reset..");
            }
            return billingManager;
        }

    }

}
