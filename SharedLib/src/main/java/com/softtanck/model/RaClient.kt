package com.softtanck.model

import android.os.Messenger

/**
 * Created by Softtanck on 2022/3/12
 */
internal data class RaClient(val clientUID: Int, val clientMessenger: Messenger) {
    override fun toString(): String {
        return "RaClient(clientPID=$clientUID, clientMessenger=$clientMessenger)"
    }
}
