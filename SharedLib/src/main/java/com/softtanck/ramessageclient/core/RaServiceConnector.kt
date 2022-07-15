package com.softtanck.ramessageclient.core

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.softtanck.MESSAGE_BUNDLE_REPLY_TO_KEY
import com.softtanck.MESSAGE_REGISTER_CLIENT_REQ
import com.softtanck.ramessage.IRaMessenger
import com.softtanck.ramessageclient.RaClientApi
import com.softtanck.ramessageclient.core.engine.RaClientHandler

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
internal class RaServiceConnector(context: Context) : BaseServiceConnection(context) {

    companion object {
        private val TAG: String = RaServiceConnector::class.java.simpleName
    }

    // NOTE: This method works on Main thread.
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        Log.d(TAG, "[CLIENT] onServiceConnected : $name, serviceDesc:${service.interfaceDescriptor}, thread:${Thread.currentThread()}")
        outBoundMessenger = IRaMessenger.Stub.asInterface(service)
        RaClientHandler.INSTANCE.setOutBoundMessenger(outBoundMessenger)
        outBoundMessenger?.send(Message.obtain(null, MESSAGE_REGISTER_CLIENT_REQ).apply {
            // replyTo InBoundMessenger as Messenger if the client is failed to reflect the messenger.
            data = Bundle().apply {
                putParcelable(MESSAGE_BUNDLE_REPLY_TO_KEY, RaClientHandler.INSTANCE.getInBoundMessenger())
            }
        }) ?: Log.e(TAG, "[CLIENT] Failed to send msg to server, Since outBoundMessenger type is null")
    }

    override fun onServiceDisconnected(name: ComponentName) {
        Log.d(TAG, "[CLIENT] onServiceDisconnected : $name, thread:${Thread.currentThread()}")
        RaClientHandler.INSTANCE.onBindStatusChanged(false)
    }

    override fun onBindingDied(name: ComponentName) {
        val unbindTriggeredByManual = isUnbindTriggeredByManual()
        Log.w(TAG, "[CLIENT] onBindingDied : $name, unbindTriggeredByManual:$unbindTriggeredByManual")
        if (!unbindTriggeredByManual) { // Retry logic is performed only the connection disconnected from the system.
            unbindRaConnectionService()
            // Looks like the server is dead, so we should try to reconnect.
            // TODO : Delay should be added?
            RaClientApi.INSTANCE.bindRaConnectionService(context, name)
        }
    }

    override fun onNullBinding(name: ComponentName) {
        Log.w(TAG, "[CLIENT] onNullBinding : $name")
    }
}