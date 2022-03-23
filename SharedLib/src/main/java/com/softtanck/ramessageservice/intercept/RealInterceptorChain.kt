package com.softtanck.ramessageservice.intercept

import android.os.Message
import com.softtanck.ramessageservice.model.RaChain

/**
 * @author Softtanck
 * @date 2022/3/23
 * Description: TODO
 */
class RealInterceptorChain(private val interceptors: List<RaResponseIntercept>, private val targetIndex: Int) : RaChain() {
    override fun proceed(message: Message, isSyncCall: Boolean): Message? {
        interceptors.size.let {
            if (targetIndex >= it) {
                return message
            } else if (targetIndex < 0) {
                return message
            }
            val interceptor = interceptors[targetIndex]
            val tempIndex = targetIndex - 1
            val realInterceptorChain = RealInterceptorChain(interceptors, tempIndex)
            return interceptor.intercept(realInterceptorChain, message, isSyncCall)
        }
    }
}