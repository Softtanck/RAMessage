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

import android.os.Parcelable
import com.softtanck.model.RaRequestTypeParameter
import com.softtanck.ramessageclient.RaClientApi
import com.softtanck.ramessageclient.core.util.ResponseHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


suspend fun <T, F : Parcelable> awaitResponse(methodName: String, parameters: ArrayList<RaRequestTypeParameter>, argsList: ArrayList<F>): T? =
    suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            // TODO : Remove?
        }
        RaClientApi.INSTANCE.remoteMethodCallAsync(methodName, parameters, argsList) { message ->
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
