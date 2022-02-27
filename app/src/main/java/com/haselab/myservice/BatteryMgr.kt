package com.haselab.myservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import org.json.JSONObject

private const val TAG = "BatteryMgr"

class BattertInfo(
    var level: Int, // percent
    var temperature: Int, // 1/10 temp
    var voltage: Int  // mill voltage
) {
    fun getInfoSttin(): String {
        return "level=$level,temperature=$temperature,voltage=$voltage"
    }

    fun json(): JSONObject {
        val json = JSONObject()
        json.put("level", level)
        json.put("temperature", temperature)
        json.put("voltage", voltage)
        return json
    }
}

class BatteryMgr(val context: Context) {
    var _battertInfo = BattertInfo(0, 0, 0)

    fun initMgr() {
        Log.v(TAG, "start initMgr")
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(BatteryServiceReceiver, intentFilter)
    }

    private val BatteryServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(TAG, "start onReceive")

            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                _battertInfo.level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                _battertInfo.temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                _battertInfo.voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
                val str = _battertInfo.getInfoSttin()
                Log.v(TAG, "info $str")
            }
        }
    }

    fun getValues(): BattertInfo {
        return _battertInfo
    }

    fun destry() {
        context.unregisterReceiver(BatteryServiceReceiver)
    }
}