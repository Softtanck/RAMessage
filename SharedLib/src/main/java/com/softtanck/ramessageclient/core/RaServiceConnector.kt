package com.softtanck.ramessageclient.core

import android.content.ComponentName
import android.content.Context
import android.os.*
import android.util.Log
import com.softtanck.MESSAGE_BUNDLE_REPLY_TO_KEY
import com.softtanck.MESSAGE_REGISTER_CLIENT_REQ
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessageclient.core.engine.RaClientHandler

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
internal class RaServiceConnector(context: Context) : BaseServiceConnection<Parcelable>(context) {

    companion object {
        private val TAG: String = RaServiceConnector::class.java.simpleName
        private const val RAMESSENGER_DESCRIPTOR = "com.softtanck.ramessage.IRaMessenger"
    }

    // NOTE: This method works on Main thread.
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        Log.d(TAG, "[CLIENT] onServiceConnected : $name, serviceDesc:${service.interfaceDescriptor}, thread:${Thread.currentThread()}")
        outBoundMessenger = if (RAMESSENGER_DESCRIPTOR == service.interfaceDescriptor) {
            RaCustomMessenger(service)
        } else {
            Messenger(service)
        }
        if (outBoundMessenger != null) {
            RaClientHandler.INSTANCE.setOutBoundMessenger(outBoundMessenger)
            when (outBoundMessenger) {
                is RaCustomMessenger -> {
                    Log.d(TAG, "[CLIENT] Saved the custom messenger from server, now")
                    (outBoundMessenger as RaCustomMessenger).send(Message.obtain(null, MESSAGE_REGISTER_CLIENT_REQ).apply {
                        // replyTo InBoundMessenger as Messenger if the client is failed to reflect the messenger.
                        replyTo = RaClientHandler.INSTANCE.getInBoundMessenger() as Messenger
                        data = Bundle().apply {
                            putParcelable(MESSAGE_BUNDLE_REPLY_TO_KEY, RaClientHandler.INSTANCE.getInBoundMessenger())
                        }
                    })
                }
                is Messenger -> {
                    Log.w(TAG, "[CLIENT] Use the default messenger")
                    throw IllegalStateException("The default messenger is not supported")
                    // TODO : Default messenger is not supported
//                    (outBoundMessenger as Messenger).send(Message.obtain(null, MESSAGE_REGISTER_CLIENT_REQ).apply {
//                        replyTo = RaClientHandler.INSTANCE.getInBoundMessenger()
//                    })
                }
                else -> {
                    Log.e(TAG, "[CLIENT] Failed to send msg to server, Since outBoundMessenger type is unknown")
                }
            }
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