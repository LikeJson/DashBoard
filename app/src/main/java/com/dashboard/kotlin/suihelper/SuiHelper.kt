package com.dashboard.kotlin.suihelper

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import rikka.sui.Sui
import java.io.DataInputStream
import java.io.DataOutputStream

object SuiHelper {
    fun init(packageName: String) {
        Sui.init(packageName)
    }

    fun checkPermission(request: Boolean = true): Boolean {
        try {
            return if (Shizuku.getVersion() >= 11) {
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    true
                } else {
                    if (request) {
                        Shizuku.requestPermission(114514)
                    }
                    return checkPermission()
                }
            } else {
                return checkPermission()
            }
        } catch (ex: Exception) {
            Log.e("Permission", "Shizuku not found")
            return checkPermission()
        }
    }

    private fun checkPermission(): Boolean{
        var process: Process? = null
        var os: DataOutputStream? = null
        try {
            process = Runtime.getRuntime().exec("su") //切换到root帐号
            os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
        }catch (e: Exception){
            Log.e("Permission", "Exception: $e", )
        }finally {
            val res = process?.waitFor() == 0
            kotlin.runCatching {
                os?.close()
                process?.destroy()
            }
            return res
        }
    }


    fun suCmd(cmd: String): String {
        var process: Process? = null
        var os: DataOutputStream? = null
        var ls: DataInputStream? = null
        var result = ""
        try {
            //Log.i("suCmd", "suCmd: $cmd")

            //process = Runtime.getRuntime().exec("su")
            //process = Shizuku.newProcess(arrayOf("sh"), null, null)
            process = try {
                Shizuku.newProcess(arrayOf("sh"), null, null)
            }catch (e: Exception){
                Runtime.getRuntime().exec("su")
            }
            os = DataOutputStream(process?.outputStream)
            ls = DataInputStream(process?.inputStream)
            os.writeBytes("$cmd\n")
            os.writeBytes("exit\n")
            os.flush()
            result = ls.bufferedReader().readText()

            process?.waitFor()
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
        //Log.d("suCmd", ".\n~>\n$cmd\n\nres:\n$result")
        return runCatching {
            if (result.last() == '\n') result.dropLast(1) else result
        }.getOrDefault("")
    }
}