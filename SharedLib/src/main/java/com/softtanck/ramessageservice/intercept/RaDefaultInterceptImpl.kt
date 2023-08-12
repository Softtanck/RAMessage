package com.softtanck.ramessageservice.intercept

import android.os.Build
import android.os.Bundle
import android.os.Message
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import com.softtanck.*
import com.softtanck.model.RaRequestTypeArg
import com.softtanck.model.RaRequestTypeParameter
import com.softtanck.ramessageclient.core.engine.retrofit.invokeCompat
import com.softtanck.ramessageservice.model.RaChain
import com.softtanck.ramessageservice.util.ServerUtil

/**
 * @author Softtanck
 * @date 2022/3/23
 * Description: TODO
 */
internal class RaDefaultInterceptImpl : IRaResponseIntercept {

    companion object {
        private const val TAG = "RaDefaultIntercept"
    }

    override fun intercept(raChain: RaChain, message: Message, isSyncCall: Boolean): Message? {
        try {
            val serBundle: Bundle? = message.data?.apply { classLoader = this@RaDefaultInterceptImpl.javaClass.classLoader }
            if (serBundle == null) {
                return raChain.proceed(message, isSyncCall)
            } else {
                val remoteMethodName = serBundle.getString(MESSAGE_BUNDLE_METHOD_NAME_KEY)
                if (TextUtils.isEmpty(remoteMethodName)) return raChain.proceed(message, isSyncCall)
                Log.d(TAG, "[SERVER] remoteMethodName:$remoteMethodName, thread:${Thread.currentThread()}")
                val requestParameters: ArrayList<RaRequestTypeParameter> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    serBundle.getParcelableArrayList(MESSAGE_BUNDLE_TYPE_PARAMETER_KEY, RaRequestTypeParameter::class.java) as ArrayList<RaRequestTypeParameter>
                } else {
                    @Suppress("DEPRECATION")
                    serBundle.getParcelableArrayList<RaRequestTypeParameter>(MESSAGE_BUNDLE_TYPE_PARAMETER_KEY) as ArrayList<RaRequestTypeParameter>
                }
                val loadServiceMethod = ServerUtil.loadServiceMethod(remoteMethodName!!, requestParameters, raChain.baseConnectionService)
                val requestArgs: ArrayList<Parcelable> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    serBundle.getParcelableArrayList(MESSAGE_BUNDLE_TYPE_ARG_KEY, Parcelable::class.java) as ArrayList<Parcelable>
                } else {
                    @Suppress("DEPRECATION")
                    serBundle.getParcelableArrayList<Parcelable>(MESSAGE_BUNDLE_TYPE_ARG_KEY) as ArrayList<Parcelable>
                }
                if (loadServiceMethod == null) {
                    Log.e(TAG, "[SERVER] loadServiceMethod is null")
                    return raChain.proceed(message, isSyncCall)
                }
                val remoteCallResult = loadServiceMethod.method.invokeCompat(raChain.baseConnectionService, *Array(requestArgs.size) {
                    if (requestArgs[it] is RaRequestTypeArg) {
                        (requestArgs[it] as RaRequestTypeArg).arg
                    } else {
                        requestArgs[it]
                    }
                })
                message.arg1 = message.arg1 // Remember the callback key
                message.data = Bundle().apply {
                    when (remoteCallResult) {
                        is Parcelable -> {
                            putInt(MESSAGE_BUNDLE_RSP_TYPE_KEY, MESSAGE_BUNDLE_PARCELABLE_TYPE)
                            putParcelable(MESSAGE_BUNDLE_NORMAL_RSP_KEY, remoteCallResult as Parcelable?)
                        }
                        is ArrayList<*> -> {
                            val tempTypedStringArrayList = remoteCallResult.asListOfType<String>()
                            val tempTypedIntArrayList = remoteCallResult.asListOfType<Int>()
                            val tempTypedParcelableArrayList = remoteCallResult.asListOfType<Parcelable>()
                            val tempTypedCharSequenceArrayList = remoteCallResult.asListOfType<CharSequence>()
                            when {
                                tempTypedStringArrayList != null -> {
                                    putInt(MESSAGE_BUNDLE_RSP_TYPE_KEY, MESSAGE_BUNDLE_ARRAYLIST_STRING_TYPE)
                                    putStringArrayList(MESSAGE_BUNDLE_NORMAL_RSP_KEY, tempTypedStringArrayList)
                                }
                                tempTypedIntArrayList != null -> {
                                    putInt(MESSAGE_BUNDLE_RSP_TYPE_KEY, MESSAGE_BUNDLE_ARRAYLIST_INTEGER_TYPE)
                                    putIntegerArrayList(MESSAGE_BUNDLE_NORMAL_RSP_KEY, tempTypedIntArrayList)
                                }
                                tempTypedParcelableArrayList != null -> {
                                    putInt(MESSAGE_BUNDLE_RSP_TYPE_KEY, MESSAGE_BUNDLE_ARRAYLIST_PARCELABLE_TYPE)
                                    putParcelableArrayList(MESSAGE_BUNDLE_NORMAL_RSP_KEY, tempTypedParcelableArrayList)
                                }
                                tempTypedCharSequenceArrayList != null -> {
                                    putInt(MESSAGE_BUNDLE_RSP_TYPE_KEY, MESSAGE_BUNDLE_ARRAYLIST_CHAR_SEQUENCE_TYPE)
                                    putCharSequenceArrayList(MESSAGE_BUNDLE_NORMAL_RSP_KEY, tempTypedCharSequenceArrayList)
                                }
                            }
                        }
                        is Boolean -> {
                            putInt(MESSAGE_BUNDLE_RSP_TYPE_KEY, MESSAGE_BUNDLE_BOOLEAN_TYPE)
                            putBoolean(MESSAGE_BUNDLE_NORMAL_RSP_KEY, remoteCallResult)
                        }
                        is Char -> {
                            putInt(MESSAGE_BUNDLE_RSP_TYPE_KEY, MESSAGE_BUNDLE_CHAR_TYPE)
                            putChar(MESSAGE_BUNDLE_NORMAL_RSP_KEY, remoteCallResult)
                        }
                        is String -> {
                            putInt(MESSAGE_BUNDLE_RSP_TYPE_KEY, MESSAGE_BUNDLE_STRING_TYPE)
                            putString(MESSAGE_BUNDLE_NORMAL_RSP_KEY, remoteCallResult)
                        }
                        is Byte -> {
                            putInt(MESSAGE_BUNDLE_RSP_TYPE_KEY, MESSAGE_BUNDLE_BYTE_TYPE)
                            putByte(MESSAGE_BUNDLE_NORMAL_RSP_KEY, remoteCallResult)
                        }
                    }
                    // TODO ： More details will be implemented
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "[SERVER] Remote call failed:${e.message}")
            return null
        }
        return raChain.proceed(message, isSyncCall)
    }

    // https://kotlinlang.org/docs/typecasts.html#unchecked-casts
    private inline fun <reified T> ArrayList<*>.asListOfType(): ArrayList<T>? = if (all { it is T })
        @Suppress("UNCHECKED_CAST")
        this as ArrayList<T> else
        null
}