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

class HttpTask(private val _msgCallBack: MsgWriteCallback) {

    private var runningFlg = false

    fun execute() {
        runningFlg = true
        val executorService = Executors.newSingleThreadExecutor()
        executorService.submit(AsyncRunnable())
    }

    fun isRunning(): Boolean {
        return runningFlg
    }

    fun stop() {
        runningFlg = false
    }

    inner class AsyncRunnable : Runnable {
        private var handler = Handler(Looper.getMainLooper())
        override fun run() {
            Log.v(TAG, "start run")
            var count = 0
            while (true) {
                if (!isRunning()) {
                    Log.v(TAG, "no running in run")
                    break
                }
                val result = doInBackground()
                handler.post { onPostExecute("${count}: " + result) }
                Thread.sleep(10 * 1000) // millisecond

                count += 1
                if (count > 10000) count = 0
            }
            stop()
            Log.v(TAG, "fin. run")
        }
    }

    fun doInBackground(): String {
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
                return ""
            }
            if (response.body != null) {
                return response.body!!.string()
            }
            return ""
        } catch (e: TimeoutException) {
            Log.e(TAG, "$e")
            //   Thread.sleep(5 * 1000) // millisecond
        } catch (e: UnknownHostException) {
            Log.e(TAG, "$e")
            //   Thread.sleep(5 * 1000) // millisecond
        } catch (e: Exception) {
            Log.e(TAG, "$e")
            //    Thread.sleep(5 * 1000) // millisecond
        }
        return ""
    }

    fun onPostExecute(result: String) {
        Log.v(TAG, " start onPostExecute")
        if (!isRunning()) {
            return
        }
        _msgCallBack.doWrite(result)
    }
}

