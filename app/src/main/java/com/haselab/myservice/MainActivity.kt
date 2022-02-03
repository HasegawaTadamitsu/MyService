package com.haselab.myservice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "MainActivity"

class MainActivity() : AppCompatActivity(), Parcelable {

    constructor(parcel: Parcel) : this(
    )

    private val btStartStopLabelStart = "START"
    private val btStartStopLabelStop = "Stop"
    private val _helper = DatabaseHelper(this)

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
                .setPositiveButton("OK") { _, _ ->
                    finish()
                }.show()
            return
        }
        Log.v("permission", "ok")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setLabelBtStartStop(btStartStopLabelStart)

        val fromNotification = intent.getBooleanExtra("fromNotification", false)
        if (fromNotification) {
            Log.v(TAG, "fromNotification")
            stopService(intent)
            setLabelBtStartStop(btStartStopLabelStart)
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
        val view = findViewById<View>(R.id.btReload) as View
        onBtReloadClick(view)
    }

    private fun setLabelBtStartStop(label: String) {
        Log.v(TAG, "start setLabelBtStartStop to $label")
        val btStartStop = findViewById<Button>(R.id.btStartStop)
        btStartStop.text = label
    }

    private fun isStartBtStartStop(): Boolean {
        Log.v(TAG, "start isStartBtStartStop")
        val btStartStop = findViewById<Button>(R.id.btStartStop)
        if (btStartStop.text == btStartStopLabelStart) {
            Log.v(TAG, "button is START(true)")
            return true
        }
        Log.v(TAG, "button is false")
        return false
    }

    fun onBtReloadClick(view: View) {
        Log.v(TAG, "start onBtReloadClick")
        val db = _helper.readableDatabase
        val columns = arrayOf("_id", "time", "lat", "lon")
        val cursor = db.query("location", columns, null, null,
            null, null, "time desc")

        val headers = arrayOf("_id", "time", "lat", "lon")
        val layouts = intArrayOf(R.id.id, R.id.time, R.id.lat, R.id.lon)

        val adapter = SimpleCursorAdapter(this,
            R.layout.row_main,
            cursor,
            headers,
            layouts,
            SimpleCursorAdapter.FLAG_AUTO_REQUERY)
        val listView = findViewById<View>(R.id.lvLocation) as ListView

        listView.onItemClickListener = ListViewClicker()
        adapter.viewBinder = ListBinder()
        listView.adapter = adapter
        db.close()
    }

    private inner class ListViewClicker : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            Log.v(TAG, "onClick ${id},${position}")
            val timeTextView = view.findViewById(R.id.time) as TextView
            Log.v(TAG, "${timeTextView.text}")
            val latTextView = view.findViewById(R.id.lat) as TextView
            val lat = latTextView.text
            Log.v(TAG, "${lat}")
            val lonTextView = view.findViewById(R.id.lon) as TextView
            val lon = lonTextView.text
            Log.v(TAG, "${lon}")
            val uri = Uri.parse ("geo:${lat},${lon}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    private inner class ListBinder : SimpleCursorAdapter.ViewBinder {
        @SuppressLint("SimpleDateFormat")
        val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        override fun setViewValue(view: View?, cursor: Cursor?, columnIndex: Int): Boolean {
            when (columnIndex) {
                0 -> { // _id  dummy
                }
                1 -> {  // time
                    val tmpView = view as TextView
                    val date = Date(cursor!!.getLong(columnIndex))
                    tmpView.text = df.format(date.time)
                    return true
                }
                2 -> {  // "lat"
                    val tmpView = view as TextView
                    tmpView.text = cursor!!.getDouble(columnIndex).toString()
                    return true
                }
                3 -> {  // "lon"
                    val tmpView = view as TextView
                    tmpView.text = cursor!!.getDouble(columnIndex).toString()
                    return true
                }
                else -> {}
            }
            return false
        }

    }


    fun onBtStartStopClick(view: View) {
        Log.v(TAG, "start onBtStartStopClick")
        val intent = Intent(this@MainActivity, SampleService::class.java)
        if (isStartBtStartStop()) {
            startService(intent)
            setLabelBtStartStop(btStartStopLabelStop)
        } else {
            stopService(intent)
            setLabelBtStartStop(btStartStopLabelStart)
        }
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
        super.onDestroy()
        _helper.close()
    }
}

