/*
 * Copyright (C) 2023 Softtanck.
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
package com.softtanck.ramessageservice.engine

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.softtanck.ramessage.IRaMessenger
import com.softtanck.ramessageservice.BaseConnectionService

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
internal open class BaseServerSyncHandler(looper: Looper, private val baseConnectionService: BaseConnectionService) : Handler(looper) {

    // IRaMessenger / IMessenger
    val innerMessenger: IRaMessenger.Stub = BaseSyncHandlerImpl()

    private inner class BaseSyncHandlerImpl : IRaMessenger.Stub() {
        override fun send(msg: Message) {
            msg.sendingUid = getCallingUid()
            this@BaseServerSyncHandler.sendMessage(msg)
        }

        override fun sendSync(msg: Message): Message {
            msg.sendingUid = getCallingUid()
            return baseConnectionService.onRemoteMessageArrived(msg, true) ?: Message.obtain(msg)
        }
    }
}