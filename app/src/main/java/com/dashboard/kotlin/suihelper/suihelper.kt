package com.dashboard.kotlin.suihelper

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import rikka.sui.Sui
import java.io.DataInputStream
import java.io.DataOutputStream

object suihelper {
    fun init(packageName: String) {
        Sui.init(packageName)
    }

    fun checkPermission(request: Boolean = true): Boolean {
        try {
            if (Shizuku.getVersion() >= 11) {
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    return true
                } else {
                    if (request) {
                        Shizuku.requestPermission(114514)
                    }
                    return false
                }
            } else {
                return false
            }
        } catch (ex: Exception) {
            return false
        }


    }


    fun suCmd(cmd: String): String {
        var process: Process? = null
        var os: DataOutputStream? = null
        var ls: DataInputStream? = null
        var result: String = ""
        try {
            Log.i("suCmd", "suCmd: $cmd")

//            process = Runtime.getRuntime().exec("su")
            process = Shizuku.newProcess(arrayOf("sh"), null, null)
            os = DataOutputStream(process.outputStream)
            ls = DataInputStream(process.inputStream)
            os.writeBytes("$cmd 2>&1 \n")
            os.writeBytes("exit\n")
            os.flush()
            result = ls.bufferedReader().readText()

            process.waitFor()
        } catch (e: Exception) {
            Log.e("suCmd", "Exception: $e")
        } finally {
            try {
                os?.close()
                ls?.close()
                process?.destroy()
            } catch (e: Exception) {
                Log.e("suCmd", "close stream exception: $e")
            }
        }
        Log.d("suCmd", "result: $result")
        return result
    }
}