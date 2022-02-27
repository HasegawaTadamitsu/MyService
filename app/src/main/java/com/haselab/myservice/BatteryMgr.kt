package com.haselab.myservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import org.json.JSONObject

private const val TAG = "BatteryMgr"

class BatteryInfo(
    var getTime: Long,
    var level: Int, // percent
    var temperature: Double, //  temp
    var voltage: Double  //  voltage
) {
    fun getInfoString(): String {
        return "getTime=$getTime, level=$level,temperature=$temperature,voltage=$voltage"
    }

    fun json(): JSONObject {
        val json = JSONObject()
        json.put("battery_get_time", getTime)
        json.put("level", level)
        json.put("temperature", temperature)
        json.put("voltage", voltage)
        return json
    }
}

class BatteryMgr(private val context: Context) {
    var mBatteryInfo = BatteryInfo(
        0,
        0,
        0.0,
        0.0
    )

    fun initMgr() {
        Log.v(TAG, "start initMgr")
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryServiceReceiver, intentFilter)
    }

    private val batteryServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(TAG, "start onReceive")

            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                mBatteryInfo.getTime = System.currentTimeMillis()
                mBatteryInfo.level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                mBatteryInfo.temperature =
                    intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1).toDouble() / 10.0
                mBatteryInfo.voltage =
                    intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1).toDouble() / 1000.0
                val str = mBatteryInfo.getInfoString()
                Log.v(TAG, "info $str")
            }
        }
    }

    fun getValues(): BatteryInfo {
        return mBatteryInfo
    }

    fun destroy() {
        context.unregisterReceiver(batteryServiceReceiver)
    }
}