package com.dashboard.kotlin.clashhelper

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class clashStatus {
    var trafficThreadFlag: Boolean = true
    var trafficRawText: String = "{\"up\":\"0\",\"down\":\"0\"}"
    private var isRunning = false

    fun runStatus(): Boolean {
        thread {
            try {
                isRunning = URL(clashConfig().baseURL).readText() == "{\"hello\":\"clash\"}\n"
            } catch (ex: Exception) {
                isRunning = false
            }
        }.join()

        return isRunning
    }

    fun getTraffic() {
        trafficThreadFlag = true
        Thread {
            try {
                val conn =
                    URL("${clashConfig().baseURL}/traffic").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer ${clashConfig().clashSecret}")
                conn.inputStream.use {
                    while (trafficThreadFlag) {
                        trafficRawText = it.bufferedReader().readLine()
                        Log.d("TRAFFIC", trafficRawText)
                    }
                }
            } catch (ex: Exception) {
                Log.w("W", ex.toString())
            }
        }.start()
    }

    fun stopGetTraffic() {
        trafficThreadFlag = false
    }

    fun getConfigPath(): String{
        return "/data/clash/"
    }
}

private class clashConfig {

    val baseURL: String
        get() {
            return "${getUrl()}:${getPort()}"
        }
    val clashSecret: String
        get() {
            return getSecret()
        }


    private fun getSecret(): String {
        return ""
    }

    private fun getUrl(): String {
        return "http://127.0.0.1"
    }

    private fun getPort(): String {
        return "9090"
    }




}