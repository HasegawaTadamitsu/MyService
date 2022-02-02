package com.haselab.myservice

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.*

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
                }
            }
        }
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

        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        _locationRequest = LocationRequest.create()
        _locationRequest.let {
            it.interval = 5000 // milli sec
            it.fastestInterval = 1000 // mill sec
            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        _onUpdateLocation = OnUpdateLocation()
        Log.v(TAG, "permission check")
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.v(TAG,"permission check error" )
            msgPermissionError()
            return
        }
        Log.v(TAG, "permission ok")
        _fusedLocationClient.requestLocationUpdates(_locationRequest, _onUpdateLocation, mainLooper)
    }


    private fun msg() {
        Log.v(TAG,"msg start")

        // Notificationを作成するBuilderクラス生成。
        val builder = NotificationCompat.Builder(this@SampleService, CHANNEL_ID)
        // 通知エリアに表示されるアイコンを設定。
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
        // 通知ドロワーでの表示タイトルを設定。
        builder.setContentTitle("error Permission")
        // 通知ドロワーでの表示メッセージを設定。
        builder.setContentText("need permission")
        // BuilderからNotificationオブジェクトを生成。
        val notification = builder.build()
        // NotificationManagerCompatオブジェクトを取得。
        val manager = NotificationManagerCompat.from(this@SampleService)
        // 通知。
        manager.notify(100, notification)
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.v(TAG,"onBind")
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v(TAG,"onStartCommand")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.v(TAG,"onDestory")
        super.onDestroy()
        _fusedLocationClient.removeLocationUpdates(_onUpdateLocation)

    }

}
