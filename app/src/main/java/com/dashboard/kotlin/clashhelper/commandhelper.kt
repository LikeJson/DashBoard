package com.dashboard.kotlin.clashhelper

import android.util.Log
import com.dashboard.kotlin.suihelper.suihelper
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

object commandhelper {
    fun autoUnit(byte: String): String {
        val unitList = listOf<String>("B/S", "KB/S", "MB/S", "GB/S")
        var index: Int = 0
        try {
            var double: Double = byte.toDouble()
            while (double >= 1024.00) {
                double /= 1024
                index -= -1
            }
            return "${BigDecimal(double).setScale(2, RoundingMode.HALF_UP)}${unitList[index]}"

        } catch (ex: Exception) {
            Log.w("autoUnit", ex.toString())
            return "error"
        }

    }

    fun doInstall(filePath: String, type: String, name: String = "") {
        when (type) {
            "SUB", "MMDB" -> {
                suihelper.suCmd("mv -f '$filePath' '${clashConfig.clashPath}/${name}'")
                suihelper.suCmd("chmod 700 '${clashConfig.clashPath}/${name}'")
                suihelper.suCmd("chown system:system '${clashConfig.clashPath}/${name}'")
                clashConfig.updateConfig(clashConfig.getClashType())
            }
            "DASHBOARD" -> {
                suihelper.suCmd("unzip -o '$filePath' -d '${clashConfig.clashPath}'")
                suihelper.suCmd("chmod 000 '${clashConfig.clashPath}/${name}/' -R")
                clashConfig.clashDashBoard = name
            }
        }
        File(filePath).delete()
    }

}