package com.softtanck.ramessageclient.core.listener

import android.content.ComponentName

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO: TBD
 */
interface BindStatusChangedListener {

    /**
     * This method will be invoked if the client is failed to bound the remote server;
     * In general, it may be caused by permission issues
     * @param componentName the componentName of remote
     */
    fun onConnectRaServicesFailed(componentName: ComponentName)

    /**
     * This method will be invoked When the client connects to the server successfully.
     * What the thread? Currently, Always on background.
     * @param componentName the componentName of remote
     */
    fun onConnectedToRaServices(componentName: ComponentName)

    /**
     * This method will be invoked if the client is disconnected from the server.
     * @param disconnectedReason  [RA_DISCONNECTED_ABNORMAL] It could be a service exception or a lower level error. [RA_DISCONNECTED_MANUAL]
     * Developer actively disconnects
     * @param componentName the componentName of remote
     */
    fun onDisconnectedFromRaServices(componentName: ComponentName, @DisconnectedReason disconnectedReason: Int)
}
