package com.kokasin

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.downloader.PRDownloader
import com.kokasin.util.NotificationUtil

class ApplicationManager: Application() {
    override fun onCreate() {
        super.onCreate()

        NotificationUtil.createNotificationChannel(this)
        PRDownloader.initialize(this)


        // createChannel (Oreo 대응)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT // LOW : 소리/진동 없음, HIGH : 소리/진동 있고, 상단 알림 표시됨
            val mChannel = NotificationChannel(Constants.Notification.CHANNEL_ID, "KOKASIN 알림", importance)
            mChannel.setShowBadge(false)
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }
}