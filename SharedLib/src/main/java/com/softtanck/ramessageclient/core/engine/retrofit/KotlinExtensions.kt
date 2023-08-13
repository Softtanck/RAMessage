/*
 * Copyright (C) 2018 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("KotlinExtensions")

package com.softtanck.ramessageclient.core.engine.retrofit

import android.content.ComponentName
import android.os.Parcelable
import com.softtanck.model.RaRequestTypeParameter
import com.softtanck.ramessageclient.RaClientApi
import com.softtanck.ramessageclient.core.util.ResponseHandler
import com.softtanck.ramessageservice.util.ServerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.reflect.Method
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Invoke the normal or suspend method
 * @param componentName The component name of the remote service
 * @param methodName The method name of the remote service
 * @param parameterTypes The parameters of the remote service. used to find the method
 * @param argsList The arguments of the remote service
 */
suspend fun <T, F : Parcelable> awaitResponse(componentName: ComponentName, methodName: String, parameterTypes: ArrayList<RaRequestTypeParameter>, argsList: ArrayList<F>): T? =
    suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            // TODO : Remove?
        }
        RaClientApi.INSTANCE.remoteMethodCallAsync(componentName = componentName, remoteMethodName = methodName, remoteMethodParameterTypes = parameterTypes, args = argsList) { message ->
            if (continuation.isActive) continuation.resume(ResponseHandler.makeupMessageForRsp(message))
        }
    }

/**
 * Force the calling coroutine to suspend before throwing [this].
 *
 * This is needed when a checked exception is synchronously caught in a [java.lang.reflect.Proxy]
 * invocation to avoid being wrapped in [java.lang.reflect.UndeclaredThrowableException].
 *
 * The implementation is derived from:
 * https://github.com/Kotlin/kotlinx.coroutines/pull/1667#issuecomment-556106349
 */
internal suspend fun Exception.suspendAndThrow(): Nothing {
    suspendCoroutineUninterceptedOrReturn<Nothing> { continuation ->
        Dispatchers.Default.dispatch(continuation.context) {
            continuation.intercepted().resumeWithException(this@suspendAndThrow)
        }
        COROUTINE_SUSPENDED
    }
}

/**
 * Invoke the normal or suspend method.
 * @param obj obj in the object
 * @param args any parameters for this the method
 */
fun Method.invokeCompat(obj: Any, vararg args: Any?): Any? {
    val isSuspendMethod = ServerUtil.isSuspendMethod(this)
    return if (isSuspendMethod) {
        runBlocking { this@invokeCompat.invokeSuspend(obj, *args) }
    } else {
        this.invoke(obj, *args)
    }
}

/**
 * Invoke the suspend method.
 * @param obj obj in the object
 * @param args any parameters for this the method
 */
suspend fun Method.invokeSuspend(obj: Any, vararg args: Any?): Any? = suspendCoroutineUninterceptedOrReturn { cont ->
    invoke(obj, *args, cont)
}