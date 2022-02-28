package com.haselab.myservice

import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.net.UnknownHostException
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException

private const val TAG = "HttpTask"


object HttpTask {
    private lateinit var mMsgCallBack: MsgWriteCallback
    private lateinit var mRun: AsyncRunnable
    fun isReady(): Boolean {
        Log.v(TAG, "start isReady")
        return (::mMsgCallBack.isInitialized)
    }

    fun setInit(arg: MsgWriteCallback, uuid: String) {
        Log.v(TAG, "start setCallBack")
        ::mMsgCallBack.set(arg)
        ::mRun.set(AsyncRunnable(mMsgCallBack, uuid))
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

class AsyncRunnable(
    private val _msgCallBack: MsgWriteCallback,
    private val mUUID: String
) : Runnable {
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
        var sequence = 0
        while (true) {
            if (!_runningFlg) {
                Log.v(TAG, "no running in run")
                break
            }
            Thread.sleep(10 * 1000) // millisecond
            sequence += 1
            if (sequence > 10000) sequence = 0

            val (cmd, msg) = execGetCommand(sequence)
            if (cmd == Command.ERROR || cmd == Command.SET_MSG) {
                handler.post {
                    execCommand(Command.SET_MSG, msg)
                }
                continue
            }
            handler.post {
                execCommand(cmd, msg)
            }
            val (resultFlg, resultMsg) = execSendResult(sequence, cmd)
            handler.post {
                execCommand(Command.SET_MSG, resultMsg)
            }
        }
        Log.v(TAG, "fin. exec count $sequence")
    }

    private fun execGetCommand(seq: Int): Pair<Command, String> {
        Log.v(TAG, " start execGetCommand seq=$seq")
        val request = createGetCommandRequest(seq)
        return doExecuteConnect(request)
    }

    private fun execSendResult(seq: Int, cmd: Command): Pair<Boolean, String> {
        Log.v(TAG, " start execSendResult seq=$seq, cmd=$cmd")
        return when (cmd) {
            Command.UPLOAD_DB_FILE -> {
                val request = createUploadRequest(seq, cmd)
                sendResult(request)
            }
            Command.GET_BATTERY_LEVEL -> {
                val info = _msgCallBack.getBatteryLevel()
                val json = info.json()
                val request = createJSONResultRequest(seq, cmd, json)
                sendResult(request)
            }
            Command.IS_RUNNING_GPS -> {
                val flg = _msgCallBack.isGPSRunning()
                val request = createBooleanResultRequest(seq, cmd, flg)
                sendResult(request)
            }
            else -> {
                true to "OK"
            }
        }
    }

    private var lastCompleteLocateId = 0L
    private var lastBatterySendMillisSec = 0L
    private var nextBatterySendMillisSec = 0L

    private fun initSendJSON(seq: Int): JSONObject {
        val locate = _msgCallBack.getLastLocate()
        val json = JSONObject()
        json.put("seq", seq)
        json.put("send_time", System.currentTimeMillis())
        json.put("uuid", mUUID)
                json.put("locate_id", locate.id)
            json.put("locate_time", locate.time)
            json.put("locate_lat", locate.lat)
            json.put("locate_lon", locate.lon)
            lastCompleteLocateId = locate.id

        if (lastBatterySendMillisSec + 30 * 60 * 1000 < System.currentTimeMillis()) {
            val info = _msgCallBack.getBatteryLevel()
            val batJson = info.json()
            batJson.keys().forEach { s: String ->
                val data = batJson.get(s)
                json.put(s, data)
            }
            nextBatterySendMillisSec = System.currentTimeMillis()
        }
        return json
    }

    private fun createGetCommandRequest(seq: Int): Request {
        Log.v(TAG, " start createGetCommandRequest")
        val url = "http://www.haselab.com/ms/command.html"
//        val url = "http://192.168.33.201:5001/ms/command.html"

        val json = initSendJSON(seq)
        Log.v(TAG, "json $json")

        return Request.Builder()
            .url(url)
            .post(
                json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()
    }

    private fun createBooleanResultRequest(seq: Int, cmd: Command, flg: Boolean): Request {
        Log.v(TAG, " start createBooleanResultRequest")
        val url = "http://www.haselab.com/ms/result.html"
        val json = initSendJSON(seq)
        json.put("cmd", cmd.str)
        json.put("flg", flg)
        Log.v(TAG, "json $json")

        return Request.Builder()
            .url(url)
            .post(
                json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()
    }

    private fun createJSONResultRequest(seq: Int, cmd: Command, addJson: JSONObject): Request {
        Log.v(TAG, " start createJSONResultRequest")
        val url = "http://www.haselab.com/ms/result.html"
        val json = initSendJSON(seq)
        json.put("cmd", cmd.str)
        addJson.keys().forEach { s: String ->
            val data = addJson.get(s)
            json.put(s, data)
        }
        Log.v(TAG, "json $json")

        return Request.Builder()
            .url(url)
            .post(
                json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()
    }

    private fun createUploadRequest(seq: Int, cmd: Command): Request {
        Log.v(TAG, " start createUploadRequest")

        val filename = _msgCallBack.getDBFile()
        val file = File(filename)
        val base64 = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)

        val url = "http://www.haselab.com/ms/upload.html"
//        val url = "http://192.168.33.201:5001/ms/upload.html"
        val json = initSendJSON(seq)
        json.put("cmd", cmd.str)
        json.put("file_name", filename)
        json.put("file", base64)
        Log.v(TAG, "json $json")

        return Request.Builder()
            .url(url)
            .post(
                json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()
    }

    private fun analyzeCommand(str: String): Pair<Command, String> {
        Log.v(TAG, "analyzeCommand")
        if (str.isEmpty()) {
            return Command.NO_COMMAND to ""
        }
        val index = str.indexOf(",")
        if (index == -1) {
            Log.e(TAG, "no comma $str")
            return Command.ERROR to "no comma $str"
        }
        val cmd = str.take(index)
        val opt = str.substring(index + 1)
        return try {
            val definedCommand = Command.values().first { it.str == cmd }
            definedCommand to opt
        } catch (e: java.util.NoSuchElementException) {
            Log.e(TAG, "unknown command $str $e")
            Command.ERROR to "unknown $str ($e)"
        }
    }

    private fun doExecuteConnect(request: Request): Pair<Command, String> {
        Log.v(TAG, "start doExecuteConnect")

        val client = OkHttpClient()
        try {
            val response = client.newCall(request).execute()
            val code = response.code
            if (code != 200) {
                val strMsg = "doExecuteConnect unknown code $code"
                Log.e(TAG, strMsg)
                if ( response.body != null){
                    response.body!!.close()
                }
                response.close()
                return Pair(Command.ERROR, strMsg)
            }
            _msgCallBack.deleteLocate( lastCompleteLocateId )
            lastBatterySendMillisSec = nextBatterySendMillisSec
            if (response.body == null) {
                val strMsg = "doExecuteConnect body is null"
                Log.e(TAG, strMsg)
                response.close()
                return Pair(Command.ERROR, strMsg)
            }
            val str = response.body!!.string()
            response.body!!.close()
            response.close()
            Log.v(TAG, "doExecuteConnect body is $str")
            return analyzeCommand(str)
        } catch (e: TimeoutException) {
            val strMsg = "doExecuteConnect Timeout $e"
            Log.e(TAG, strMsg)
            return Pair(Command.ERROR, strMsg)
        } catch (e: UnknownHostException) {
            val strMsg = "doExecuteConnect UnknownHostException $e"
            Log.e(TAG, strMsg)
            return Pair(Command.ERROR, strMsg)
        } catch (e: Exception) {
            val strMsg = "doExecuteConnect OtherException  $e"
            Log.e(TAG, strMsg)
            return Pair(Command.ERROR, strMsg)
        }
    }

    private fun sendResult(request: Request): Pair<Boolean, String> {
        Log.v(TAG, "start sendResult")
        val client = OkHttpClient()
        try {
            val response = client.newCall(request).execute()
            val code = response.code
            if (code != 200) {
                val strMsg = "sendResult unknown code $code"
                Log.e(TAG, strMsg)
                if ( response.body != null){
                    response.body!!.close()
                }
                response.close()
                return Pair(false, strMsg)
            }
            _msgCallBack.deleteLocate( lastCompleteLocateId )
            lastBatterySendMillisSec = nextBatterySendMillisSec

            if (response.body == null) {
                val strMsg = "sendResult body is null"
                Log.e(TAG, strMsg)
                response.close()
                return Pair(false, strMsg)
            }
            val str = response.body!!.string()
            response.body!!.close()
            response.close()
            return Pair(true, str)
        } catch (e: TimeoutException) {
            val strMsg = "sendResult Timeout $e"
            Log.e(TAG, strMsg)
            return Pair(false, strMsg)
        } catch (e: UnknownHostException) {
            val strMsg = "sendResult unknownHostException  $e"
            Log.e(TAG, strMsg)
            return Pair(false, strMsg)
        } catch (e: Exception) {
            val strMsg = "sendResult Exception  $e"
            Log.e(TAG, strMsg)
            return Pair(false, strMsg)
        }
    }

    private fun execCommand(cmd: Command, resultMsg: String): Boolean {
        Log.v(TAG, " start execCommand cmd=$cmd resultMsg=$resultMsg")
        if (!HttpTask.isRunning()) {
            Log.v(TAG, " not running ")
            return false
        }
        return cmd.execute(_msgCallBack, resultMsg)
    }




}