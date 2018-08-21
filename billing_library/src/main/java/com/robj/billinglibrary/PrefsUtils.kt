package com.robj.billinglibrary

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Rob J on 05/11/17.
 */

internal object PrefsUtils {

    private val TAG = PrefsUtils::class.java.getSimpleName()

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(context.packageName + TAG, Context.MODE_PRIVATE)
    }

    fun clearPref(context: Context, name: String) {
        val editor = getSharedPreferences(context).edit()
        editor.remove(name)
        editor.apply()
    }

    fun writeStringPref(context: Context, name: String, s: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(name, s)
        editor.apply()
    }

    fun readStringPref(context: Context, name: String): String {
        val sp = getSharedPreferences(context)
        return sp.getString(name, "")
    }

    fun readLongPref(context: Context, name: String): Long {
        val sp = getSharedPreferences(context)
        return sp.getLong(name, 0)
    }

    fun writeLongPref(context: Context, name: String, l: Long) {
        val editor = getSharedPreferences(context).edit()
        editor.putLong(name, l)
        editor.apply()
    }

}
