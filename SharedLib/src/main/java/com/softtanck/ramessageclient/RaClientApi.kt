/*
 * Copyright (C) 2023 Softtanck.
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
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.os.Parcelable
import com.softtanck.IRaMessageInterface
import com.softtanck.MESSAGE_BUNDLE_METHOD_NAME_KEY
import com.softtanck.MESSAGE_BUNDLE_TYPE_ARG_KEY
import com.softtanck.MESSAGE_BUNDLE_TYPE_PARAMETER_KEY
import com.softtanck.model.RaRequestTypeParameter
import com.softtanck.ramessageclient.core.BaseServiceConnection
import com.softtanck.ramessageclient.core.RaServiceConnector
import com.softtanck.ramessageclient.core.engine.retrofit.RaRetrofit
import com.softtanck.ramessageclient.core.listener.BindStatusChangedListener
import com.softtanck.ramessageclient.core.listener.ClientListenerManager
import com.softtanck.ramessageclient.core.listener.RaRemoteMessageListener
import com.softtanck.ramessageclient.core.model.RaClientBindStatus
import com.softtanck.ramessageclient.core.util.ResponseHandler
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: [getDefaultComponentName]、[addBindStatusListener]、[removeBindStatusListener]、[clearAllBindStatusListener]、[removeRemoteBroadcastMessageListener]
 */
class RaClientApi private constructor() {

    private val raRetrofit by lazy { RaRetrofit(false) }

    private val _innerDefaultComponentName by lazy { ComponentName(BaseServiceConnection::class.java.`package`?.name ?: "", BaseServiceConnection::class.java.name) }

    private val remoteConnections by lazy { mutableListOf<RaServiceConnector.RaRemoteConnection>() }

    companion object {
        @JvmStatic
        val INSTANCE: RaClientApi by lazy { RaClientApi() }
    }

    /**
     * Bind a remote service.
     * @param context the context
     * @param componentName the componentName of remote
     * @param bindStatusChangedListener the [BindStatusChangedListener]
     */
    @JvmOverloads
    fun bindRaConnectionService(context: Context, componentName: ComponentName, bindStatusChangedListener: BindStatusChangedListener? = null) {
        val localRaRemoteConnection = remoteConnections.find { it.serviceConnector.raClientBindStatus.componentName == componentName }
        val localRaServiceConnector = localRaRemoteConnection?.serviceConnector
        if (localRaServiceConnector == null) {
            val newLocalRaServiceConnector: RaServiceConnector
            val newLocalRaRemoteConnection: RaServiceConnector.RaRemoteConnection
            synchronized(remoteConnections) {
                newLocalRaServiceConnector = RaServiceConnector(context, RaClientBindStatus(componentName = componentName, bindStatus = MutableStateFlow(false), bindInProgress = MutableStateFlow(false)))
                newLocalRaRemoteConnection = RaServiceConnector.RaRemoteConnection(serviceConnector = newLocalRaServiceConnector)
                remoteConnections.add(newLocalRaRemoteConnection)
            }
            newLocalRaServiceConnector.bindRaConnectionService(componentName = componentName, bindStatusChangedListener = bindStatusChangedListener)
        } else {
            localRaServiceConnector.bindRaConnectionService(componentName = componentName, bindStatusChangedListener = bindStatusChangedListener)
        }
    }

    /**
     * Unbind the remote service, should be called on background thread.
     * @param componentName the componentName of remote
     */
    fun unbindRaConnectionService(componentName: ComponentName) {
        remoteConnections.find { it.serviceConnector.raClientBindStatus.componentName == componentName }?.run {
            serviceConnector.unbindRaConnectionService()
            synchronized(remoteConnections) {
                remoteConnections.remove(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    remoteConnections.removeIf { !it.serviceConnector.raClientBindStatus.bindStatus.value }
                } else {
                    remoteConnections.removeAll { !it.serviceConnector.raClientBindStatus.bindStatus.value }
                }
            }
            ClientListenerManager.INSTANCE.clearAllBindStatusChangedListener(componentName = componentName)
        }
    }

    fun getDefaultComponentName(): ComponentName = _innerDefaultComponentName

    /**
     * Added a bind listener
     * @param bindStatusChangedListener the [BindStatusChangedListener]
     */
    fun addBindStatusListener(componentName: ComponentName, bindStatusChangedListener: BindStatusChangedListener) {
        ClientListenerManager.INSTANCE.addBindStatusChangedListener(componentName, bindStatusChangedListener)
    }

    /**
     * Remove a bind listener
     * @param bindStatusChangedListener the [BindStatusChangedListener]
     */
    fun removeBindStatusListener(componentName: ComponentName, bindStatusChangedListener: BindStatusChangedListener) {
        ClientListenerManager.INSTANCE.removeBindStatusChangedListener(componentName, bindStatusChangedListener)
    }

    /**
     * Clear all bind listeners
     */
    fun clearAllBindStatusListener() {
        ClientListenerManager.INSTANCE.clearAllBindStatusChangedListener()
    }

    /**
     * Add a remote broadcast message listener
     * @param remoteMessageListener the [RaRemoteMessageListener]
     */
    fun addRemoteBroadcastMessageListener(componentName: ComponentName, remoteMessageListener: RaRemoteMessageListener) {
        ClientListenerManager.INSTANCE.addRemoteBroadCastMessageCallback(componentName, remoteMessageListener)
    }

    /**
     * Remove a remote broadcast message listener
     */
    fun removeRemoteBroadcastMessageListener(componentName: ComponentName, remoteMessageListener: RaRemoteMessageListener) {
        ClientListenerManager.INSTANCE.removeRemoteBroadCastMessageCallback(componentName, remoteMessageListener)
    }

    /**
     * Clear all remote broadcast message listeners
     */
    fun clearAllRemoteBroadcastMessageListener(componentName: ComponentName) {
        ClientListenerManager.INSTANCE.clearAllRemoteBroadCastMessageCallbacks(componentName)
    }

    /**
     * Current the client is bound to server.
     * ture bound, otherwise not.
     */
    fun isBoundToService(componentName: ComponentName) = remoteConnections.find { it.serviceConnector.raClientBindStatus.componentName == componentName }?.serviceConnector?.raClientBindStatus?.bindStatus ?: false

    /**
     * Destroy all resources.
     */
    fun destroyAllResources() {
        synchronized(remoteConnections) {
            remoteConnections.forEach { it.serviceConnector.unbindRaConnectionService() }
            remoteConnections.clear()
        }
        ClientListenerManager.INSTANCE.clearAllRemoteBroadCastMessageCallbacks()
        ClientListenerManager.INSTANCE.clearAllBindStatusChangedListener()
    }

    /**
     * Call a remote method with sync.
     * @param remoteMethodName the remote method name.
     * @param requestParameters the requestParameters.
     * @param requestArgs the requestArgs.
     */
    fun <T, F : Parcelable> remoteMethodCallSync(componentName: ComponentName, remoteMethodName: String, requestParameters: ArrayList<RaRequestTypeParameter>, requestArgs: ArrayList<F>): T? {
        val msg = remoteConnections.find { it.serviceConnector.raClientBindStatus.componentName == componentName }?.serviceConnector?.raClientHandler?.sendMsgToServerSync(Message.obtain().apply {
            val bundle = Bundle()
            bundle.putString(MESSAGE_BUNDLE_METHOD_NAME_KEY, remoteMethodName)
            bundle.putParcelableArrayList(MESSAGE_BUNDLE_TYPE_PARAMETER_KEY, requestParameters)
            bundle.putParcelableArrayList(MESSAGE_BUNDLE_TYPE_ARG_KEY, requestArgs)
            data = bundle
        })
        return ResponseHandler.makeupMessageForRsp(msg)
    }

    /**
     * Call a remote method with async.
     * @param componentName the componentName of remote
     * @param remoteMethodName the remote method name.
     * @param remoteMethodParameterTypes the requestParameters for find the remote method.
     * @param raRemoteMessageListener the [RaRemoteMessageListener], can be NULL.
     * @param args the requestArgs.
     */
    @JvmOverloads
    fun <T : Parcelable> remoteMethodCallAsync(componentName: ComponentName, remoteMethodName: String, remoteMethodParameterTypes: ArrayList<RaRequestTypeParameter>, args: ArrayList<T>, raRemoteMessageListener: RaRemoteMessageListener? = null) {
        remoteConnections.find { it.serviceConnector.raClientBindStatus.componentName == componentName }?.serviceConnector?.raClientHandler?.sendMsgToServerAsync(Message.obtain().apply {
            val bundle = Bundle()
            bundle.putString(MESSAGE_BUNDLE_METHOD_NAME_KEY, remoteMethodName)
            bundle.putParcelableArrayList(MESSAGE_BUNDLE_TYPE_PARAMETER_KEY, remoteMethodParameterTypes)
            bundle.putParcelableArrayList(MESSAGE_BUNDLE_TYPE_ARG_KEY, args)
            data = bundle
        }, raRemoteMessageListener)
    }

    /**
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     * Like retrofit.
     */
    fun <T : IRaMessageInterface> create(componentName: ComponentName, service: Class<T>): T = raRetrofit.create(componentName = componentName, service = service)

}