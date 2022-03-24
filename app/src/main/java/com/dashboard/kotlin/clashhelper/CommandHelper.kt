package com.dashboard.kotlin.clashhelper

import android.util.Log
import com.dashboard.kotlin.suihelper.SuiHelper
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

object CommandHelper {
    fun autoUnitForSpeed(byte: String): String {
        val units = listOf("B/S", "KB/S", "MB/S", "GB/S")
        var index = 0
        return try {
            var double: Double = byte.toDouble()
            while (double >= 1024.00) {
                double /= 1024
                index -= -1
            }
            "${BigDecimal(double).setScale(2, RoundingMode.HALF_UP)}${units[index]}"

        } catch (ex: Exception) {
            Log.w("autoUnit", ex.toString())
            "error"
        }

    }

    fun autoUnitForSize(kbyte: String): String{
        val units = listOf("KB", "MB", "GB", "TB")

        var num =
            runCatching { kbyte.toDouble() }
            .onFailure { return "error" }
            .getOrDefault(0.00)

        for (unit in units){
            if (num >= 1024){
                num /= 1024
            }else{
                return "${BigDecimal(num).setScale(2, RoundingMode.HALF_UP)}$unit"
            }
        }
        return "error"
    }

    fun isCmdRunning() = SuiHelper.suCmd(
        "if [ -f ${ClashConfig.dataPath}/run/cmdRunning ];then\necho 'true'\nelse\necho 'false'\nfi") == "true"

    fun doInstall(filePath: String, type: String, name: String = "") {
        when (type) {
            "SUB", "MMDB" -> {
                SuiHelper.suCmd("mv -f '$filePath' '${ClashConfig.dataPath}/${name}'")
                SuiHelper.suCmd("chmod 700 '${ClashConfig.dataPath}/${name}'")
                SuiHelper.suCmd("chown system:system '${ClashConfig.dataPath}/${name}'")
                ClashConfig.updateConfig{}
            }
            "DASHBOARD" -> {
                SuiHelper.suCmd("unzip -o '$filePath' -d '${ClashConfig.dataPath}'")
                SuiHelper.suCmd("chmod 000 '${ClashConfig.dataPath}/${name}/' -R")
                ClashConfig.dashBoard = name
            }
        }
        File(filePath).delete()
    }

}