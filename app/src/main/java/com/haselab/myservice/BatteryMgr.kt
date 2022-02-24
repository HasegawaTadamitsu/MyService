package com.haselab.myservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class BattertInfo(var level: Int, var temperature: Int, var voltage: Int)

class BatteryMgr(val intentFilter: IntentFilter, val context: Context) {
    var _battertInfo = BattertInfo(0, 0, 0)

    fun create() {
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(BatteryServiceReceiver, intentFilter)
    }

    private val BatteryServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                _battertInfo.level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                _battertInfo.temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                _battertInfo.voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
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