package com.haselab.myservice

import android.util.Log

private const val TAG = "Command"

interface MsgWriteCallback {
    fun setServerMsg(str: String): Boolean
    fun fin(): Boolean
    fun bg(): Boolean
    fun startGPS(): Boolean
    fun stopGPS(): Boolean
    fun isGPSRunning(): Boolean
    fun getDBFile(): String
    fun getBatteryLevel(): BatteryInfo
    fun getLastLocate(): Location
}

enum class Command(val str: String) {
    NO_COMMAND("NO_COMMAND") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            return true
        }
    },
    GET_BATTERY_LEVEL("GET_BATTERY_LEVEL") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            return true
        }
    },
    FIN("FIN") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            Log.v(TAG, "start execute FIN")
            return callback.fin()
        }
    },
    BG("BG") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            Log.v(TAG, "start execute Background")
            return callback.bg()
        }
    },
    STOP_GPS("STOP_GPS") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            Log.v(TAG, "start execute STOP_GPS")
            return callback.stopGPS()
        }
    },
    START_GPS("START_GPS") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            Log.v(TAG, "start execute START_GPS")
            return callback.startGPS()
        }
    },
    IS_RUNNING_GPS("IS_RUNNING_GPS") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            return true
        }
    },
    VIBRATE("VIBRATE") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            VibrationMgr.single()
            return true
        }
    },
    UPLOAD_DB_FILE("UPLOAD_DB_FILE") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            Log.v(TAG, "start execute uploadDBFile")
            return false
        }
    },
    SET_MSG("SET_MSG") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            Log.v(TAG, "start execute SET_MSG")
            return callback.setServerMsg(arg)
        }
    },
    ERROR("ERROR") {
        override fun execute(callback: MsgWriteCallback, arg: String): Boolean {
            Log.v(TAG, "start execute ERROR")
            return callback.setServerMsg("ERROR arg")
        }
    };

    abstract fun execute(callback: MsgWriteCallback, arg: String): Boolean
}