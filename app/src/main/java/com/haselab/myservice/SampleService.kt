package com.haselab.myservice

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "Service"

class SampleService : Service() {
    companion object {
        const val CHANNEL_ID = "SampleService_notification_channel"
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
            super.onLocationResult(locationResult)
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

    private val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

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
    }

    override fun onBind(intent: Intent): IBinder {
        Log.v(TAG, "onBind")
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.v(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)

        val myIntent = Intent(this@SampleService, MainActivity::class.java)
            .putExtra("fromNotification", true)
        val stopServiceIntent = PendingIntent.getActivity(this@SampleService,
            0,
            myIntent,
            PendingIntent.FLAG_CANCEL_CURRENT)


        val builder = NotificationCompat.Builder(this@SampleService, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(resources.getString(R.string.notifi_title))
            .setContentText(resources.getString(R.string.notifi_text))
            .setContentIntent(stopServiceIntent)
            .setAutoCancel(true)

        val notification = builder.build()
        startForeground(9999, notification)

        // init location
        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        _locationRequest = LocationRequest.create()
        _locationRequest.let {
            it.interval = 10000 // milli sec
            it.fastestInterval = 10000 // mill sec
            it.maxWaitTime = 5000 // mill sec
            it.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        _onUpdateLocation = OnUpdateLocation()


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
        _fusedLocationClient.requestLocationUpdates(_locationRequest, _onUpdateLocation, Looper.getMainLooper())
        return START_STICKY
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        _fusedLocationClient.removeLocationUpdates(_onUpdateLocation)
        _helper.close()
        super.onDestroy()
    }
}
