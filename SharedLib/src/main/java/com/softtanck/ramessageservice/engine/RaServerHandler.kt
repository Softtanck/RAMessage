package com.softtanck.ramessageservice.engine

import android.os.Looper
import android.os.Message
import android.os.Parcelable
import android.os.RemoteException
import android.util.Log
import com.softtanck.*
import com.softtanck.model.RaClient
import com.softtanck.model.RaCustomMessenger
import com.softtanck.ramessageservice.BaseConnectionService
import com.softtanck.sharedlib.BuildConfig

internal class RaServerHandler internal constructor(looper: Looper, private val baseConnectionService: BaseConnectionService) : BaseServerSyncHandler(looper, baseConnectionService) {
    private val TAG = this.javaClass.name
    private val clients: LinkedHashSet<RaClient<Parcelable>> = LinkedHashSet()

    override fun handleMessage(msg: Message) {
        Log.d(TAG, "[SERVER] RaServerHandler handleMessage: ${msg.what}")
        when (msg.what) {
            MESSAGE_REGISTER_CLIENT_REQ -> {
                synchronized(clients) {
                    Log.d(TAG, "[SERVER] RaServerHandler handleMessage: MESSAGE_REGISTER_CLIENT_REQ, msg.sendingUid:${msg.sendingUid}")
                    val dataFromClient = msg.data.apply { classLoader = this@RaServerHandler.javaClass.classLoader }
                    val tempRaClient = RaClient(msg.sendingUid, dataFromClient.getParcelable<Parcelable>(MESSAGE_BUNDLE_REPLY_TO_KEY) as? RaCustomMessenger ?: msg.replyTo)
                    clients.add(tempRaClient)
                    sendConnectionRegisterStateToClient(tempRaClient)
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

    private fun sendConnectionRegisterStateToClient(raClient: RaClient<Parcelable>) {
        try {
            raClient.sendAsyncMessageToClient(Message.obtain(null, MESSAGE_REGISTER_CLIENT_RSP))
        } catch (e: Exception) {
            Log.e(TAG, "[SERVER] Exception occurs when send register state to client state: " + e.message, e)
        }
    }

    fun sendMsgToClient(message: Message) {
        synchronized(clients) {
            val iterator = clients.iterator()
            while (iterator.hasNext()) {
                val client = iterator.next()
                Log.d(TAG, "[SERVER] sendMsgToClient: client.uid:${client.clientUID}")
                // TODO : Need to check the pid if the client is running in the same process.
                //  and send the message to the client only if the client is running in the same process.
                //  Also, the request type of message needs to checked (Single point and broadcast) - TangCe
                if (message.sendingUid == client.clientUID) {
                    val clientBinder = client.getClientBinder()
                    if (clientBinder != null && clientBinder.isBinderAlive) {
                        try {
                            client.sendAsyncMessageToClient(Message.obtain(message).apply { what = MESSAGE_CLIENT_RSP })
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
                    Log.d(TAG, "[SERVER] Current UID(${message.sendingUid}) not is ${client.clientUID}, Skip sending message to client")
                }
            }
        }
    }
}