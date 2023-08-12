package com.softtanck.ramessageclient.core.util

import android.os.Build
import android.os.Bundle
import android.os.Message
import android.os.Parcelable
import com.softtanck.MESSAGE_BUNDLE_ARRAYLIST_CHAR_SEQUENCE_TYPE
import com.softtanck.MESSAGE_BUNDLE_ARRAYLIST_INTEGER_TYPE
import com.softtanck.MESSAGE_BUNDLE_ARRAYLIST_PARCELABLE_TYPE
import com.softtanck.MESSAGE_BUNDLE_ARRAYLIST_STRING_TYPE
import com.softtanck.MESSAGE_BUNDLE_BOOLEAN_TYPE
import com.softtanck.MESSAGE_BUNDLE_BYTE_TYPE
import com.softtanck.MESSAGE_BUNDLE_CHAR_TYPE
import com.softtanck.MESSAGE_BUNDLE_NORMAL_RSP_KEY
import com.softtanck.MESSAGE_BUNDLE_PARCELABLE_TYPE
import com.softtanck.MESSAGE_BUNDLE_RSP_TYPE_KEY
import com.softtanck.MESSAGE_BUNDLE_STRING_TYPE

// TODO： TBD
internal object ResponseHandler {

    @Suppress("UNCHECKED_CAST")
    fun <T> makeupMessageForRsp(message: Message?): T? {
        val serBundle: Bundle = message?.data ?: return null
        val remoteInvokeResult = serBundle.apply { classLoader = ResponseHandler.javaClass.classLoader }.run {
            when (getInt(MESSAGE_BUNDLE_RSP_TYPE_KEY)) {
                MESSAGE_BUNDLE_PARCELABLE_TYPE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        getParcelable(MESSAGE_BUNDLE_NORMAL_RSP_KEY, Parcelable::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        getParcelable(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                    }
                }

                MESSAGE_BUNDLE_ARRAYLIST_CHAR_SEQUENCE_TYPE -> {
                    getCharSequenceArrayList(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                }

                MESSAGE_BUNDLE_ARRAYLIST_INTEGER_TYPE -> {
                    getIntegerArrayList(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                }

                MESSAGE_BUNDLE_ARRAYLIST_STRING_TYPE -> {
                    getStringArrayList(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                }

                MESSAGE_BUNDLE_ARRAYLIST_PARCELABLE_TYPE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        getParcelableArrayList(MESSAGE_BUNDLE_NORMAL_RSP_KEY, Parcelable::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        getParcelableArrayList(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                    }
                }

                MESSAGE_BUNDLE_BOOLEAN_TYPE -> {
                    getBoolean(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                }

                MESSAGE_BUNDLE_CHAR_TYPE -> {
                    getChar(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                }

                MESSAGE_BUNDLE_STRING_TYPE -> {
                    getString(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                }

                MESSAGE_BUNDLE_BYTE_TYPE -> {
                    getByte(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                }

                else -> {
                    null
                }
            }
        }
        return remoteInvokeResult as? T
    }
}