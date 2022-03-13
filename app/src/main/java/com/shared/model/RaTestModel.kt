package com.shared.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RaTestModel(val testString: String) : Parcelable {
    override fun toString(): String {
        return "testString:$testString"
    }
}
