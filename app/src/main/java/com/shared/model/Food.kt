package com.shared.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Food(
    val name: String
) : Parcelable {

    override fun toString(): String {
        return "name:$name"
    }
}
