package com.softtanck.ramessageclient.core.util

import android.annotation.SuppressLint
import android.os.Handler
import android.os.MessageQueue
import com.softtanck.ramessageservice.BaseServerSyncHandler
import java.lang.reflect.InvocationTargetException

/**
 * Created by Softtanck on 2022/3/12
 * Compatible with Android 12+ :)
 */
internal object ReflectionUtils {

    /**
     * get the IMessenger binder from system
     * @param targetHandler the BaseSyncHandler
     * @return the Binder object
     */
    @SuppressLint("DiscouragedPrivateApi")
    fun getIMessengerFromSystem(targetHandler: BaseServerSyncHandler): Any? {
        try {
            val getIMessengerMethod = Handler::class.java.getDeclaredMethod("getIMessenger")
            getIMessengerMethod.isAccessible = true
            return getIMessengerMethod.invoke(targetHandler)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * get the message queue from handler
     * @param targetHandler the targetHandler
     * @return MessageQueue
     */
    @SuppressLint("SoonBlockedPrivateApi")
    fun getMessageQueueFromHandler(targetHandler: BaseServerSyncHandler?): MessageQueue? {
        try {
            val mQueueField = Handler::class.java.getDeclaredField("mQueue")
            mQueueField.isAccessible = true
            return mQueueField[targetHandler] as MessageQueue
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }
}