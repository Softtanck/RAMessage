package com.softtanck.ramessageservice.engine

import android.os.Looper
import android.os.Message
import android.util.Log
import com.softtanck.MESSAGE_CLIENT_SINGLE_REQ
import com.softtanck.MESSAGE_REGISTER_CLIENT_REQ
import com.softtanck.ramessageservice.BaseConnectionService

internal class RaServerHandler internal constructor(looper: Looper, private val baseConnectionService: BaseConnectionService) : BaseServerSyncHandler(looper, baseConnectionService) {
    private val TAG = this.javaClass.name

    override fun handleMessage(msg: Message) {
        Log.d(TAG, "[SERVER] RaServerHandler handleMessage: ${msg.what}")
        when (msg.what) {
            MESSAGE_REGISTER_CLIENT_REQ -> {
                RaClientManager.registerClientFromBinderWithMessage(serviceKey = baseConnectionService.javaClass.name, msg = msg)
            }
            MESSAGE_CLIENT_SINGLE_REQ -> {
                baseConnectionService.onRemoteMessageArrived(msg, false)
            }
            else -> {
                Log.d(TAG, "[SERVER] Not in protocol, Discarding the message. msg:$msg")
            }
        }
    }
}