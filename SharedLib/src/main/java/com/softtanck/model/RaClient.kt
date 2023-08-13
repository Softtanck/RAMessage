package com.softtanck.model

import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.util.Log

/**
 * Created by Softtanck on 2022/3/12
 * a client of RaMessageService
 * @param clientUID client's uid
 * @param clientMessenger client's messenger
 * @param serviceKey service's key, used to identify the service
 */
internal data class RaClient<T : Parcelable>(val clientUID: Int, val clientMessenger: T, val serviceKey: String) {

    private val TAG = this.javaClass.name

    fun getClientBinder() =
        if (clientMessenger is RaCustomMessenger) {
            clientMessenger.binder
        } else {
            (clientMessenger as Messenger).binder
        }

    fun sendAsyncMessageToClient(message: Message) {
        when (clientMessenger) {
            is RaCustomMessenger -> {
                clientMessenger.send(message)
            }

            is Messenger -> {
                clientMessenger.send(message)
            }

            else -> {
                Log.e(TAG, "[SERVER] sendConnectionRegisterStateToClient: Unknown client messenger type")
                throw IllegalArgumentException("Unknown client messenger type")
            }
        }
    }

    fun sendSyncMessageToClient(message: Message): Message? =
        when (clientMessenger) {
            is RaCustomMessenger -> {
                clientMessenger.sendSync(message)
            }

            else -> {
                Log.e(TAG, "[SERVER] sendConnectionRegisterStateToClient: Unknown client messenger type, Dropped message:$message")
                null
            }
        }

    override fun toString(): String {
        return "RaClient(clientPID=$clientUID, clientMessenger=$clientMessenger)"
    }
}
