package com.softtanck.ramessageclient.core.engine

import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.softtanck.MESSAGE_CLIENT_DISCONNECT_REQ
import com.softtanck.MESSAGE_CLIENT_REQ
import com.softtanck.MESSAGE_CLIENT_RSP
import com.softtanck.MESSAGE_REGISTER_CLIENT_RSP
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessageclient.core.RaServiceConnector
import com.softtanck.ramessageclient.core.listener.*
import com.softtanck.ramessageclient.core.util.LockHelper
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
class RaClientHandler : BaseClientHandler {
    constructor() : super()
    constructor(looper: Looper) : super(looper)
    constructor(looper: Looper, callback: Callback) : super(looper, callback)

    /**
     * A flag to show the status of client.
     * true: Bound, otherwise not.
     */
    private val clientBoundStatus = AtomicBoolean(false)

    companion object {
        private val TAG: String = RaServiceConnector::class.java.simpleName
        private val workThreadHandler = HandlerThread(TAG)
        private val inBoundMessenger: Messenger

        @JvmStatic
        val INSTANCE: RaClientHandler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            // 1. Let the background thread started
            if (!workThreadHandler.isAlive) workThreadHandler.start()
            // 2. Use the looper from above
            RaClientHandler(workThreadHandler.looper)
        }.apply { inBoundMessenger = Messenger(this.value) } // 3. Remember create an inBoundMessenger
    }

    /**
     * Set an outbound's messenger from outside
     * @param outBoundMessenger the outBoundMessenger from server
     */
    @Synchronized
    fun setOutBoundMessenger(outBoundMessenger: RaCustomMessenger?) {
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
        if (msg.what == MESSAGE_REGISTER_CLIENT_RSP) {
            onBindStatusChanged(true)
        } else if (msg.what == MESSAGE_CLIENT_RSP) {
            Log.d(TAG, "[CLIENT] Received a new msg from server: $msg, trxID: ${msg.arg1}")
            callbacks.get(msg.arg1).get()?.onMessageArrived(msg)
        }
    }

    fun onBindStatusChanged(isConnected: Boolean, @DisconnectedReason disconnectedReason: Int = RA_DISCONNECTED_ABNORMAL) {
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
                }
            } else {
                makeClientBoundStatusUseCAS(false)
            }
        }
    }

    fun trySendDisconnectedToService() {
        sendMsgSync(Message.obtain().apply {
            what = MESSAGE_CLIENT_DISCONNECT_REQ

        })
        // I think you are disconnected with service, So hardcode here.
        onBindStatusChanged(false, RA_DISCONNECTED_MANUAL)
    }

    fun sendMsgToServerAsync(message: Message, raRemoteMessageListener: RaRemoteMessageListener? = null) {
        if (clientBoundStatus.get()) {
            sendMsgAsync(message.apply { what = MESSAGE_CLIENT_REQ }, raRemoteMessageListener)
        } else {
            Log.w(TAG, "[CLIENT] You are disconnected with server, Ignore this the message. msg:$message")
        }
    }

    fun sendMsgToServerSync(message: Message): Message? = if (clientBoundStatus.get()) {
        sendMsgSync(message.apply { what = MESSAGE_CLIENT_REQ })
    } else {
        Log.w(TAG, "[CLIENT] You are disconnected with server, Ignore this the message. msg:$message")
        null
    }
}