package com.softtanck

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * @author Softtanck
 * @date 2022/3/12
 * Description: TODO
 */
internal object RaNotification {

    private const val NOTIFICATION_CHANNEL_ID = "com.softtanck.ramessageservice.foreground.service.id"
    private const val NOTIFICATION_CHANNEL_NAME = "com.softtanck.ramessageservice.foreground.service"

    const val BASE_CONNECTION_SERVICE_NOTIFICATION_ID = 1

    fun getNotificationForInitSetup(context: Context): Notification {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(notificationChannel)
            NOTIFICATION_CHANNEL_ID
        } else {
            NOTIFICATION_CHANNEL_ID
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, channelId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.priority = NotificationManager.IMPORTANCE_HIGH
        }
        return builder.setSmallIcon(R.drawable.ic_dialog_info)
            .setColor(-0xf05a35)
            .setContentTitle("RA Connection Service is working...")
            .build()
    }
}