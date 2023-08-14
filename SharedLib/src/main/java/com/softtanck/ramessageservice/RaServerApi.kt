package com.softtanck.ramessageservice

import android.os.Message
import com.softtanck.MESSAGE_CLIENT_BROADCAST_RSP
import com.softtanck.ramessageservice.engine.RaClientManager

/**
 * @author Softtanck
 * @date 2022/3/28
 * Description: TODO
 */
class RaServerApi private constructor() {
    companion object {
        @JvmStatic
        val INSTANCE: RaServerApi by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            RaServerApi()
        }
    }

    /**
     * Send a broadcast message to all clients
     * @param message a broadcast message
     */
    fun sendBroadcastToAllClients(serviceKey: String?, message: Message) {
        RaClientManager.sendMsgToClient(serviceKey = serviceKey, message = message.apply { // Change the message type to MESSAGE_CLIENT_BROADCAST_RSP before send
            what = MESSAGE_CLIENT_BROADCAST_RSP
        })
    }

    fun getAllRaClientServiceKeys() = RaClientManager.clients.map { it.serviceKey }

    // TODO : 获取对应客户端的Binder，然后通过remoteMethodCallAsync、remoteMethodCallSync等方法调用对应客户的的方法
}