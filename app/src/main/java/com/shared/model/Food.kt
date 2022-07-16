package com.shared.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Food(
    val name: String
) : Parcelable {

    override fun toString(): String {
        return "name:$name"
    }
}
