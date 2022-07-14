package com.softtanck.ramessageservice.engine

import android.os.Message
import android.os.Parcelable
import android.os.RemoteException
import android.util.Log
import com.softtanck.MESSAGE_BUNDLE_REPLY_TO_KEY
import com.softtanck.MESSAGE_CLIENT_BROADCAST_RSP
import com.softtanck.MESSAGE_CLIENT_SINGLE_RSP
import com.softtanck.MESSAGE_REGISTER_CLIENT_RSP
import com.softtanck.model.RaClient
import com.softtanck.model.RaCustomMessenger
import com.softtanck.sharedlib.BuildConfig

/**
 * @author Softtanck
 * @date 2022/6/29
 * Description: An object to hold the binder of the client.
 */
internal object RaClientManager {
    private val TAG = RaClientManager::class.java.simpleName

    /**
     * All clients put into this map.
     */
    private val clients: LinkedHashSet<RaClient<Parcelable>> = LinkedHashSet()

    /**
     * Add current client into the clients map, then send the connection register state to the client.
     * @param msg The message from the clients.
     */
    fun registerClientFromBinderWithMessage(msg: Message) {
        synchronized(clients) {
            Log.d(TAG, "[SERVER] RaServerHandler handleMessage: MESSAGE_REGISTER_CLIENT_REQ, msg.sendingUid:${msg.sendingUid}")
            val dataFromClient = msg.data.apply { classLoader = this@RaClientManager.javaClass.classLoader }
            val tempRaClient = RaClient(msg.sendingUid, dataFromClient.getParcelable<Parcelable>(MESSAGE_BUNDLE_REPLY_TO_KEY) as? RaCustomMessenger ?: msg.replyTo)
            clients.add(tempRaClient)
            try {
                tempRaClient.sendAsyncMessageToClient(Message.obtain(null, MESSAGE_REGISTER_CLIENT_RSP))
            } catch (e: Exception) {
                Log.e(TAG, "[SERVER] Exception occurs when send register state to client state: " + e.message, e)
            }
            Log.d(TAG, "[SERVER] Client is registered. No of active clients : ${clients.size}")
        }
    }

    /**
     * Send a message to client.
     * @param message The message to send.
     */
    fun sendMsgToClient(message: Message) {
        synchronized(clients) {
            val iterator = clients.iterator()
            while (iterator.hasNext()) {
                val client = iterator.next()
                Log.d(TAG, "[SERVER] sendMsgToClient: client.uid:${client.clientUID}")
                //  Need to check the pid if the client is running in the same process.
                //  and send the message to the client only if the client is running in the same process.
                //  Also, the request type of message needs to checked (Single point and broadcast)
                if (message.what == MESSAGE_CLIENT_BROADCAST_RSP || message.sendingUid == client.clientUID) {
                    val clientBinder = client.getClientBinder()
                    if (clientBinder != null && clientBinder.isBinderAlive) {
                        try {
                            client.sendAsyncMessageToClient(Message.obtain(message).apply { what = MESSAGE_CLIENT_SINGLE_RSP })
                            if (BuildConfig.DEBUG) Log.d(TAG, "[SERVER] Sent response to msg($message) Clients")
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
                } else {
                    Log.d(TAG, "[SERVER] Current UID(${message.sendingUid}) not is ${client.clientUID} or type is not broadcast(${message.what}), Skip sending message to client")
                }
            }
        }
    }
}