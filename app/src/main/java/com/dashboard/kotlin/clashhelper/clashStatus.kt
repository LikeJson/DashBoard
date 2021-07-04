package com.dashboard.kotlin.clashhelper

import android.util.Log
import com.dashboard.kotlin.GExternalCacheDir
import java.net.HttpURLConnection
import java.net.URL
import com.dashboard.kotlin.suihelper.suihelper
import java.io.File
import kotlin.concurrent.thread

class clashStatus {
    var trafficThreadFlag: Boolean = true
    var trafficRawText: String = "{\"up\":\"0\",\"down\":\"0\"}"

    fun runStatus(): Boolean {
        var isRunning = false
         thread(start = true){
            isRunning = try {
                URL(clashConfig().baseURL).readText() == "{\"hello\":\"clash\"}\n"
            } catch (ex: Exception) {
                false
            }
        }.join()
        return isRunning
    }

    fun getTraffic() {
        trafficThreadFlag = true
        val secret = clashConfig().clashSecret
        val baseURL = clashConfig().baseURL
        Thread {
            try {
                val conn =
                    URL("${baseURL}/traffic").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer $secret")
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


}

class clashConfig {

    val clashPath: String
        get() {
            return getConfigPath()
        }

    val baseURL: String
        get() {
            return "http://${getExternalController()}"
        }
    val clashSecret: String
        get() {
            return getSecret()
        }


    private fun getSecret(): String {
        suihelper().suCmd("cp /data/clash/template ${GExternalCacheDir}/template")

        var tempStr: String = ""
        try {
            tempStr = getFromFile("${GExternalCacheDir}/template", "secret")
            File("${GExternalCacheDir}/template").delete()
        } catch (ex: Exception) {
            Log.w("readFromFile", ex.toString())
        }


        return tempStr
    }

    private fun getConfigPath(): String {
        return "/data/clash/"
    }

    private fun getExternalController(): String {
        suihelper().suCmd("cp /data/clash/template ${GExternalCacheDir}/template")

        var tempStr: String = ""
        try {
            tempStr = getFromFile("${GExternalCacheDir}/template", "external-controller")
            File("${GExternalCacheDir}/template").delete()
        } catch (ex: Exception) {
            Log.w("readFromFile", ex.toString())
        }

        return tempStr
    }

    private external fun getFromFile(path: String, node: String): String

    companion object {
        init {
            System.loadLibrary("yaml-reader")
        }
    }
}