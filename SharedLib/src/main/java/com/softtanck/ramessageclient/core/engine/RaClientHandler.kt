package com.softtanck.ramessageclient.core.engine

import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.util.Log
import com.softtanck.MESSAGE_BUNDLE_REPLY_TO_KEY
import com.softtanck.MESSAGE_CLIENT_BROADCAST_RSP
import com.softtanck.MESSAGE_CLIENT_DISCONNECT_REQ
import com.softtanck.MESSAGE_CLIENT_SINGLE_REQ
import com.softtanck.MESSAGE_CLIENT_SINGLE_RSP
import com.softtanck.MESSAGE_REGISTER_CLIENT_REQ
import com.softtanck.MESSAGE_REGISTER_CLIENT_RSP
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessageclient.core.listener.ClientListenerManager
import com.softtanck.ramessageclient.core.listener.DisconnectedReason
import com.softtanck.ramessageclient.core.listener.RA_DISCONNECTED_ABNORMAL
import com.softtanck.ramessageclient.core.listener.RA_DISCONNECTED_MANUAL
import com.softtanck.ramessageclient.core.listener.RaRemoteMessageListener
import com.softtanck.ramessageclient.core.model.RaClientBindStatus
import com.softtanck.ramessageclient.core.util.LockHelper

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
internal class RaClientHandler(looper: Looper, private val raClientBindStatus: RaClientBindStatus) : BaseClientHandler(looper, raClientBindStatus) {

    companion object {
        private val TAG: String = RaClientHandler::class.java.simpleName
    }

    override fun handleMessage(msg: Message) {
        Log.d(TAG, "[CLIENT] RaClientHandler handleMessage: ${msg.what}")
        when (msg.what) {
            MESSAGE_REGISTER_CLIENT_RSP -> {
                onBindStatusChanged(true)
            }

            MESSAGE_CLIENT_SINGLE_RSP -> {
                Log.d(TAG, "[CLIENT] Received a new msg from server: $msg, trxID: ${msg.arg1}")
                singleCallbacks.get(msg.arg1)?.run {
                    // 1. first is the callback needs to be called
                    onMessageArrived(message = msg)
                    // 2. finally, remove the trxID from the map
                    synchronized(singleCallbacks) {
                        singleCallbacks.remove(msg.arg1)
                    }
                }
            }

            MESSAGE_CLIENT_BROADCAST_RSP -> {
                Log.d(TAG, "[CLIENT] Received a new msg(broadcast) from server: $msg")
                ClientListenerManager.INSTANCE.getAllRemoteBroadCastMessageCallbacks(raClientBindStatus.componentName).forEach { callback ->
                    callback.onMessageArrived(message = msg)
                }
            }
        }
    }

    fun onBindStatusChanged(isConnected: Boolean, @DisconnectedReason disconnectedReason: Int = RA_DISCONNECTED_ABNORMAL) {
        Log.d(TAG, "[CLIENT] onBindStatusChanged: $isConnected, $disconnectedReason")
        if (isConnected) {
            if (!raClientBindStatus.bindStatus.value) {
                synchronized(LockHelper.BIND_RESULT_OBJ_LOCK) {
                    if (!raClientBindStatus.bindStatus.value) { // Double check here
                        raClientBindStatus.bindStatus.value = true
                        val iterator = ClientListenerManager.INSTANCE.getAllBindStatusChangedListener(raClientBindStatus.componentName).iterator()
                        while (iterator.hasNext()) {
                            val bindStateListener = iterator.next()
                            bindStateListener.onConnectedToRaServices(raClientBindStatus.componentName)
                        }
                    } else {
                        Log.w(TAG, "[CLIENT] Already scheduled, Ignore this request, isConnected: $isConnected, Thread:${Thread.currentThread()}")
                    }
                }
            } else {
                // Just log here
                Log.w(TAG, "[CLIENT] It looks like you should not going here. Thread:${Thread.currentThread()}")
            }
        } else {
            if (raClientBindStatus.bindStatus.value) {
                synchronized(LockHelper.BIND_RESULT_OBJ_LOCK) {
                    if (raClientBindStatus.bindStatus.value) { // Double check here
                        raClientBindStatus.bindStatus.value = false
                        val iterator = ClientListenerManager.INSTANCE.getAllBindStatusChangedListener(raClientBindStatus.componentName).iterator()
                        while (iterator.hasNext()) {
                            val bindStatusListener = iterator.next()
                            bindStatusListener.onDisconnectedFromRaServices(componentName = raClientBindStatus.componentName, disconnectedReason = disconnectedReason)
                        }
                    } else {
                        Log.w(TAG, "[CLIENT] Already scheduled, Ignore this request, isConnected: true, Thread:${Thread.currentThread()}")
                    }
                    // finally, clear the singleCallbacks and broadcastCallbacks
                    clearAllCallbacks()
                }
            } else {
                raClientBindStatus.bindStatus.value = false
            }
        }
    }

    /**
     * Send a message to service with compat.
     * Since the client maybe not have the permission to access the hidden API, we need to use the compat.
     * if the client has the permission, we can use the normal way to send message.
     * Note: Currently, This method only for disconnection.
     * @param message the message to send
     */
    private fun sendMsgToServiceCompat(message: Message) {
        if (outputMessengerStateFlow.value != null && outputMessengerStateFlow.value is RaCustomMessenger) {
            sendSyncMessageToServer(message)
        } else {
            sendMsgToServerAsync(message)
        }
    }

    fun trySendDisconnectedToService() {
        sendMsgToServiceCompat(Message.obtain().apply { what = MESSAGE_CLIENT_DISCONNECT_REQ })
        // I think you are disconnected with service, So hardcode here.
        onBindStatusChanged(false, RA_DISCONNECTED_MANUAL)
    }

    fun sendMsgToServerAsync(message: Message, raRemoteMessageListener: RaRemoteMessageListener? = null) {
        sendAsyncMessageToServer(message.apply { what = MESSAGE_CLIENT_SINGLE_REQ }, raRemoteMessageListener)
    }

    fun sendMsgToServerSync(message: Message): Message? = sendSyncMessageToServer(message.apply { what = MESSAGE_CLIENT_SINGLE_REQ })

    fun sendRegisterMsgToServer(inputMessenger: RaCustomMessenger) = notSafetySendAsyncMessageToServer(message = Message.obtain(null, MESSAGE_REGISTER_CLIENT_REQ).apply {
        // replyTo inputMessenger as Messenger if the client is failed to reflect the messenger.
        data = Bundle().apply {
            putParcelable(MESSAGE_BUNDLE_REPLY_TO_KEY, inputMessenger)
        }
    })

    override fun onRemoteMessageArrived(msg: Message, isSync: Boolean): Message? {
        //TODO("Not yet implemented")
        // TODO : 跟服务端一样的实现。不过要反着来：
        //  即： 服务端动态代理接口，然后获取参数类型、参数、函数名字后，通过IPC调用，把相关参数传递给客户端，客户端通过「反射」执行对应返回并同步返回；
        //  那么异步情况如何处理？
        return null
    }

}