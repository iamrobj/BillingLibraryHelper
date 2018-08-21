package com.robj.billinglibrary.models

import java.util.NoSuchElementException

import io.reactivex.annotations.Nullable

/**
 * Created by Rob J on 21/09/17.
 */

class Optional<T> internal constructor(@param:Nullable private val optional: T?) {

    val isEmpty: Boolean
        get() = this.optional == null

    fun get(): T {
        if (optional == null)
            throw NoSuchElementException("Item was null..")
        return optional
    }
}
