package com.kokasin.fcm

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kokasin.activity.MainWebViewActivity
import com.kokasin.util.LogUtil
import com.kokasin.util.NotificationUtil
import com.kokasin.util.PreferenceUtil

class FirebaseInstanceIDService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val pref = PreferenceUtil(this)
        pref.put(PreferenceUtil.KEYS.PUSH_ID, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        LogUtil.e("test", "remoteMessage.getData() : " + remoteMessage.data)

        val bundle = Bundle()
        for ((key, value) in remoteMessage.data.entries) {
            bundle.putString(key, value)
        }

        val intent = Intent(this, MainWebViewActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtras(bundle)
        val uniqueInt = (System.currentTimeMillis() and 0xfffffff).toInt()
        val contentIntent: PendingIntent?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentIntent = PendingIntent.getActivity(
                this,
                uniqueInt,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
        else {
            contentIntent = PendingIntent.getActivity(
                this,
                uniqueInt,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val nid = (System.currentTimeMillis() / 1000).toInt() // 여러개 표시하기 위해
        NotificationUtil.showNotification(this, nid, bundle, contentIntent)
    }
}