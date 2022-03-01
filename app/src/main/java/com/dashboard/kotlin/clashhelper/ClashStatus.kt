package com.dashboard.kotlin.clashhelper

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import com.dashboard.kotlin.suihelper.SuiHelper
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.concurrent.thread

class ClashStatus {
    var statusThreadFlag: Boolean = true
    var statusRawText: String = "{\"up\":\"0\",\"down\":\"0\",\"RES\":\"0\",\"CPU\":\"0%\"}"

    fun runStatus(): Boolean {
        var isRunning = false
        thread(start = true) {
            isRunning = try {
                val conn =
                    URL(ClashConfig.baseURL).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer ${ClashConfig.secret}")
                conn.inputStream.bufferedReader().readText() == "{\"hello\":\"clash\"}\n"
            } catch (ex: Exception) {
                false
            }
        }.join()
        return isRunning
    }

    fun getStatus() {
        statusThreadFlag = true
        val secret = ClashConfig.secret
        val baseURL = ClashConfig.baseURL
        Thread {
            try {
                val conn =
                    URL("${baseURL}/traffic").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", "Bearer $secret")

                conn.inputStream.use {
                    var lastCpuTotal = 0L
                    var lastClashCpuTotal = 0L

                    while (statusThreadFlag) {
                        var cpuTotal = 0L
                        var clashCpuTotal = 0L
                        SuiHelper.suCmd("cat /proc/stat | grep \"cpu \"")
                            .replace("\n","")
                            .replace("cpu ","")
                            .split(Regex(" +")).forEach{ str ->
                                runCatching {
                                    cpuTotal += str.toLong()
                                }
                            }
                        SuiHelper.suCmd(
                            "cat /proc/`cat ${ClashConfig.pidPath}`/stat")
                            .split(Regex(" +"))
                            .filterIndexed { index, _ -> index in 13..16 }
                            .forEach{ str ->
                                runCatching {
                                    clashCpuTotal += str.toLong()
                                }
                            }
                        val cpuAVG = BigDecimal(
                            runCatching {
                                ((clashCpuTotal - lastClashCpuTotal) /
                                        (cpuTotal - lastCpuTotal).toDouble() *100)
                            }.getOrDefault(0) as Double
                        ).setScale(2, RoundingMode.HALF_UP)

                        lastClashCpuTotal = clashCpuTotal
                        lastCpuTotal = cpuTotal

                        val res = SuiHelper.suCmd(
                            "cat /proc/`cat ${ClashConfig.pidPath}`/status | " +
                                    "grep VmRSS | " +
                                    "awk '{print \$2}'")

                        statusRawText = it.bufferedReader().readLine()
                            .replace("}", ",\"RES\":\"$res\",\"CPU\":\"$cpuAVG%\"}")

                        Thread.sleep(600)
                    }
                }
            } catch (ex: Exception) {
                Log.d("TRAFFIC-W", ex.toString())
            }
        }.start()
    }

    fun stopGetStatus() {
        statusThreadFlag = false
    }
}