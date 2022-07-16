package com.softtanck.ramessageservice

import android.app.Service
import android.content.Intent
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.softtanck.RaNotification
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessageservice.engine.RaClientManager
import com.softtanck.ramessageservice.engine.RaServerHandler
import com.softtanck.ramessageservice.intercept.IRaResponseIntercept
import com.softtanck.ramessageservice.intercept.RaDefaultInterceptImpl
import com.softtanck.ramessageservice.intercept.RealInterceptorChain

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 * @param startInForeground is start in foreground, that is, show a notification
 */
abstract class BaseConnectionService(private val startInForeground: Boolean = true) : Service() {
    private val TAG: String = BaseConnectionService::class.java.simpleName
    private val workHandlerThread = HandlerThread(TAG)

    // Add an interceptor chain for developers to add their own interceptors
    private val intercepts = mutableListOf<IRaResponseIntercept>().apply {
        add(RaDefaultInterceptImpl())
    }

    init {
        // Start the work's thread at first, And it is a single object!
        if (!workHandlerThread.isAlive) workHandlerThread.start()
    }

    fun onRemoteMessageArrived(message: Message, isSyncCall: Boolean): Message? {
        Log.d(TAG, "[SERVER] onRemoteMessageArrived: $message, what:${message.what} isSyncCall:$isSyncCall, trxID:${message.arg1}, thread:${Thread.currentThread()}")
        val response = RealInterceptorChain(intercepts, 0, this@BaseConnectionService).proceed(message, isSyncCall)
        return if (isSyncCall) { // If it is a sync call, return the response
            response
        } else {
            // The message will be changed in the interceptor chain, so we need to return the message directly
            sendMsgToClient(message)
            null
        }
    }

    @Suppress("LeakingThis")
    private val customProcessHandler: RaServerHandler = RaServerHandler(workHandlerThread.looper, this)
    private val raCustomMessenger = customProcessHandler.innerMessenger

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "[SERVER] onCreate")
        if (startInForeground) { // if start in foreground, show a notification
            startForeground(RaNotification.BASE_CONNECTION_SERVICE_NOTIFICATION_ID, RaNotification.getNotificationForInitSetup(this))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "[SERVER] onStartCommand")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        val clientVersionCode = intent.getIntExtra(RaCustomMessenger.raMsgVersion.first, -1)
        Log.d(TAG, "[SERVER] onBind coming, the client version code is:$clientVersionCode")
        // Return our custom Binder if that is supported in Clients. (IPC)
        return if (raCustomMessenger.asBinder() != null && clientVersionCode != -1 && RaCustomMessenger.raMsgVersion.second >= clientVersionCode) {
            raCustomMessenger.asBinder()
        } else {
            throw IllegalStateException("[SERVER] Failed to get the Messenger, Please check your handler")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (workHandlerThread.isAlive) workHandlerThread.quitSafely()
        Log.d(TAG, "[SERVER] onDestroy")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "[SERVER] onTaskRemoved")
    }

    private fun sendMsgToClient(message: Message) {
        RaClientManager.sendMsgToClient(message)
    }
}