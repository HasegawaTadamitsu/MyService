package com.haselab.myservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class BatteryMgr (val _intentFilter:IntentFilter,val _context: Context ){
    var _level       : Int    = 0
    var _temperature : Int    = 0
    var _voltage     : Int    = 0

    fun create() {
        _intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        _context.registerReceiver(BatteryServiceReceiver, _intentFilter)
    }

    private val BatteryServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED){
                _level       = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1)
                _temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1)
                _voltage     = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1)
            }
        }
    }
    fun getValues():Triple<Int,Int,Int>{
        return Triple(_level,_temperature,_voltage)
    }

    fun destry() {
        _context.unregisterReceiver(BatteryServiceReceiver)
    }
}