package com.haselab.myservice

import android.util.Log

private const val TAG = "Command"

enum class Command {
    FIN {
        override fun string(): String {
            return "FIN"
        }

        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute FIN")
            callback.fin()
            return ""
        }
    },
    BG {
        override fun string(): String {
            return "BG"
        }

        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute Background")
            callback.bg()
            return ""
        }
    },
    STOP_GPS {
        override fun string(): String {
            return "STOP_GPS"
        }

        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute STOP_GPS")
            callback.stopGPS()
            return ""
        }
    },
    START_GPS {
        override fun string(): String {
            return "START_GPS"
        }

        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute START_GPS")
            callback.startGPS()
            return ""
        }
    },
    IS_RUNNING_GPS {
        override fun string(): String {
            return "IS_RUNNING_GPS"
        }

        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute IS_RUNNING_GPS")
            return callback.isGPSRunning().toString()
        }
    },
    UPLOAD_DBFILE {
        override fun string(): String {
            return "UPLOAD_DBFILE"
        }

        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute uploadDBFile")
            return callback.uploadDBFile().toString()
        }
    },
    SET_MSG {
        override fun string(): String {
            return "SET_MSG"
        }

        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute SET_MSG")
            callback.setServerMsg(arg)
            return ""
        }
    },
    ERROR {
        override fun string(): String {
            return "ERR"
        }

        override fun execute(callback: MsgWriteCallback, arg: String): String {
            Log.v(TAG, "start execute ERROR")
            callback.setServerMsg("ERROR" + arg)
            return ""
        }
    };

    abstract fun string(): String
    abstract fun execute(callback: MsgWriteCallback, arg: String): String
}