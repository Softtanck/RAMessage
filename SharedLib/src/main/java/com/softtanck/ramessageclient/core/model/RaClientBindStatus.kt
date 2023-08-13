package com.softtanck.ramessageclient.core.model

import android.content.ComponentName
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A model class for bind status.
 * @param componentName the componentName of remote
 * @param bindStatus the bind status
 * @param bindInProgress the bind in progress
 */
data class RaClientBindStatus(
    val componentName: ComponentName,
    var bindStatus: MutableStateFlow<Boolean>,
    var bindInProgress: MutableStateFlow<Boolean>
)
