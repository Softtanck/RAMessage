package com.softtanck.ramessageservice.intercept

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
internal class RaDefaultIntercept : RaResponseIntercept {

    companion object {
        private const val TAG = "RaDefaultIntercept"
    }

    override fun intercept(raChain: RaChain, message: Message, isSyncCall: Boolean): Message? {
        try {
            val serBundle: Bundle? = message.data?.apply { classLoader = this@RaDefaultIntercept.javaClass.classLoader }
            if (serBundle == null) {
                return raChain.proceed(message, isSyncCall)
            } else {
                val remoteMethodName = serBundle.getString(MESSAGE_BUNDLE_METHOD_NAME_KEY)
                if (TextUtils.isEmpty(remoteMethodName)) return raChain.proceed(message, isSyncCall)
                Log.d(TAG, "[SERVER] remoteMethodName:$remoteMethodName")
                val requestParameters: ArrayList<RaRequestTypeParameter> = serBundle.getParcelableArrayList<RaRequestTypeParameter>(MESSAGE_BUNDLE_TYPE_PARAMETER_KEY) as ArrayList<RaRequestTypeParameter>
                val loadServiceMethod = ServerUtil.loadServiceMethod(remoteMethodName!!, requestParameters, raChain.baseConnectionService)
                val requestArgs: ArrayList<Parcelable> = serBundle.getParcelableArrayList<Parcelable>(MESSAGE_BUNDLE_TYPE_ARG_KEY) as ArrayList<Parcelable>
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
                            // FIXME : 这儿可能会crash，如果是List<String>.因为这儿强制要求的是一个Parcelable.
                            putInt(MESSAGE_BUNDLE_RSP_TYPE_KEY, MESSAGE_BUNDLE_ARRAYLIST_TYPE)
                            putParcelableArrayList(MESSAGE_BUNDLE_NORMAL_RSP_KEY, remoteCallResult as java.util.ArrayList<out Parcelable>?)
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

}