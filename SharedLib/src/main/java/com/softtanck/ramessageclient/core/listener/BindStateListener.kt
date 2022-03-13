package com.softtanck.ramessageclient.core.listener

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO: TBD
 */
interface BindStateListener {

    /**
     * This method will be invoked if the client is failed to bound the remote server;
     * In general, it may be caused by permission issues
     */
    fun onConnectRaServicesFailed()

    /**
     * This method will be invoked When the client connects to the server successfully.
     * TODO: What the thread? Currently, Always on background.
     */
    fun connectedToRaServices()

    /**
     * This method will be invoked if the client is disconnected from the server.
     * @param disconnectedReason  [RA_DISCONNECTED_ABNORMAL] It could be a service exception or a lower level error. [RA_DISCONNECTED_MANUAL]
     * Developer actively disconnects
     */
    fun disconnectedFromRaServices(@DisconnectedReason disconnectedReason: Int)
}
