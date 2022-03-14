/*
 * Copyright (C) 2022 Softtanck.
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
package com.softtanck.ramessageclient

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.os.Parcelable
import com.softtanck.*
import com.softtanck.model.RaRequestTypeArg
import com.softtanck.model.RaRequestTypeParameter
import com.softtanck.ramessageclient.core.BaseServiceConnection
import com.softtanck.ramessageclient.core.RaServiceConnector
import com.softtanck.ramessageclient.core.engine.RaClientHandler
import com.softtanck.ramessageclient.core.engine.retrofit.RaRetrofit
import com.softtanck.ramessageclient.core.listener.BindStateListener
import com.softtanck.ramessageclient.core.listener.BindStateListenerManager
import com.softtanck.ramessageclient.core.listener.RaRemoteMessageListener

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
class RaClientApi private constructor() {

    @Volatile
    private var _innerRaServiceConnector: BaseServiceConnection? = null

    private val raRetrofit by lazy { RaRetrofit(false) }

    // This is a TEST component
    private val _innerDefaultComponentName by lazy { ComponentName(BaseServiceConnection::class.java.`package`?.name ?: "", BaseServiceConnection::class.java.name) }

    companion object {
        @JvmStatic
        val INSTANCE: RaClientApi by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            RaClientApi()
        }
    }

    /**
     * Bind a remote service.
     * @param context the context
     * @param componentName the componentName of remote
     * @param bindStateListener the [BindStateListener]
     */
    @JvmOverloads
    fun bindRaConnectionService(context: Context, componentName: ComponentName, bindStateListener: BindStateListener? = null) {
        if (_innerRaServiceConnector == null) {
            synchronized(INSTANCE) {
                if (_innerRaServiceConnector == null) {
                    _innerRaServiceConnector = RaServiceConnector(context)
                }
            }
        }
        _innerRaServiceConnector!!.bindRaConnectionService(componentName, bindStateListener)
    }

    /**
     * Unbind the remote service
     */
    fun unbindRaConnectionService() {
        _innerRaServiceConnector?.unbindRaConnectionService()
    }

    fun getDefaultComponentName(): ComponentName = _innerDefaultComponentName

    /**
     * Added a bind listener
     * @param bindStateListener the [BindStateListener]
     */
    fun addBindStateListener(bindStateListener: BindStateListener) {
        BindStateListenerManager.INSTANCE.add(bindStateListener)
    }

    /**
     * Remove a bind listener
     * @param bindStateListener the [BindStateListener]
     */
    fun removeBindStateListener(bindStateListener: BindStateListener) {
        BindStateListenerManager.INSTANCE.remove(bindStateListener)
    }

    /**
     * Clear all bind listeners
     */
    fun clearAllBindStateListener() {
        BindStateListenerManager.INSTANCE.clearAll()
    }

    /**
     * Current the client is bound to server.
     * ture bound, otherwise not.
     */
    fun isBoundToService() = RaClientHandler.INSTANCE.clientIsBoundStatus()

    /**
     * Call a remote method with sync.
     * @param remoteMethodName the remote method name.
     * @param requestParameters the requestParameters.
     * @param requestArgs the requestArgs.
     */
    fun <T> remoteMethodCallSync(remoteMethodName: String, requestParameters: ArrayList<RaRequestTypeParameter>, requestArgs: ArrayList<RaRequestTypeArg>): T? {
        val msg = RaClientHandler.INSTANCE.sendMsgToServerSync(Message.obtain().apply {
            val bundle = Bundle()
            bundle.putString(MESSAGE_BUNDLE_METHOD_NAME_KEY, remoteMethodName)
            bundle.putParcelableArrayList(MESSAGE_BUNDLE_TYPE_PARAMETER_KEY, requestParameters)
            bundle.putParcelableArrayList(MESSAGE_BUNDLE_TYPE_ARG_KEY, requestArgs)
            data = bundle
        })
        val serBundle: Bundle = msg?.data ?: return null
        val remoteInvokeResult = serBundle.apply { classLoader = this@RaClientApi.javaClass.classLoader }.run {
            when (getInt(MESSAGE_BUNDLE_RSP_TYPE_KEY)) {
                MESSAGE_BUNDLE_PARCELABLE_TYPE -> {
                    getParcelable<Parcelable>(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                }
                MESSAGE_BUNDLE_ARRAYLIST_TYPE -> {
                    getParcelableArrayList<Parcelable>(MESSAGE_BUNDLE_NORMAL_RSP_KEY)
                }
                else -> {
                    null
                }
            }
        }
        return remoteInvokeResult as? T
    }

    /**
     * Call a remote method with async.
     * @param remoteMethodName the remote method name.
     * @param requestParameters the requestParameters.
     * @param raRemoteMessageListener the [RaRemoteMessageListener], can be NULL.
     */
    @JvmOverloads
    fun remoteMethodCallAsync(remoteMethodName: String, requestParameters: ArrayList<RaRequestTypeParameter>, args: ArrayList<RaRequestTypeArg>, raRemoteMessageListener: RaRemoteMessageListener? = null) {
        RaClientHandler.INSTANCE.sendMsgToServerAsync(Message.obtain().apply {
            val bundle = Bundle()
            bundle.putString(MESSAGE_BUNDLE_METHOD_NAME_KEY, remoteMethodName)
            bundle.putParcelableArrayList(MESSAGE_BUNDLE_TYPE_PARAMETER_KEY, requestParameters)
            bundle.putParcelableArrayList(MESSAGE_BUNDLE_TYPE_ARG_KEY, args)
            data = bundle
        }, raRemoteMessageListener)
    }

    /**
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     * Like retrofit.
     */
    fun <T> create(service: Class<T>): T = raRetrofit.create(service)

}