package com.softtanck.ramessageclient.core.listener

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
// TODO : How about weakReference?
internal class BindStateListenerManager {
    companion object {
        @JvmStatic
        val INSTANCE: BindStateListenerManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            BindStateListenerManager()
        }
    }

    private val clientsBindStateList = LinkedHashSet<BindStateListener>()

    fun add(bindStateListener: BindStateListener) {
        synchronized(clientsBindStateList) {
            clientsBindStateList.add(bindStateListener)
        }
    }

    fun remove(bindStateListener: BindStateListener) {
        synchronized(clientsBindStateList) {
            clientsBindStateList.remove(bindStateListener)
        }
    }

    fun clearAll() {
        synchronized(clientsBindStateList) {
            clientsBindStateList.clear()
        }
    }

    // TIPS: use the list of copy
    fun getAllListener(): List<BindStateListener> = clientsBindStateList.toList()
}