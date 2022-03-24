package com.softtanck.ramessageservice

import android.app.Service
import android.content.Intent
import android.os.HandlerThread
import android.os.IBinder
import android.os.IInterface
import android.os.Message
import android.util.Log
import com.softtanck.RaNotification
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessageservice.engine.RaServerHandler
import com.softtanck.ramessageservice.intercept.RaDefaultIntercept
import com.softtanck.ramessageservice.intercept.RaResponseIntercept
import com.softtanck.ramessageservice.intercept.RealInterceptorChain

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
abstract class BaseConnectionService : Service() {
    private val TAG: String = BaseConnectionService::class.java.simpleName
    private val workHandlerThread = HandlerThread(TAG)

    // TODO : Add a interceptor chain for developers to add their own interceptors
    private val intercepts = mutableListOf<RaResponseIntercept>().apply {
        add(RaDefaultIntercept())
    }

    init {
        // Start the work's thread at first, And it is a single object!
        if (!workHandlerThread.isAlive) workHandlerThread.start()
    }

    fun onRemoteMessageArrived(message: Message, isSyncCall: Boolean): Message? {
        Log.d(TAG, "[SERVER] onRemoteMessageArrived: $message, what:${message.what} isSyncCall:$isSyncCall, trxID:${message.arg1}")
        val response = RealInterceptorChain(intercepts, 0, this@BaseConnectionService).proceed(message, isSyncCall)
        return if (isSyncCall) {
            response
        } else {
            sendMsgToClient(message)
            null
        }
    }

    @Suppress("LeakingThis")
    private val customProcessHandler: RaServerHandler = RaServerHandler(workHandlerThread.looper, this)
    private val raCustomMessenger: RaCustomMessenger = RaCustomMessenger((customProcessHandler.getIMessenger(false) as IInterface).asBinder())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "[SERVER] onCreate")
        startForeground(RaNotification.BASE_CONNECTION_SERVICE_NOTIFICATION_ID, RaNotification.getNotificationForInitSetup(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "[SERVER] onStartCommand")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        val clientVersionCode = intent.getIntExtra(RaCustomMessenger.raMsgVersion.first, -1)
        Log.d(TAG, "[SERVER] onBind coming, the client version code is:$clientVersionCode")
        // Return our custom Binder if that is supported in Clients. (IPC)
        return if (raCustomMessenger.binder != null && clientVersionCode != -1 && RaCustomMessenger.raMsgVersion.second >= clientVersionCode) {
            raCustomMessenger.binder
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
        customProcessHandler.sendMsgToClient(message)
    }
}