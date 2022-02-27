package com.haselab.myservice

import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

private const val TAG = "VibrationMgr"
private const val INTERVAL_VIBRATE_MILLI_SEC = 3 * 60 * 1000 // milli sec

object VibrationMgr {
    private var mLastVibrateLong: Long = 0 // last vibrate milli sec
    private val mEffectSingle by lazy {
        VibrationEffect.createOneShot(
            200, 100 //VibrationEffect.DEFAULT_AMPLITUDE
        )
    }

    private var vb: Vibrator? = null
    fun setVibrator(_vb: Vibrator) {
        vb = _vb
    }

    fun single() {
        Log.v(TAG, "vibrator_single ")

        if (!checkInterval()) {
            return
        }
        if (vb != null && vb is Vibrator) {
            vb!!.vibrate(mEffectSingle)
        }
    }

    private fun checkInterval(): Boolean {
        val now = System.currentTimeMillis()
        if (mLastVibrateLong + INTERVAL_VIBRATE_MILLI_SEC < now) {
            mLastVibrateLong = now
            return true
        }
        return false
    }
}
