package com.softtanck.ramessageclient.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Parcelable
import android.util.Log
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessageclient.core.engine.RaClientHandler
import com.softtanck.ramessageclient.core.listener.BindStateListener
import com.softtanck.ramessageclient.core.listener.BindStateListenerManager
import com.softtanck.ramessageclient.core.util.LockHelper
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
abstract class BaseServiceConnection<T : Parcelable>(val context: Context) : ServiceConnection {
    private val TAG: String = this.javaClass.simpleName

    protected var outBoundMessenger: T? = null

    // The unbind is triggered by manual, Like [unbindRaConnectionService]
    // This flag is for reconnect the service if the IBinder died from server.
    private val isUnbindTriggeredByManual = AtomicBoolean(false)

    // Current bind is in progress, default is false.
    private val bindInProgress = AtomicBoolean(false)

    private fun makeBindInProgressUseCAS(isInProgress: Boolean) {
        // Make sure the bind is changed success!!!
        @Suppress("ControlFlowWithEmptyBody")
        while (!bindInProgress.compareAndSet(bindInProgress.get(), isInProgress));
    }

    private fun makeIsUnbindTriggeredByManualUseCAS(unbindTriggeredByManual: Boolean) {
        // Make sure the unbind is changed success!!!
        @Suppress("ControlFlowWithEmptyBody")
        while (!isUnbindTriggeredByManual.compareAndSet(isUnbindTriggeredByManual.get(), unbindTriggeredByManual));
    }

    fun isUnbindTriggeredByManual(): Boolean {
        return isUnbindTriggeredByManual.get()
    }

    /**
     * Bind a remote service.
     * @param componentName the componentName
     * @param bindStateListener the [BindStateListener]
     */
    fun bindRaConnectionService(componentName: ComponentName, bindStateListener: BindStateListener?) {
        if (bindInProgress.get()) {
            Log.w(TAG, "[CLIENT] Binding is in progress, Ignore this request. Thread:${Thread.currentThread()}")
            // Add the listener if it is not NULL
            bindStateListener?.let { BindStateListenerManager.INSTANCE.add(bindStateListener) }
        } else {
            synchronized(LockHelper.BIND_IN_PROGRESS_OBJ_LOCK) {
                Log.d(TAG, "[CLIENT] Before CAS bindInProgress:${bindInProgress.get()}, Thread:${Thread.currentThread()}")
                if (bindInProgress.get()) {
                    Log.w(TAG, "[CLIENT] Binding is in progress, Ignore this request. Thread:${Thread.currentThread()}")
                    // Add the listener if it is not NULL
                    bindStateListener?.let { BindStateListenerManager.INSTANCE.add(bindStateListener) }
                    return@synchronized
                }
                Log.d(TAG, "[CLIENT] Binding to RaConnectionService. Thread:${Thread.currentThread()}")
                // Make sure the bind is changed success!!!
                makeBindInProgressUseCAS(true)
                Log.d(TAG, "[CLIENT] After CAS bindInProgress:${bindInProgress.get()}, Thread:${Thread.currentThread()}")
                bindStateListener?.let { BindStateListenerManager.INSTANCE.add(bindStateListener) }
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
                    makeBindInProgressUseCAS(false)
                    makeIsUnbindTriggeredByManualUseCAS(false)
                    if (!bindServiceResult) { // Callback the result to user if the action is failed to execute
                        // Use the iterator to avoid CME!!!
                        val iterator = BindStateListenerManager.INSTANCE.getAllListener().iterator()
                        while (iterator.hasNext()) {
                            iterator.next().onConnectRaServicesFailed()
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
        if (RaClientHandler.INSTANCE.clientIsBoundStatus()) {
            synchronized(LockHelper.BIND_RESULT_OBJ_LOCK) {
                if (RaClientHandler.INSTANCE.clientIsBoundStatus()) {
                    Log.d(TAG, "[CLIENT] Unbinding from RaMessageService, Thread: ${Thread.currentThread()}")
                    // 1. Trying to send the message of disconnected to server if the connection is good.
                    RaClientHandler.INSTANCE.trySendDisconnectedToService()
                    // 2. Null the outBoundMessenger
                    outBoundMessenger = null
                    RaClientHandler.INSTANCE.setOutBoundMessenger(null)
                    // 3. Last, unbind the service
                    context.unbindService(this)
                    // 4. mark the flag as true
                    makeIsUnbindTriggeredByManualUseCAS(true)
                    Log.d(TAG, "unbindRaConnectionService: done")
                } else {
                    Log.w(TAG, "[CLIENT] Already in unbinding, Ignore this request, Thread: ${Thread.currentThread()}")
                }
            }
        }
    }
}