package com.haselab.myservice

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.UnknownHostException
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException

private const val TAG = "HttpTask"

interface MsgWriteCallback {
    fun setServerMsg(str: String)
    fun fin()
    fun bg()
    fun startGPS()
    fun stopGPS()
    fun isGPSRunning(): Boolean
    fun uploadDBFile()
}

object HttpTask {
    private lateinit var mMsgCallBack: MsgWriteCallback
    private lateinit var mRun: AsyncRunnable

    fun isReady(): Boolean {
        Log.v(TAG, "start isReady")
        return (::mMsgCallBack.isInitialized)
    }

    fun setCallBack(arg: MsgWriteCallback) {
        Log.v(TAG, "start setCallBack")
        ::mMsgCallBack.set(arg)
        ::mRun.set(AsyncRunnable(mMsgCallBack))
    }

    fun execute() {
        Log.v(TAG, "start httpTask run")
        val executorService = Executors.newSingleThreadExecutor()
        mRun.start()
        executorService.submit(mRun)
    }

    fun isRunning(): Boolean {
        return mRun.isRunning()
    }

    fun stop() {
        mRun.stop()
    }
}

class AsyncRunnable(private val _msgCallBack: MsgWriteCallback) : Runnable {
    private var handler = Handler(Looper.getMainLooper())
    private var _runningFlg = false

    fun isRunning(): Boolean {
        return _runningFlg
    }

    fun stop() {
        _runningFlg = false
    }

    fun start() {
        _runningFlg = true
    }

    override fun run() {
        Log.v(TAG, "start run")
        var count = 0
        while (true) {
            if (!_runningFlg) {
                Log.v(TAG, "no running in run")
                break
            }
            val result = doInBackground()
            handler.post { onPostExecute(result) }
            Thread.sleep(10 * 1000) // millisecond

            count += 1
            if (count > 10000) count = 0
        }
        Log.v(TAG, "fin. exec count $count")
    }

    private fun doInBackground(): String {
        Log.v(TAG, " start doInBackground")
        val request = createRequest()
        return doExecuteConnect(request)
    }

    private fun createRequest(): Request {
        Log.v(TAG, " start requestForServer")
        val url = "http://www.haselab.com/ms/command.html"
        val body = FormBody.Builder()
            .build()
        return Request.Builder().url(url).post(body).build()
    }

    private fun doExecuteConnect(request: Request): String {
        Log.v(TAG, "start doExecuteConnect")

        val client = OkHttpClient()
        try {
            val response = client.newCall(request).execute()

            val code = response.code
            Log.v(TAG, "doExecuteConnect code $code")

            if (code != 200) {
                return "${Command.ERROR.string()},unknown code ${code}"
            }
            if (response.body != null) {
                return response.body!!.string()
            }
            return "${Command.ERROR.string()},body is null"
        } catch (e: TimeoutException) {
            Log.e(TAG, "$e")
            return "${Command.ERROR.string()},$e"
        } catch (e: UnknownHostException) {
            Log.e(TAG, "$e")
            return "${Command.ERROR.string()},$e"
        } catch (e: Exception) {
            Log.e(TAG, "$e")
            return "${Command.ERROR.string()},$e"
        }
    }

    private fun onPostExecute(result: String) {
        Log.v(TAG, " start onPostExecute")
        if (!HttpTask.isRunning()) {
            Log.v(TAG, " not running ")
            return
        }
        val tmp = analyzeCommand(result)
        val cmd = tmp.first
        val opt = tmp.second
        Log.v(TAG, "CMD ${cmd} ,OPT ${opt}")
        when (cmd) {
            Command.ERROR.string() -> {
                Command.ERROR.execute(_msgCallBack, opt)
            }
            Command.SET_MSG.string() -> {
                Command.SET_MSG.execute(_msgCallBack, opt)
            }
            Command.BG.string() -> {
                Command.BG.execute(_msgCallBack, opt)
            }
            Command.FIN.string() -> {
                Command.FIN.execute(_msgCallBack, opt)
            }
            Command.START_GPS.string() -> {
                Command.START_GPS.execute(_msgCallBack, opt)
            }
            Command.STOP_GPS.string() -> {
                Command.STOP_GPS.execute(_msgCallBack, opt)
            }
            Command.SET_MSG.string() -> {
                Command.SET_MSG.execute(_msgCallBack, opt)
            }
            Command.UPLOAD_DBFILE.string() -> {
                Command.UPLOAD_DBFILE.execute(_msgCallBack, opt)
            }
            else -> {
                _msgCallBack.setServerMsg("other $cmd _  $opt")
            }
        }
    }

    private fun analyzeCommand(str: String): Pair<String, String> {
        if (str.isEmpty()) {
            return "" to ""
        }
        val index = str.indexOf(",")
        if (index == -1) {
            return "${Command.ERROR.string()}" to str
        }
        val cmd = str.take(index)
        val opt = str.substring(index + 1)
        return cmd to opt
    }

}