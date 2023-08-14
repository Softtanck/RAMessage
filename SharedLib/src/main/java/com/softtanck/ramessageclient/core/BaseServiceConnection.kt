package com.softtanck.ramessageclient.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.HandlerThread
import android.util.Log
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessageclient.core.engine.RaClientHandler
import com.softtanck.ramessageclient.core.listener.BindStatusChangedListener
import com.softtanck.ramessageclient.core.listener.ClientListenerManager
import com.softtanck.ramessageclient.core.model.RaClientBindStatus
import com.softtanck.ramessageclient.core.util.LockHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
abstract class BaseServiceConnection(val context: Context, val raClientBindStatus: RaClientBindStatus) : ServiceConnection {
    private val TAG: String = this.javaClass.simpleName

    private val workThreadHandler = HandlerThread(TAG)
    internal val raClientHandler by lazy {
        // Let the background thread started
        if (!workThreadHandler.isAlive) {
            workThreadHandler.start()
        }
        RaClientHandler(looper = workThreadHandler.looper, raClientBindStatus = raClientBindStatus)
    }

    // The unbind is triggered by manual, Like [unbindRaConnectionService]
    // This flag is for reconnect the service if the IBinder died from server.
    private val _isUnbindTriggeredByManualStateFlow = MutableStateFlow(false)
    val isUnbindTriggeredByManualStateFlow = _isUnbindTriggeredByManualStateFlow.asStateFlow()

    /**
     * Bind a remote service.
     * @param componentName the componentName
     * @param bindStatusChangedListener the [BindStatusChangedListener]
     */
    fun bindRaConnectionService(componentName: ComponentName, bindStatusChangedListener: BindStatusChangedListener?) {
        if (raClientBindStatus.bindInProgress.value) {
            Log.w(TAG, "[CLIENT] Binding is in progress, Ignore this request. Thread:${Thread.currentThread()}")
            // Add the listener if it is not NULL
            bindStatusChangedListener?.let { ClientListenerManager.INSTANCE.addBindStatusChangedListener(componentName = componentName, bindStatusChangedListener = bindStatusChangedListener) }
        } else {
            synchronized(LockHelper.BIND_IN_PROGRESS_OBJ_LOCK) {
                Log.d(TAG, "[CLIENT] Before CAS bindInProgress:${raClientBindStatus.bindInProgress.value}, Thread:${Thread.currentThread()}")
                if (raClientBindStatus.bindInProgress.value) {
                    Log.w(TAG, "[CLIENT] Binding is in progress, Ignore this request. Thread:${Thread.currentThread()}")
                    // Add the listener if it is not NULL
                    bindStatusChangedListener?.let { ClientListenerManager.INSTANCE.addBindStatusChangedListener(componentName = componentName, bindStatusChangedListener = bindStatusChangedListener) }
                    return@synchronized
                }
                Log.d(TAG, "[CLIENT] Binding to RaConnectionService. Thread:${Thread.currentThread()}")
                // Make sure the bind is changed success!!!
                raClientBindStatus.bindInProgress.value = true
                Log.d(TAG, "[CLIENT] After CAS bindInProgress:${raClientBindStatus.bindInProgress.value}, Thread:${Thread.currentThread()}")
                bindStatusChangedListener?.let { ClientListenerManager.INSTANCE.addBindStatusChangedListener(componentName = componentName, bindStatusChangedListener = bindStatusChangedListener) }
                val serviceIntent = Intent()
                serviceIntent.component = componentName
                serviceIntent.putExtra(RaCustomMessenger.raMsgVersion.first, RaCustomMessenger.raMsgVersion.second)
                /*
                 * These code from AOSP. :)
                 * public boolean filterEquals(Intent other) {
                 *         if (other == null) {
                 *             return false;
                 *         }
                 *         // Action，Uri，MIME type，PackageName，Component，Category
                 *         if (!Objects.equals(this.mAction, other.mAction)) return false;
                 *         if (!Objects.equals(this.mData, other.mData)) return false;
                 *         if (!Objects.equals(this.mType, other.mType)) return false;
                 *         if (!Objects.equals(this.mPackage, other.mPackage)) return false;
                 *         if (!Objects.equals(this.mComponent, other.mComponent)) return false;
                 *         if (!Objects.equals(this.mCategories, other.mCategories)) return false;
                 *
                 *         return true;
                 * }
                 */
                // If every action is different it makes the Service's onBind call every time, which is compatible with the old code. PLEASE DO NOT REMOVE THIS LINE!!!
                serviceIntent.action = RaCustomMessenger.raMsgVersion.toString()
                var bindServiceResult = false
                try {
                    bindServiceResult = context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)
                } catch (e: SecurityException) {
                    Log.w(TAG, "[CLIENT] Looks you are missing the permission, ${e.message}")
                } finally {
                    Log.d(TAG, "[CLIENT] Binding Api results:$bindServiceResult, Thread:${Thread.currentThread()}")
                    raClientBindStatus.bindInProgress.value = false
                    _isUnbindTriggeredByManualStateFlow.value = false
                    if (!bindServiceResult) { // Callback the result to user if the action is failed to execute
                        // Use the iterator to avoid CME!!!
                        val iterator = ClientListenerManager.INSTANCE.getAllBindStatusChangedListener(componentName = componentName).iterator()
                        while (iterator.hasNext()) {
                            iterator.next().onConnectRaServicesFailed(componentName)
                        }
                    }
                }
            }
        }
    }

    /**
     * Unbind the remove service
     */
    fun unbindRaConnectionService() {
        // Check current status with bound.
        if (raClientBindStatus.bindStatus.value) {
            synchronized(LockHelper.BIND_RESULT_OBJ_LOCK) {
                if (raClientBindStatus.bindStatus.value) {
                    Log.d(TAG, "[CLIENT] Unbinding from RaMessageService, Thread: ${Thread.currentThread()}")
                    // 1. Trying to send the message of disconnected to server if the connection is good.
                    raClientHandler.trySendDisconnectedToService()
                    raClientHandler.setOutBoundMessenger(null)
                    // 3. Last, unbind the service
                    context.unbindService(this)
                    // 4. mark the flag as true
                    _isUnbindTriggeredByManualStateFlow.value = true
                    Log.d(TAG, "unbindRaConnectionService: done")
                } else {
                    Log.w(TAG, "[CLIENT] Already in unbinding, Ignore this request, Thread: ${Thread.currentThread()}")
                }
            }
        }
    }
}