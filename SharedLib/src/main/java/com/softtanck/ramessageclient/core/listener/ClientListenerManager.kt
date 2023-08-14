package com.softtanck.ramessageclient.core.listener

import android.content.ComponentName
import android.os.Build

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
// TODO : How about weakReference? looks like the WeakReference is not necessary. And will be removed in the future.
internal class ClientListenerManager private constructor() {
    companion object {
        @JvmStatic
        val INSTANCE: ClientListenerManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            ClientListenerManager()
        }
    }

    // first: componentName second: BindStatusListener
    private val clientsBindStatusChangedListenerList by lazy { LinkedHashSet<Pair<ComponentName, BindStatusChangedListener>>() }

    /**
     * Remember all callbacks from the client. And WeakReference is used as value.
     * That can be void memory leaks here.
     */
    private val broadcastCallbacks by lazy { mutableListOf<Pair<ComponentName, RaRemoteMessageListener>>() }

    fun addBindStatusChangedListener(componentName: ComponentName, bindStatusChangedListener: BindStatusChangedListener) {
        synchronized(clientsBindStatusChangedListenerList) {
            clientsBindStatusChangedListenerList.add(Pair(first = componentName, second = bindStatusChangedListener))
        }
    }

    fun removeBindStatusChangedListener(componentName: ComponentName, bindStatusChangedListener: BindStatusChangedListener) {
        synchronized(clientsBindStatusChangedListenerList) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                clientsBindStatusChangedListenerList.removeIf { it.first == componentName && it.second == bindStatusChangedListener }
            } else {
                clientsBindStatusChangedListenerList.remove(Pair(first = componentName, second = bindStatusChangedListener))
            }
        }
    }

    fun clearAllBindStatusChangedListener() {
        synchronized(clientsBindStatusChangedListenerList) {
            clientsBindStatusChangedListenerList.clear()
        }
    }

    fun clearAllBindStatusChangedListener(componentName: ComponentName) {
        synchronized(clientsBindStatusChangedListenerList) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                clientsBindStatusChangedListenerList.removeIf { it.first == componentName }
            } else {
                clientsBindStatusChangedListenerList.removeAll { it.first == componentName }
            }
        }
    }

    // TIPS: use the list of copy
    fun getAllBindStatusChangedListener(componentName: ComponentName): List<BindStatusChangedListener> = clientsBindStatusChangedListenerList.filter { it.first == componentName }.map { it.second }

    /**
     * Add a broadcast callback to the list.
     * @param remoteMessageListener The callback to be added.
     */
    fun addRemoteBroadCastMessageCallback(componentName: ComponentName, remoteMessageListener: RaRemoteMessageListener) {
        synchronized(broadcastCallbacks) {
            broadcastCallbacks.add(Pair(componentName, remoteMessageListener))
        }
    }

    /**
     * Remove a broadcast callback from the list.
     */
    fun removeRemoteBroadCastMessageCallback(componentName: ComponentName, remoteMessageListener: RaRemoteMessageListener) {
        synchronized(broadcastCallbacks) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                broadcastCallbacks.removeIf { it.first == componentName && it.second == remoteMessageListener }
            } else {
                broadcastCallbacks.removeAll { it.first == componentName && it.second == remoteMessageListener }
            }
        }
    }

    /**
     * Clear all broadcast callbacks.
     */
    fun clearAllRemoteBroadCastMessageCallbacks(componentName: ComponentName) {
        synchronized(broadcastCallbacks) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                broadcastCallbacks.removeIf { it.first == componentName }
            } else {
                broadcastCallbacks.removeAll { it.first == componentName }
            }
        }
    }

    fun clearAllRemoteBroadCastMessageCallbacks() {
        synchronized(broadcastCallbacks) {
            broadcastCallbacks.clear()
        }
    }

    fun getAllRemoteBroadCastMessageCallbacks(componentName: ComponentName): List<RaRemoteMessageListener> = broadcastCallbacks.filter { it.first == componentName }.map { it.second }
}