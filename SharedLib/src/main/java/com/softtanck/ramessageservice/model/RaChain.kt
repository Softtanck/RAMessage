package com.softtanck.ramessageservice.model

import android.os.Message

/**
 * @author Softtanck
 * @date 2022/3/23
 * Description: TODO
 */
abstract class RaChain {

    abstract fun proceed(message: Message, isSyncCall: Boolean): Message?
}