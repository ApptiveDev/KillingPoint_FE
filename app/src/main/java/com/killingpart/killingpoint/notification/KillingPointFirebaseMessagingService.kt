package com.killingpart.killingpoint.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.killingpart.killingpoint.MainActivity
import com.killingpart.killingpoint.R
import com.killingpart.killingpoint.data.local.AlarmReadStore
import com.killingpart.killingpoint.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections

class KillingPointFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (token.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            AuthRepository(applicationContext).addDeviceToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        AlarmReadStore.markLocalUnread(applicationContext)
        createNotificationChannel()

        // 같은 메시지가 두 번 배달되는 경우(FCM 재전송 or 토큰 중복) 무시
        val messageId = message.messageId
        if (messageId != null) {
            if (!processedMessageIds.add(messageId)) return
            if (processedMessageIds.size > 30) {
                processedMessageIds.iterator().let { it.next(); it.remove() }
            }
        }

        // notification payload가 있고 앱이 백그라운드이면 시스템이 이미 알림을 표시했으므로 건너뜀
        val isInForeground = ProcessLifecycleOwner.get()
            .lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (!isInForeground && message.notification != null) return

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "킬링파트"
        val body = message.notification?.body
            ?: message.data["content"]
            ?: message.data["body"]
            ?: ""

        val deepLink = message.data["deepLink"].orEmpty()
        val alarmType = message.data["type"].orEmpty()
        val notificationId = System.currentTimeMillis().toInt()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (deepLink.isNotBlank()) putExtra("deepLink", deepLink)
            if (alarmType.isNotBlank()) putExtra("type", alarmType)
            messageId?.let { putExtra("notificationId", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "killingpoint_notifications"
        private const val CHANNEL_NAME = "킬링파트 알림"
        private const val CHANNEL_DESCRIPTION = "좋아요, 댓글, 소셜 활동 알림"

        // 같은 메시지 ID가 두 번 배달되는 경우(FCM 재전송 or 토큰 중복)를 막기 위한 중복 방지 세트
        private val processedMessageIds =
            Collections.synchronizedSet(LinkedHashSet<String>())
    }
}
