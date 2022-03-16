package com.softtanck.ramessageclient.core.listener

import android.os.Message

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
fun interface RaRemoteMessageListener {
    /**
     * This method will be invoked if the message is arrived from outside.
     * @param message the message, null will be returned if request is failed.
     */
    fun onMessageArrived(message: Message?)
}