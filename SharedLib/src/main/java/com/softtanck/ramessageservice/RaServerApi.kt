package com.softtanck.ramessageservice

/**
 * @author Softtanck
 * @date 2022/3/28
 * Description: TODO
 */
class RaServerApi {
    companion object {
        @JvmStatic
        val INSTANCE: RaServerApi by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            RaServerApi()
        }
    }


    // TODO : 获取对应客户端的Binder，然后通过remoteMethodCallAsync、remoteMethodCallSync等方法调用对应客户的的方法
}