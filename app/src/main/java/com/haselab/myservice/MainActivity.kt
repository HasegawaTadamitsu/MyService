package com.haselab.myservice

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


private const val TAG = "MainActivity"

class MainActivity() : AppCompatActivity(), Parcelable, MsgWriteCallback {

    private val btStartStopLabelStart = "START"
    private val btStartStopLabelStop = "Stop"
    private val mDatabaseHelper = DatabaseHelper(this)
    private lateinit var mBatteryMgr: BatteryMgr
    private lateinit var mVibrationMgr: VibrationMgr

    constructor(parcel: Parcel) : this()

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
        Log.v(TAG, "start onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val channel = NotificationChannel(
            SampleService.CHANNEL_ID, "Location Get Sample",
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        if (isRunningService()) {
            Log.v(TAG, "running  service ")
            setLabelBtStartStop(btStartStopLabelStop)
        } else {
            Log.v(TAG, "not running  service ")
            setLabelBtStartStop(btStartStopLabelStart)
        }

        val view = findViewById<View>(R.id.btReload)
        onBtReloadClick(view)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.v(TAG, "error  permission")
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, 1000)
            return
        }
        Log.v(TAG, "permission ok")

        mBatteryMgr = BatteryMgr(this)
        mBatteryMgr.initMgr()
        mVibrationMgr =
            VibrationMgr(this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)


    }

    override fun isGPSRunning(): Boolean {
        return isRunningService()
    }

    private fun isRunningService(): Boolean {
        Log.v(TAG, "start isRunningService ")
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        manager.getRunningServices(Integer.MAX_VALUE).forEach { serviceInfo ->
            if (SampleService::class.java.name == (serviceInfo.service.className)) {
                Log.v(TAG, "${SampleService::class.java.name} == ${serviceInfo.service.className}")
                return true
            }
        }
        return false
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
            Log.v(TAG, "button label is START(true)")
            return true
        }
        Log.v(TAG, "button label is STOP(false)")
        return false
    }

    fun onBtReloadClick(view: View) {
        Log.v(TAG, "start onBtReloadClick")
        val db = mDatabaseHelper.readableDatabase
        val columns = arrayOf("_id", "time", "lat", "lon")
        val cursor = db.query(
            "location", columns, null, null,
            null, null, "time desc"
        )

        val headers = arrayOf("_id", "time", "lat", "lon")
        val layouts = intArrayOf(R.id.id, R.id.time, R.id.lat, R.id.lon)

        val adapter = SimpleCursorAdapter(
            this,
            R.layout.row_main,
            cursor,
            headers,
            layouts,
            SimpleCursorAdapter.FLAG_AUTO_REQUERY
        )
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
            Log.v(TAG, "$lat")
            val lonTextView = view.findViewById(R.id.lon) as TextView
            val lon = lonTextView.text
            Log.v(TAG, "$lon")
            val uri = Uri.parse("geo:${lat},${lon}")
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

    override fun startGPS(): Boolean {
        Log.v(TAG, "start startGPS")
        if (isRunningService()) {
            Log.v(TAG, "already start service")
            return true
        }
        val intentSampleService = Intent(this@MainActivity, SampleService::class.java)
        startForegroundService(intentSampleService)
        setLabelBtStartStop(btStartStopLabelStop)
        return true
    }

    override fun stopGPS(): Boolean {
        Log.v(TAG, "start stopGPS")
        if (!isRunningService()) {
            Log.v(TAG, "already stop service")
            return true
        }
        val intentSampleService = Intent(this@MainActivity, SampleService::class.java)
        stopService(intentSampleService)
        setLabelBtStartStop(btStartStopLabelStart)
        return true
    }

    fun onBtStartStopClick(view: View) {
        Log.v(TAG, "start onBtStartStopClick")
        if (isStartBtStartStop()) {
            startGPS()
        } else {
            stopGPS()
        }
    }

    override fun getLastLocate(): Location {
        val db = mDatabaseHelper.readableDatabase
        val columns = arrayOf("_id", "time", "lat", "lon")
        val cursor = db.query(
            "location",
            columns,
            null,
            null,
            null,
            null,
            "time desc",
            "1"
        )
        cursor.moveToFirst()
        val ixId = cursor.getColumnIndex("_id")
        val id = cursor.getLong(ixId)
        val ixTime = cursor.getColumnIndex("time")
        val time = cursor.getLong(ixTime)
        val ixLat = cursor.getColumnIndex("lat")
        val lat = cursor.getDouble(ixLat)
        val ixLon = cursor.getColumnIndex("lon")
        val lon = cursor.getDouble(ixLon)
        cursor.close()
        db.close()
        return Location(id, time, lat, lon)
    }

    override fun deleteLocate(id: Long) {
        Log.v(TAG, "start deleteLocate $id")
        val sql = "DELETE from location  where _id = ?"
        val db = mDatabaseHelper.writableDatabase
        db.beginTransaction()
        val stmt = db.compileStatement(sql)
        stmt.bindLong(1, id)
        stmt.executeUpdateDelete()
        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
    }

    override fun onResume() {
        Log.v(TAG, "start onResume")
        super.onResume()
        if (!HttpTask.isReady()) {
            HttpTask.setInit(this, mDatabaseHelper.getUUDI())
        }
        if (!HttpTask.isRunning()) {
            Log.v(TAG, "start httpTask")
            HttpTask.execute()
        } else {
            Log.v(TAG, "already running  httpTask")
        }
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

    val dtformat1 = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss")

    @SuppressLint("SetTextI18n")
    override fun setServerMsg(str: String): Boolean {
        val now = LocalDateTime.now()
        val dateStr = dtformat1.format(now)
        val msg = findViewById<TextView>(R.id.tvMsg)
        msg.text = "$dateStr $str"
        return true
    }

    override fun fin(): Boolean {
        Log.v(TAG, "start fin")
        if (isRunningService()) {
            stopGPS()
        }
        HttpTask.stop()
        mDatabaseHelper.close()
        finish()
        return true
    }

    fun onBtFin(view: View) {
        Log.v(TAG, "start onBtFin")
        fin()
    }

    override fun bg(): Boolean {
        Log.v(TAG, "start bg")
        startGPS()
        finish()
        return true
    }

    fun onBtBg(view: View) {
        Log.v(TAG, "start onBtBg")
        bg()
    }

    override fun onDestroy() {
        Log.v(TAG, "start onDestroy")
        super.onDestroy()
        mBatteryMgr.destroy()
    }

    override fun getDBFile(): String {
        Log.v(TAG, "start uploadFBFile")
        val dbFilePath = this.getDatabasePath(DatabaseHelper.DATABASE_NAME)
        Log.v(TAG, "db = $dbFilePath")
        return dbFilePath.toString()
    }

    override fun getBatteryLevel(): BatteryInfo {
        Log.v(TAG, "start getBatteryLevel")
        return mBatteryMgr.getValues()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun vibrate() {
        Log.v(TAG, "start vibrate")
        mVibrationMgr.single()
    }
}

