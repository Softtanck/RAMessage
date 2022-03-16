package com.softtanck.ramessageclient.core.util

import android.os.Bundle
import android.os.Message
import android.os.Parcelable
import com.softtanck.*

// TODO： TBD
internal object ResponseHandler {

    fun <T> makeupMessageForRsp(message: Message?): T? {
        val serBundle: Bundle = message?.data ?: return null
        val remoteInvokeResult = serBundle.apply { classLoader = ResponseHandler.javaClass.classLoader }.run {
            when (getInt(MESSAGE_BUNDLE_RSP_TYPE_KEY)) {
                MESSAGE_BUNDLE_PARCELABLE_TYPE -> {
                    getParcelable<Parcelable>(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                }
                MESSAGE_BUNDLE_ARRAYLIST_TYPE -> {
                    getParcelableArrayList<Parcelable>(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
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