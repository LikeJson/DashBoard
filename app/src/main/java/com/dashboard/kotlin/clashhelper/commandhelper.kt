package com.dashboard.kotlin.clashhelper

import android.util.Log
import com.dashboard.kotlin.suihelper.SuiHelper
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
                SuiHelper.suCmd("mv -f '$filePath' '${ClashConfig.clashPath}/${name}'")
                SuiHelper.suCmd("chmod 700 '${ClashConfig.clashPath}/${name}'")
                SuiHelper.suCmd("chown system:system '${ClashConfig.clashPath}/${name}'")
                ClashConfig.updateConfig("CFM")
            }
            "DASHBOARD" -> {
                SuiHelper.suCmd("unzip -o '$filePath' -d '${ClashConfig.clashPath}'")
                SuiHelper.suCmd("chmod 000 '${ClashConfig.clashPath}/${name}/' -R")
                ClashConfig.clashDashBoard = name
            }
        }
        File(filePath).delete()
    }

}