package com.softtanck.ramessageclient.core

import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.softtanck.MESSAGE_REGISTER_CLIENT_REQ
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessageclient.core.engine.RaClientHandler

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
internal class RaServiceConnector(context: Context) : BaseServiceConnection(context) {

    private val TAG: String = RaServiceConnector::class.java.simpleName

    // NOTE: This method works on Main thread.
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        Log.d(TAG, "[CLIENT] onServiceConnected : $name, thread:${Thread.currentThread()}")
        outBoundMessenger = RaCustomMessenger(service)
        if (outBoundMessenger != null) {
            RaClientHandler.INSTANCE.setOutBoundMessenger(outBoundMessenger)
            outBoundMessenger!!.send(Message.obtain(null, MESSAGE_REGISTER_CLIENT_REQ).apply {
                replyTo = RaClientHandler.INSTANCE.getInBoundMessenger()
            })
        } else {
            Log.e(TAG, "[CLIENT] Failed to send msg to server, Since outBoundMessenger is null")
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        Log.d(TAG, "[CLIENT] onServiceDisconnected : $name, thread:${Thread.currentThread()}")
        RaClientHandler.INSTANCE.onBindStatusChanged(false)
    }

    override fun onBindingDied(name: ComponentName) {
        Log.w(TAG, "[CLIENT] onBindingDied : $name")
    }

    override fun onNullBinding(name: ComponentName) {
        Log.w(TAG, "[CLIENT] onNullBinding : $name")
    }
}