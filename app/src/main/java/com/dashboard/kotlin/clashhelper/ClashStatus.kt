package com.dashboard.kotlin.clashhelper

import android.util.Log
import com.dashboard.kotlin.GExternalCacheDir
import java.net.HttpURLConnection
import java.net.URL
import com.dashboard.kotlin.suihelper.SuiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import rikka.shizuku.Shizuku
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import kotlin.concurrent.thread

class ClashStatus {
    var statusThreadFlag: Boolean = true
    var statusRawText: String = ""//"{\"up\":\"0\",\"down\":\"0\",\"RES\":\"0\",\"CPU\":\"0%\"}"

    fun runStatus(): Boolean {
        var isRunning = false
        thread(start = true) {
            isRunning = try {
                val conn =
                    URL(ClashConfig.baseURL).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer ${ClashConfig.clashSecret}")
                conn.inputStream.bufferedReader().readText() == "{\"hello\":\"clash\"}\n"
            } catch (ex: Exception) {
                false
            }
        }.join()
        return isRunning
    }

    fun getStatus() {
        statusThreadFlag = true
        val secret = ClashConfig.clashSecret
        val baseURL = ClashConfig.baseURL

        var process: Process? = null
        var os: DataOutputStream? = null
        var ls: DataInputStream? = null
        Thread {
            try {
                val conn =
                    URL("${baseURL}/traffic").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer $secret")

                process = try {
                    Shizuku.newProcess(arrayOf("sh"), null, null)
                }catch (e: Exception){
                    Runtime.getRuntime().exec("su")
                }
                os = DataOutputStream(process?.outputStream)
                ls = DataInputStream(process?.inputStream)
                os?.writeBytes(
                    "top -p `cat ${ClashConfig.clashPath}/run/clash.pid` | " +
                            "grep clash\n")//
                //// os!!.writeBytes("exit\n")
                os?.flush()

                conn.inputStream.use {
                    while (statusThreadFlag) {
                        val status = ls?.bufferedReader()?.readLine()?.split(Regex(" +"))
                        statusRawText = it.bufferedReader().readLine()
                            .replace("}", ",\"RES\":\"${
                                status?.get(6)
                            }\",\"CPU\":\"${
                                status?.get(9)
                            }%\"}")
                    }
                }
            } catch (ex: Exception) {

                Log.d("TRAFFIC-W", ex.toString())
            }finally {
                runCatching {
                    os?.close()
                    ls?.close()
                    process?.destroy()
                }
            }
        }.start()
    }

    fun stopGetStatus() {
        statusThreadFlag = false
    }


}

object ClashConfig {

    val corePath: String
        get() = "/data/adb/modules/Clash_For_Magisk/system/bin/clash"

    val scriptsPath: String
        get() = "/data/clash/scripts"

    val clashPath: String
        get() = "/data/clash"

    val baseURL: String
        get() {
            return "http://${getExternalController()}"
        }
    val clashSecret: String
        get() {
            return getSecret()
        }

    var clashDashBoard: String
        get() = getFromFile("${GExternalCacheDir}/template", "external-ui")
        set(value) {
            setFileNR(
                clashPath, "template"
            ) { modifyFile("${GExternalCacheDir}/template", "external-ui", value) }
            return
        }

    fun updateConfig(type: String) {
        when (type) {
            "CFM" -> {
                mergeConfig("${clashPath}/run/config.yaml")
                updateConfigNet("${clashPath}/run/config.yaml")
            }
            "CPFM" -> {
                mergeConfig("${clashPath}/config_new.yaml")
                SuiHelper.suCmd("mv '${clashPath}/config_new.yaml' '${clashPath}/config.yaml'")
                updateConfigNet("${clashPath}/config.yaml")
            }
        }
    }


    private fun updateConfigNet(configPath: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val conn =
                    URL("${baseURL}/configs?force=false").openConnection() as HttpURLConnection
                conn.requestMethod = "PUT"
                conn.setRequestProperty("Authorization", "Bearer $clashSecret")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                conn.outputStream.use { os ->
                    os.write(
                        JSONObject(
                            mapOf(
                                "path" to configPath
                            )
                        ).toString().toByteArray()
                    )
                }

                conn.connect()
                Log.i("NET", "HTTP CODE : ${conn.responseCode}")
                conn.inputStream.use {
                    val data = it.bufferedReader().readText()
                    Log.i("NET", data)
                }


            } catch (ex: Exception) {
                Log.w("NET", ex.toString())
            }
        }


    }

    private fun mergeConfig(outputFilePath: String) {
        setFileNR(clashPath, "config.yaml") {
            setFileNR(clashPath, "template") {
                mergeFile(
                    "${GExternalCacheDir}/config.yaml",
                    "${GExternalCacheDir}/template",
                    "${GExternalCacheDir}/config_output.yaml"
                )
                SuiHelper.suCmd("mv '${GExternalCacheDir}/config_output.yaml' '$outputFilePath'")
            }
        }
    }

    private fun getSecret() = getFromFile("${GExternalCacheDir}/template", "secret")



    private fun getExternalController(): String {

        val temp = getFromFile("${GExternalCacheDir}/template", "external-controller")

        return if (temp.startsWith(":")) {
            "127.0.0.1$temp"
        } else {
            temp
        }

    }



    private fun setFileNR(dirPath: String, fileName: String, func: () -> Unit) {
        copyFile(dirPath, fileName)
        func()
        SuiHelper.suCmd("cp '${GExternalCacheDir}/${fileName}' '${dirPath}/${fileName}'")
        deleteFile(GExternalCacheDir, fileName)
    }

    private fun copyFile(dirPath: String, fileName: String) {
        SuiHelper.suCmd("cp '${dirPath}/${fileName}' '${GExternalCacheDir}/${fileName}'")
        SuiHelper.suCmd("chmod +rw '${GExternalCacheDir}/${fileName}'")
        return
    }

    private fun deleteFile(dirPath: String, fileName: String) {
        try {
            File(dirPath, fileName).delete()
        } catch (ex: Exception) {
        }
    }

    private external fun getFromFile(path: String, node: String): String
    private external fun modifyFile(path: String, node: String, value: String)
    private external fun mergeFile(
        mainFilePath: String,
        templatePath: String,
        outputFilePath: String
    )

    private fun setTemplate() = copyFile(clashPath, "template")

    init {
        System.loadLibrary("yaml-reader")
        setTemplate()
    }

}