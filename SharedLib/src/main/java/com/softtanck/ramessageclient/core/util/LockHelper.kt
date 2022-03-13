package com.softtanck.ramessageclient.core.util

/**
 * Created by Softtanck on 2022/3/12
 * A lock helper for the client.
 * These locks both are working on the client.
 */
internal object LockHelper {
    /**
     * The lock is added when the result of the binding is retrieved
     */
    val BIND_RESULT_OBJ_LOCK = Object()

    /**
     * Add a lock to the binding process
     */
    val BIND_IN_PROGRESS_OBJ_LOCK = Object()
}