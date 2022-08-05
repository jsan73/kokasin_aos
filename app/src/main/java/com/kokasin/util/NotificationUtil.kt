package com.kokasin.util
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.graphics.BitmapFactory
//import android.os.Build
//import android.os.Bundle
//import android.text.TextUtils
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.core.content.ContextCompat
//import com.kokasin.R
//import me.leolin.shortcutbadger.ShortcutBadger
//import java.net.URL
//import java.net.URLDecoder
//
//
//
//
//object NotificationUtil {
//    private const val DEFAULT_CHANNEL_ID = "KS_DEFAULT_CHANNEL_ID"
//
//    // 노티피케이션 채널 생성 (앱 시작 후 처음 한번 실행)
//    fun createNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  // Android Oreo 이상 기능
//            try {
//                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                var notificationChannel = notificationManager.getNotificationChannel(DEFAULT_CHANNEL_ID)
//                if (notificationChannel == null) {
//                    notificationChannel = NotificationChannel(DEFAULT_CHANNEL_ID, "알림", NotificationManager.IMPORTANCE_DEFAULT)
//                    notificationChannel.enableLights(true)
////                    notificationChannel.lightColor = ContextCompat.getColor(context, R.color.colorAccent)
//                    notificationChannel.enableVibration(true)
//                    notificationChannel.vibrationPattern = longArrayOf(100, 200, 100, 200)
//                    notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC // 잠금홤면에 title, message 노출 여부
//                    notificationManager.createNotificationChannel(notificationChannel)
//                }
//            } catch (e: Exception) {
//                LogUtil.e(e.toString())
//                e.printStackTrace()
//            }
//        }
//    }
//
//    // 노티피케이션 표시 (이미지 있는 경우 표시)
//    fun showNotification(context: Context, id: Int, bundle: Bundle, intent: PendingIntent?) {
//        try {
//            // 키 값은 서버와 같아야 함
//            val title = URLDecoder.decode(bundle.getString("title"), "utf-8")
//            val message = URLDecoder.decode(bundle.getString("body"), "utf-8")
//            val imgUrl = bundle.getString("imgUrl")
//            val badge = bundle.getString("badge")?.toInt()
//            val builder = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
////                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.notification_large_icon))
////                    .setSmallIcon(R.drawable.icon_notification_96_w)
////                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
//                    .setContentTitle(title)
//                    .setContentText(message)
//                    .setDefaults(Notification.FLAG_SHOW_LIGHTS or Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
//                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
//                    .setAutoCancel(true)
//
//            // 이미지 경로 있는 경우 추가
//            if (!TextUtils.isEmpty(imgUrl)) {
//                val url = URL(imgUrl)
//                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
//                builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(bitmap).setSummaryText(message))
//            }
//            if (intent != null) {
//                builder.setContentIntent(intent)
//            }
//            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.notify(id, builder.build())
//
//            if(badge != null) {
//                ShortcutBadger.applyCount(context, badge)
//            }
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    // 모든 노티피케이션 삭제
//    fun clearNotification(context: Context) {
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.cancelAll()
//        ShortcutBadger.applyCount(context, 0)
//    }
//
//    // 애플리케이션 설정에서 알림 설정 여부값 리턴
//    open fun areNotificationsEnabled(context: Context): Boolean {
//        return NotificationManagerCompat.from(context).areNotificationsEnabled()
//    }
//}
