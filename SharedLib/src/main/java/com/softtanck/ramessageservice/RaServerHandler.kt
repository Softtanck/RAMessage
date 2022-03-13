package com.softtanck.ramessageservice

import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import com.softtanck.MESSAGE_CLIENT_REQ
import com.softtanck.MESSAGE_CLIENT_RSP
import com.softtanck.MESSAGE_REGISTER_CLIENT_REQ
import com.softtanck.MESSAGE_REGISTER_CLIENT_RSP
import com.softtanck.model.RaClient
import com.softtanck.sharedlib.BuildConfig

internal class RaServerHandler internal constructor(looper: Looper, private val baseConnectionService: BaseConnectionService) : BaseServerSyncHandler(looper, baseConnectionService) {
    private val TAG = this.javaClass.name
    private val clients: LinkedHashSet<RaClient> = LinkedHashSet()

    override fun handleMessage(msg: Message) {
        Log.d(TAG, "[SERVER] RaServerHandler handleMessage: ${msg.what}")
        when (msg.what) {
            MESSAGE_REGISTER_CLIENT_REQ -> {
                synchronized(clients) {
                    // TODO : PID will be implemented in next release
                    clients.add(RaClient(0, msg.replyTo))
                    sendConnectionRegisterStateToClient(msg.replyTo)
                    Log.d(TAG, "[SERVER] Client is registered. No of active clients : ${clients.size}")
                }
            }
            MESSAGE_CLIENT_REQ -> {
                baseConnectionService.onRemoteMessageArrived(msg, false)
            }
            else -> {
                Log.d(TAG, "[SERVER] Not in protocol, Discarding the message. msg:$msg")
            }
        }
    }

    private fun sendConnectionRegisterStateToClient(client: Messenger) {
        try {
            val registerStateMessage: Message = Message.obtain(null, MESSAGE_REGISTER_CLIENT_RSP)
            client.send(registerStateMessage)
        } catch (e: Exception) {
            Log.e(TAG, "[SERVER] Exception occurs when send register state to client state: " + e.message, e)
        }
    }

    fun sendMsgToClient(message: Message) {
        synchronized(clients) {
            val iterator = clients.iterator()
            while (iterator.hasNext()) {
                val client = iterator.next()
                val clientBinder = client.clientMessenger.binder
                if (clientBinder != null && clientBinder.isBinderAlive) {
                    try {
                        client.clientMessenger.send(Message.obtain(message).apply { what = MESSAGE_CLIENT_RSP })
                    } catch (e: RemoteException) {
                        iterator.remove()
                        // The client is dead. Remove it from the list;
                        Log.d(TAG, "[SERVER] Removing inactive client. New client count is " + clients.size)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        Log.e(TAG, "[SERVER] Send failed for client : $client")
                        iterator.remove()
                    }
                } else {
                    Log.d(TAG, "[SERVER] Removing inactive client(Binder Died). New client count is " + clients.size)
                    iterator.remove()
                }
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "[SERVER] Sent response to " + clients.size + " Clients")
        }
    }
}