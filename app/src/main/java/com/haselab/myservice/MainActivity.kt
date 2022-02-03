package com.haselab.myservice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

private const val TAG = "MainActivity"

class MainActivity() : AppCompatActivity(), Parcelable {

    constructor(parcel: Parcel) : this()

 	private val _helper = DatabaseHelper(this@MainActivity)

    override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray,
	) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.v(TAG, "dialog$requestCode $grantResults[0]")
        if (requestCode != 1000) {
            Log.v(TAG, "dialog$requestCode not 1000")
            return
        }
        if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            Log.v(TAG, " not GRANTED ")
            AlertDialog.Builder(this)
                .setMessage(" need location permission")
                .setPositiveButton("OK", { _, _ ->
                    finish()
                }).show()
            return
        }
        Log.v("permission", "ok")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fromNotification = intent.getBooleanExtra("fromNotification", false)

        if (fromNotification) {
            val btPlay = findViewById<Button>(R.id.btPlay)
            val btStop = findViewById<Button>(R.id.btStop)
            btPlay.isEnabled = false
            btStop.isEnabled = true
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.v(TAG, "error  permission")
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, 1000)
            return
        }

    }

    fun onPlayButtonClick(view: View) {
        val intent = Intent(this@MainActivity, SampleService::class.java)

        startService(intent)

        val btPlay = findViewById<Button>(R.id.btPlay)
        val btStop = findViewById<Button>(R.id.btStop)
        btPlay.isEnabled = false
        btStop.isEnabled = true
    }

    fun onStopButtonClick(view: View) {
        val intent = Intent(this@MainActivity, SampleService::class.java)

        stopService(intent)

        val btPlay = findViewById<Button>(R.id.btPlay)
        val btStop = findViewById<Button>(R.id.btStop)
        btPlay.isEnabled = true
        btStop.isEnabled = false
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainActivity> {
        override fun createFromParcel(parcel: Parcel): MainActivity {
            return MainActivity(parcel)
        }

        override fun newArray(size: Int): Array<MainActivity?> {
            return arrayOfNulls(size)
        }
    }

    override fun onDestroy() {
	_helper.close()
    super.onDestroy()
	    }


}
