package com.softtanck.ramessageservice.model

import android.os.Message
import com.softtanck.ramessageservice.BaseConnectionService

/**
 * @author Softtanck
 * @date 2022/3/23
 * Description: TODO
 */
abstract class RaChain(val baseConnectionService: BaseConnectionService) {

    abstract fun proceed(message: Message, isSyncCall: Boolean): Message?
}