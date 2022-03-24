package com.softtanck.ramessageservice.util

import android.util.Log
import com.softtanck.model.RaRequestTypeParameter
import com.softtanck.ramessageservice.BaseConnectionService
import com.softtanck.ramessageservice.model.RaRemoteMethod
import com.softtanck.sharedlib.BuildConfig
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Softtanck
 * @date 2022/3/23
 * Description: TODO
 */
internal object ServerUtil {

    private val serviceMethodCache: MutableMap<String, RaRemoteMethod> = ConcurrentHashMap()

    private const val TAG = "ServerUtil"

    // TODO : TBD improve performance
    fun loadServiceMethod(remoteMethodName: String, requestParameters: ArrayList<RaRequestTypeParameter>, baseConnectionService: BaseConnectionService): RaRemoteMethod? {
        var result = serviceMethodCache[remoteMethodName]
        if (result != null && isEqual(requestParameters, result.methodRequestParams)) {
            if (BuildConfig.DEBUG) Log.d(TAG, "loadServiceMethod: Use the cached methods")
            return result
        }
        synchronized(serviceMethodCache) {
            result = serviceMethodCache[remoteMethodName]
            if (result == null) {
                try {
                    val remoteMethod: Method = baseConnectionService.javaClass.getDeclaredMethod(remoteMethodName, *Array(requestParameters.size) { requestParameters[it].parameterTypeClasses })
                    remoteMethod.isAccessible = true
                    result = RaRemoteMethod(remoteMethodName, requestParameters, remoteMethod)
                    serviceMethodCache[remoteMethodName] = result!!
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                    Log.e(TAG, "loadServiceMethod: remoteMethodName failed to found")
                }
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