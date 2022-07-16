package com.softtanck.ramessageservice.intercept

import android.os.Message
import com.softtanck.ramessageservice.model.RaChain

/**
 * @author Softtanck
 * @date 2022/3/23
 * Description: TODO
 */
interface IRaResponseIntercept {

    /**
     * Intercept the response before it is handled by the chain.
     */
    fun intercept(raChain: RaChain, message: Message, isSyncCall: Boolean): Message?
}