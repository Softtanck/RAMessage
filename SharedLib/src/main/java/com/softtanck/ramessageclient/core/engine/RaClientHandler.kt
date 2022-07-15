package com.softtanck.ramessageclient.core.engine

import android.os.*
import android.util.Log
import com.softtanck.*
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessage.IRaMessenger
import com.softtanck.ramessageclient.core.listener.*
import com.softtanck.ramessageclient.core.util.LockHelper
import com.softtanck.ramessageclient.core.util.ReflectionUtils

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
internal class RaClientHandler : BaseClientHandler {
    constructor() : super()
    constructor(looper: Looper) : super(looper)
    constructor(looper: Looper, callback: Callback) : super(looper, callback)

    companion object {
        private val TAG: String = RaClientHandler::class.java.simpleName
        private val workThreadHandler = HandlerThread(TAG)
        private val inBoundMessenger: Parcelable

        @JvmStatic
        val INSTANCE: RaClientHandler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            // 1. Let the background thread started
            if (!workThreadHandler.isAlive) workThreadHandler.start()
            // 2. Use the looper from above
            RaClientHandler(workThreadHandler.looper)
        }.apply {
            // 3. Remember create an inBoundMessenger
            inBoundMessenger = RaCustomMessenger(this.value)
        }
    }

    /**
     * Set an outbound's messenger from outside
     * @param outBoundMessenger the outBoundMessenger from server
     */
    @Synchronized
    fun setOutBoundMessenger(outBoundMessenger: IRaMessenger?) {
        outputMessenger = outBoundMessenger
    }

    fun getInBoundMessenger() = inBoundMessenger

    fun clientIsBoundStatus() = clientBoundStatus.get()

    private fun makeClientBoundStatusUseCAS(isBound: Boolean) {
        // Make sure the bind is changed success!!!
        @Suppress("ControlFlowWithEmptyBody")
        while (!clientBoundStatus.compareAndSet(clientBoundStatus.get(), isBound));
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
                    get()?.onMessageArrived(msg)
                    // 2. then remove the callback from the map
                    clear()
                }
                // 3. finally, remove the trxID from the map
                synchronized(singleCallbacks) {
                    singleCallbacks.remove(msg.arg1)
                }
            }
            MESSAGE_CLIENT_BROADCAST_RSP -> {
                Log.d(TAG, "[CLIENT] Received a new msg(broadcast) from server: $msg")
                broadcastCallbacks.forEachIndexed { _, callback ->
                    // 1. first is the callback needs to be called
                    callback.get()?.onMessageArrived(msg)
                }
            }
        }
    }

    fun onBindStatusChanged(isConnected: Boolean, @DisconnectedReason disconnectedReason: Int = RA_DISCONNECTED_ABNORMAL) {
        Log.d(TAG, "[CLIENT] onBindStatusChanged: $isConnected, $disconnectedReason")
        if (isConnected) {
            if (!clientBoundStatus.get()) {
                synchronized(LockHelper.BIND_RESULT_OBJ_LOCK) {
                    if (!clientBoundStatus.get()) { // Double check here
                        makeClientBoundStatusUseCAS(true)
                        val iterator = BindStateListenerManager.INSTANCE.getAllListener().iterator()
                        while (iterator.hasNext()) {
                            val bindStateListener = iterator.next()
                            bindStateListener.onConnectedToRaServices()
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
            if (clientBoundStatus.get()) {
                synchronized(LockHelper.BIND_RESULT_OBJ_LOCK) {
                    if (clientBoundStatus.get()) { // Double check here
                        makeClientBoundStatusUseCAS(false)
                        val iterator = BindStateListenerManager.INSTANCE.getAllListener().iterator()
                        while (iterator.hasNext()) {
                            val bindStateListener = iterator.next()
                            bindStateListener.onDisconnectedFromRaServices(disconnectedReason)
                        }
                    } else {
                        Log.w(TAG, "[CLIENT] Already scheduled, Ignore this request, isConnected: $isConnected, Thread:${Thread.currentThread()}")
                    }
                    // finally, clear the singleCallbacks and broadcastCallbacks
                    synchronized(singleCallbacks) {
                        singleCallbacks.clear()
                    }
                    synchronized(broadcastCallbacks) {
                        broadcastCallbacks.clear()
                    }
                }
            } else {
                makeClientBoundStatusUseCAS(false)
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
        if (outputMessenger != null && outputMessenger is RaCustomMessenger) {
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

    override fun onRemoteMessageArrived(msg: Message, isSync: Boolean): Message? {
        TODO("Not yet implemented")
        // TODO : 跟服务端一样的实现。不过要反着来：
        //  即： 服务端动态代理接口，然后获取参数类型、参数、函数名字后，通过IPC调用，把相关参数传递给客户端，客户端通过「反射」执行对应返回并同步返回；
        //  那么异步情况如何处理？
    }
}