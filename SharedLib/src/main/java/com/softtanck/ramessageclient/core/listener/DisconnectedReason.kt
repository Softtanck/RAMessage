package com.softtanck.ramessageclient.core.listener

import androidx.annotation.IntDef

@MustBeDocumented
@IntDef(RA_DISCONNECTED_MANUAL, RA_DISCONNECTED_ABNORMAL)
@Target(AnnotationTarget.VALUE_PARAMETER)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class DisconnectedReason

/**
 * Developer actively disconnects
 */
const val RA_DISCONNECTED_MANUAL: Int = 1

/**
 * It could be a service exception or a lower level error.
 */
const val RA_DISCONNECTED_ABNORMAL: Int = 2
