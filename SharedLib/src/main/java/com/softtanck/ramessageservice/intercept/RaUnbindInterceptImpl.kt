package com.softtanck.ramessageservice.intercept

import android.os.Message
import android.util.Log
import com.softtanck.MESSAGE_CLIENT_DISCONNECT_REQ
import com.softtanck.ramessageservice.engine.RaClientManager
import com.softtanck.ramessageservice.model.RaChain

/**
 * @author Softtanck
 * @date 2023/11/21
 * Description: to handle the unbind event
 * @param serviceKey the service key for the client
 */
internal class RaUnbindInterceptImpl(private val serviceKey: String) : IRaResponseIntercept {
    companion object {
        private const val TAG = "RaUnbindIntercept"
    }

    override fun intercept(raChain: RaChain, message: Message, isSyncCall: Boolean): Message? {
        return runCatching {
            if (message.what == MESSAGE_CLIENT_DISCONNECT_REQ) {
                Log.d(TAG, "[SERVER] onDisconnect coming: $message, what:${message.what} isSyncCall:$isSyncCall, trxID:${message.arg1}, serviceKey:${serviceKey}, thread:${Thread.currentThread()}")
                RaClientManager.removeClientFromBinderWithMessage(msg = message, serviceKey = serviceKey)
                null
            } else {
                raChain.proceed(message, isSyncCall)
            }
        }.getOrNull()
    }
}