package com.haselab.myservice

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "Service"

class SampleService : Service() {
    companion object {
        private const val CHANNEL_ID = "SampleService_notification_channel"
    }

    private var _latitude = 0.0
    private var _longitude = 0.0
    private lateinit var _fusedLocationClient: FusedLocationProviderClient
    private lateinit var _locationRequest: LocationRequest
    private lateinit var _onUpdateLocation: OnUpdateLocation

    private val _helper = DatabaseHelper(this)

    private inner class OnUpdateLocation : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.v(TAG, "start onLocationResult")
            locationResult.let { itx ->
                val location = itx.lastLocation
                location.let {
                    _latitude = it.latitude
                    _longitude = it.longitude
                    val text = _latitude.toString()
                    val text2 = _longitude.toString()
                    Log.v(TAG, "latitude = $text")
                    Log.v(TAG, "longitude = $text2")
                    writeDb()
                }
            }
        }
    }

    val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    private fun writeDb() {
        Log.v(TAG, "writeDB")
        val sqlInsert = "INSERT INTO location ( time, lat, lon) VALUES (?, ?, ?)"
        val db = _helper.writableDatabase
        db.beginTransaction()
        val stmt = db.compileStatement(sqlInsert)
        val currentTimeMillis = System.currentTimeMillis()
        val date = Date(currentTimeMillis)
        Log.v(TAG, "${df.format(date.time)}")
        stmt.bindLong(1, currentTimeMillis)
        stmt.bindDouble(2, _latitude)
        stmt.bindDouble(3, _longitude)
        stmt.executeInsert()

        db.setTransactionSuccessful()
        db.endTransaction()
    }

    override fun onCreate() {
        Log.v(TAG, "onCreate start")
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


        // Notificationを作成するBuilderクラス生成。
        val builder = NotificationCompat.Builder(this@SampleService, CHANNEL_ID)
        // 通知エリアに表示されるアイコンを設定。
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
        // 通知ドロワーでの表示タイトルを設定。
        builder.setContentTitle("msg title")
        // 通知ドロワーでの表示メッセージを設定。
        builder.setContentText("hmm")

        // 起動先Activityクラスを指定したIntentオブジェクトを生成。
        val intent = Intent(this@SampleService, MainActivity::class.java)
        // 起動先アクティビティに引き継ぎデータを格納。
        intent.putExtra("fromNotification", true)
        // PendingIntentオブジェクトを取得。
        val stopServiceIntent = PendingIntent.getActivity(this@SampleService,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT)
        // PendingIntentオブジェクトをビルダーに設定。
        builder.setContentIntent(stopServiceIntent)
        // タップされた通知メッセージを自動的に消去するように設定。
        builder.setAutoCancel(true)

        // BuilderからNotificationオブジェクトを生成。
        val notification = builder.build()
        // Notificationオブジェクトを元にサービスをフォアグラウンド化。
        startForeground(200, notification)

        // init location
        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        _locationRequest = LocationRequest.create()
        _locationRequest.let {
            it.interval = 5000 // milli sec
            it.fastestInterval = 1000 // mill sec
            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        _onUpdateLocation = OnUpdateLocation()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.v(TAG, "onBind")
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v(TAG, "onStartCommand")

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.v(TAG, "permission check error")
            stopSelf()
            return START_NOT_STICKY
        }
        Log.v(TAG, "permission ok")
        _fusedLocationClient.requestLocationUpdates(_locationRequest, _onUpdateLocation, mainLooper)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        _fusedLocationClient.removeLocationUpdates(_onUpdateLocation)
        // ヘルパーオブジェクトの解放。
        _helper.close()
        super.onDestroy()
    }
}
