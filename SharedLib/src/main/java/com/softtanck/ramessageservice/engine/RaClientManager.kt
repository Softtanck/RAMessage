package com.softtanck.ramessageservice.engine

import android.os.Build
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
     * All clients put into this set.
     */
    val clients: LinkedHashSet<RaClient<Parcelable>> = LinkedHashSet()

    /**
     * Add current client into the clients map, then send the connection register state to the client.
     * @param msg The message from the clients.
     */
    fun registerClientFromBinderWithMessage(serviceKey: String, msg: Message) {
        synchronized(clients) {
            Log.d(TAG, "[SERVER] RaServerHandler handleMessage: MESSAGE_REGISTER_CLIENT_REQ, msg.sendingUid:${msg.sendingUid}")
            val dataFromClient = msg.data.apply { classLoader = this@RaClientManager.javaClass.classLoader }
            val tempRaClient = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                RaClient(clientUID = msg.sendingUid, clientMessenger = dataFromClient.getParcelable(MESSAGE_BUNDLE_REPLY_TO_KEY, Parcelable::class.java) as? RaCustomMessenger ?: msg.replyTo, serviceKey = serviceKey)
            } else {
                @Suppress("DEPRECATION")
                RaClient(clientUID = msg.sendingUid, clientMessenger = dataFromClient.getParcelable<Parcelable>(MESSAGE_BUNDLE_REPLY_TO_KEY) as? RaCustomMessenger ?: msg.replyTo, serviceKey = serviceKey)
            }
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
     * Remove the client from the clients map.
     * @param serviceKey The service key of the client. like: com.softtanck.ramessageservice.RaConnectionService
     * @param msg The message from the clients.
     */
    fun removeClientFromBinderWithMessage(serviceKey: String, msg: Message) {
        synchronized(clients) {
            Log.d(TAG, "[SERVER] RaServerHandler handleMessage: MESSAGE_CLIENT_DISCONNECT_REQ, msg.sendingUid:${msg.sendingUid}")
            val iterator = clients.iterator()
            while (iterator.hasNext()) {
                val client = iterator.next()
                if (client.clientUID == msg.sendingUid && client.serviceKey == serviceKey) {
                    iterator.remove()
                    Log.d(TAG, "[SERVER] Client is removed. No of active clients : ${clients.size}")
                    break
                }
            }
        }
    }

    /**
     * Send a message to client.
     * @param serviceKey The service key of the client. like: com.softtanck.ramessageservice.RaConnectionService
     * @param message The message to send.
     */
    fun sendMsgToClient(serviceKey: String?, message: Message) {
        synchronized(clients) {
            val iterator = clients.iterator()
            while (iterator.hasNext()) {
                val client = iterator.next()
                Log.d(TAG, "[SERVER] sendMsgToClient(what:${message.what}): client.uid:${client.clientUID}, client.serviceKey:${client.serviceKey}, current serviceKey:$serviceKey")
                if (!serviceKey.isNullOrEmpty() && client.serviceKey != serviceKey) {
                    Log.d(TAG, "[SERVER] sendMsgToClient(what:${message.what}): dropped, since client.serviceKey:${client.serviceKey} != current serviceKey:$serviceKey")
                    continue
                }
                //  Need to check the pid if the client is running in the same process.
                //  and send the message to the client only if the client is running in the same process.
                //  Also, the request type of message needs to checked (Single point and broadcast)
                if (message.what == MESSAGE_CLIENT_BROADCAST_RSP || message.sendingUid == client.clientUID) {
                    val clientBinder = client.getClientBinder()
                    if (clientBinder != null && clientBinder.isBinderAlive) {
                        try {
                            client.sendAsyncMessageToClient(Message.obtain(message).apply {
                                if (message.what != MESSAGE_CLIENT_BROADCAST_RSP) {
                                    // Only not broadcast message need to change the message type to MESSAGE_CLIENT_SINGLE_RSP
                                    what = MESSAGE_CLIENT_SINGLE_RSP
                                }
                            })
                            if (BuildConfig.DEBUG) Log.d(TAG, "[SERVER] Sent msg(${message.what},$message) to Clients")
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