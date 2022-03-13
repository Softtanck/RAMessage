/*
 * Copyright (C) 2022 Softtanck.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.softtanck.ramessageservice

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.MessageQueue
import com.softtanck.ramessage.IRaMessenger
import com.softtanck.ramessageclient.core.util.ReflectionUtils

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
internal open class BaseServerSyncHandler : Handler {
    private var baseConnectionService: BaseConnectionService? = null

    constructor() : super()
    constructor(looper: Looper) : super(looper)
    constructor(looper: Looper, baseConnectionService: BaseConnectionService) : super(looper) {
        this.baseConnectionService = baseConnectionService
    }

    constructor(looper: Looper, callback: Callback) : super(looper, callback)

    // IRaMessenger / IMessenger
    private var _innerMessenger: Any? = null

    // IRaMessenger / IMessenger
    fun getIMessenger(): Any? = getIMessenger(true)

    /**
     * Hook the getIMessenger, for add more interfaces
     *
     * @param fromSystem use sys's binder
     * @return IRaMessenger / IMessenger
     */
    fun getIMessenger(fromSystem: Boolean): Any? {
        return if (fromSystem) {
            ReflectionUtils.getIMessengerFromSystem(this)
        } else {
            // HOOK the messenger
            val messageQueue: MessageQueue = ReflectionUtils.getMessageQueueFromHandler(this) ?: return null
            synchronized(messageQueue) {
                if (_innerMessenger != null) {
                    return _innerMessenger
                }
                _innerMessenger = BaseSyncHandlerImp()
                return _innerMessenger
            }
        }
    }

    private inner class BaseSyncHandlerImp : IRaMessenger.Stub() {
        override fun send(msg: Message) {
            msg.sendingUid = getCallingUid()
            this@BaseServerSyncHandler.sendMessage(msg)
        }

        override fun sendSync(msg: Message): Message {
            msg.sendingUid = getCallingUid()
            return baseConnectionService?.onRemoteMessageArrived(msg, true) ?: Message.obtain(msg)
        }
    }
}