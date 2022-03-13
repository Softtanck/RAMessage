package com.shared.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A common model in interface.
 * This is a sample
 */
@Parcelize
data class RaTestModel(val testString: String) : Parcelable {
    override fun toString(): String {
        return "testString:$testString"
    }
}
