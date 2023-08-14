package com.softtanck.ramessageclient.core

import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import android.util.Log
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessage.IRaMessenger
import com.softtanck.ramessageclient.RaClientApi
import com.softtanck.ramessageclient.core.model.RaClientBindStatus

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
internal class RaServiceConnector(context: Context, clientBindStatus: RaClientBindStatus) : BaseServiceConnection(context, clientBindStatus) {

    data class RaRemoteConnection(val serviceConnector: RaServiceConnector)

    companion object {
        private val TAG: String = RaServiceConnector::class.java.simpleName
    }

    // NOTE: This method works on Main thread.
    override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
        Log.d(TAG, "[CLIENT] onServiceConnected : $componentName, serviceDesc:${service.interfaceDescriptor}, thread:${Thread.currentThread()}")
        raClientHandler.setOutBoundMessenger(IRaMessenger.Stub.asInterface(service))
        if (!raClientHandler.sendRegisterMsgToServer(RaCustomMessenger(raClientHandler))) {
            Log.e(TAG, "[CLIENT] Failed to send msg to server, Since outBoundMessenger type is null")
        }
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        Log.d(TAG, "[CLIENT] onServiceDisconnected : $componentName, thread:${Thread.currentThread()}")
        raClientHandler.onBindStatusChanged(false)
    }

    override fun onBindingDied(componentName: ComponentName) {
        val unbindTriggeredByManual = isUnbindTriggeredByManualStateFlow.value
        Log.w(TAG, "[CLIENT] onBindingDied : $componentName, unbindTriggeredByManual:$unbindTriggeredByManual")
        if (!unbindTriggeredByManual) { // Retry logic is performed only the connection disconnected from the system.
            unbindRaConnectionService()
            // Looks like the server is dead, so we should try to reconnect.
            // TODO : Delay should be added?
            RaClientApi.INSTANCE.bindRaConnectionService(context = context, componentName = componentName)
        }
    }

    override fun onNullBinding(componentName: ComponentName) {
        Log.w(TAG, "[CLIENT] onNullBinding : $componentName")
    }
}