package com.haselab.myservice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class SampleService : Service() {
    companion object {
        private const val CHANNEL_ID = "SampleService_notification_channel"
    }

    override fun onCreate() {
        log.v(TAG,"onCreate start")
        // 通知チャネル名をstrings.xmlから取得。
        val name = getString(R.string.notification_channel_name)
        // 通知チャネルの重要度を標準に設定。
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        // 通知チャネルを生成。
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        // NotificationManagerオブジェクトを取得。
        val manager = getSystemService(NotificationManager::class.java)
        // 通知チャネルを設定。
        manager.createNotificationChannel(channel)
    }

    private fun msg() {
        Log.v(TAG,"msg start")

        // Notificationを作成するBuilderクラス生成。
        val builder = NotificationCompat.Builder(this@SampleService, CHANNEL_ID)
        // 通知エリアに表示されるアイコンを設定。
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
        // 通知ドロワーでの表示タイトルを設定。
        builder.setContentTitle(getString(R.string.msg_notification_title_finish))
        // 通知ドロワーでの表示メッセージを設定。
        builder.setContentText(getString(R.string.msg_notification_text_finish))
        // BuilderからNotificationオブジェクトを生成。
        val notification = builder.build()
        // NotificationManagerCompatオブジェクトを取得。
        val manager = NotificationManagerCompat.from(this@SampleService)
        // 通知。
        manager.notify(100, notification)
//        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
       msg()
        return START_NOT_STICKY
    }

    override fun onDestroy() {

    }

}
