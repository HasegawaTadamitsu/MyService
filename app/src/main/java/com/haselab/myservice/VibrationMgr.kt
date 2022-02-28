package com.haselab.myservice

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.annotation.RequiresApi

private const val TAG = "VibrationMgr"
private const val INTERVAL_VIBRATE_MILLI_SEC = 1 * 1000 // milli sec   1 sec

class VibrationMgr(private val mVibration: Vibrator) {
    private var mLastVibrateLong: Long = 0 // last vibrate milli sec
    private val mEffectSingle by lazy {
        VibrationEffect.createOneShot(
            2000, VibrationEffect.DEFAULT_AMPLITUDE
        )
    }

    fun single() {
        Log.v(TAG, "start single")
        if (!checkInterval()) {
            Log.v(TAG, "checkInterval is false")
            return
        }
        Log.v(TAG, "do vibration")
        mVibration.vibrate(mEffectSingle)
        mLastVibrateLong = System.currentTimeMillis()
    }

    private fun checkInterval(): Boolean {
        Log.v(TAG, "start checkInterval")
        val now = System.currentTimeMillis()
        if (mLastVibrateLong + INTERVAL_VIBRATE_MILLI_SEC < now) {
            return true
        }
        return false
    }
}

