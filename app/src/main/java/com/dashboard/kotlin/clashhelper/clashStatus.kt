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
        thread(start = true) {
            isRunning = try {
                URL(clashConfig.baseURL).readText() == "{\"hello\":\"clash\"}\n"
            } catch (ex: Exception) {
                false
            }
        }.join()
        return isRunning
    }

    fun getTraffic() {
        trafficThreadFlag = true
        val secret = clashConfig.clashSecret
        val baseURL = clashConfig.baseURL
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

object clashConfig {

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

    var clashDashBoard: String
        get() {
            return setFile(
                getConfigPath(), "template"
            ) { getFromFile("${GExternalCacheDir}/template", "external-ui") }
        }
        set(value) {
            setFileNR(
                getConfigPath(), "template"
            ) { modifyFile("${GExternalCacheDir}/template", "external-ui", value) }
            return
        }


    private fun getSecret(): String {
        return setFile(
            getConfigPath(), "template"
        ) { getFromFile("${GExternalCacheDir}/template", "secret") }
    }

    private fun getConfigPath(): String {
        return "/data/clash"
    }

    private fun getExternalController(): String {
        return setFile(
            "/data/clash", "template"
        ) { getFromFile("${GExternalCacheDir}/template", "external-controller") }
    }

    private fun setFile(dirPath: String, fileName: String, func: () -> String): String {
        copyFile(dirPath, fileName)
        val temp = func()
        deleteFile(GExternalCacheDir, fileName)
        return temp
    }

    private fun setFileNR(dirPath: String, fileName: String, func: () -> Unit) {
        copyFile(dirPath, fileName)
        func()
        suihelper().suCmd("cp ${GExternalCacheDir}/${fileName} ${dirPath}/${fileName} ")
        deleteFile(GExternalCacheDir, fileName)
    }

    private fun copyFile(dirPath: String, fileName: String) {
        suihelper().suCmd("cp ${dirPath}/${fileName} ${GExternalCacheDir}/${fileName}")
        return
    }

    private fun deleteFile(dirPath: String, fileName: String) {
        try {
            File(dirPath, fileName).delete()
        } catch (ex: Exception) {
            null
        }
    }

    private external fun getFromFile(path: String, node: String): String
    private external fun modifyFile(path: String, node: String, value: String)


    init {
        System.loadLibrary("yaml-reader")
    }
    
}