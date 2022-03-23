package com.softtanck.ramessageservice.intercept

import android.os.Bundle
import android.os.Message
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import com.softtanck.*
import com.softtanck.model.RaRequestTypeArg
import com.softtanck.model.RaRequestTypeParameter
import com.softtanck.ramessageservice.model.RaChain
import com.softtanck.ramessageservice.model.RaRemoteMethod
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Softtanck
 * @date 2022/3/23
 * Description: TODO
 */
class RaDefaultIntercept : RaResponseIntercept {
    private val TAG: String = RaDefaultIntercept::class.java.simpleName
    private val serviceMethodCache: MutableMap<String, RaRemoteMethod> = ConcurrentHashMap()

    override fun intercept(raChain: RaChain, message: Message, isSyncCall: Boolean): Message? {
        try {
            val serBundle: Bundle? = message.data?.apply { classLoader = this@RaDefaultIntercept.javaClass.classLoader }
            if (serBundle == null) {
                return raChain.proceed(message, isSyncCall)
            } else {
                val remoteName = serBundle.getString(MESSAGE_BUNDLE_METHOD_NAME_KEY)
                if (TextUtils.isEmpty(remoteName)) return raChain.proceed(message, isSyncCall)
                Log.d(TAG, "[SERVER] remoteMethodName:$remoteName")
                val requestParameters: ArrayList<RaRequestTypeParameter> = serBundle.getParcelableArrayList<RaRequestTypeParameter>(MESSAGE_BUNDLE_TYPE_PARAMETER_KEY) as ArrayList<RaRequestTypeParameter>
                val requestArgs: ArrayList<Parcelable> = serBundle.getParcelableArrayList<Parcelable>(MESSAGE_BUNDLE_TYPE_ARG_KEY) as ArrayList<Parcelable>
                val remoteMethod: Method = this.javaClass.getDeclaredMethod(remoteName!!, *Array(requestParameters.size) { requestParameters[it].parameterTypeClasses })
                remoteMethod.isAccessible = true
                // TODO : Method mesh should be implemented
                val remoteCallResult = remoteMethod.invoke(this, *Array(requestArgs.size) {
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

    private fun loadServiceMethod(remoteMethodName: String, requestParameters: ArrayList<RaRequestTypeParameter>): RaRemoteMethod? {
        var result = serviceMethodCache[remoteMethodName]
        if (result != null && isEqual(requestParameters, result.methodRequestParams)) {
            return result
        }
        synchronized(serviceMethodCache) {
            result = serviceMethodCache[remoteMethodName]
            if (result == null) {
                result = RaRemoteMethod(remoteMethodName, requestParameters)
                serviceMethodCache[remoteMethodName] = result!!
            }
        }
        return result
    }

    private fun <T> isEqual(first: List<T>, second: List<T>): Boolean {
        if (first.size != second.size) {
            return false
        }
        return first.zip(second).all { (x, y) -> x == y }
    }

}