package com.haselab.myservice

import android.util.Log

private const val TAG = "Command"

enum class Command(val str: String) {
    FIN("FIN") {
        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute FIN")
            callback.fin()
            return ""
        }
    },
    BG("BG") {
        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute Background")
            callback.bg()
            return ""
        }
    },
    STOP_GPS("STOP_GPS") {
        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute STOP_GPS")
            callback.stopGPS()
            return ""
        }
    },
    START_GPS("START_GPS") {
            override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute START_GPS")
            callback.startGPS()
            return ""
        }
    },
    IS_RUNNING_GPS("IS_RUNNING_GPS") {
        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute IS_RUNNING_GPS")
            return callback.isGPSRunning().toString()
        }
    },
    UPLOAD_DB_FILE("UPLOAD_DB_FILE") {
        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute uploadDBFile")
            return callback.uploadDBFile().toString()
        }
    },
    SET_MSG("SET_MSG") {
        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute SET_MSG")
            callback.setServerMsg(arg)
            return ""
        }
    },
    ERROR("ERROR") {

        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute ERROR")
            callback.setServerMsg("ERROR ${arg}")
            return ""
        }
    };
    abstract fun execute(callback: MsgWriteCallback, arg: String): String
}