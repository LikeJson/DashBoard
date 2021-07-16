package com.dashboard.kotlin

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.dashboard.kotlin.suihelper.suihelper
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.toolbar.*
import java.io.DataInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


lateinit var GExternalCacheDir: String
lateinit var KV: MMKV

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        this.window.statusBarColor = ResourcesCompat.getColor(
            resources,
            android.R.color.transparent,
            applicationContext?.theme
        )
        this.window.navigationBarColor = ResourcesCompat.getColor(
            resources,
            android.R.color.transparent,
            applicationContext?.theme
        )

        //sui
        suihelper.init(packageName)

        //debug version print logs
        if (BuildConfig.DEBUG) {
            thread { saveLogs() }
        } else {
            File(externalCacheDir.toString()).walk()
                .maxDepth(1)
                .filter { it.isFile }
                .filter { it.name.startsWith("log") }
                .filter { it.extension == "txt" }
                .forEach { it.delete() }
        }
        //verbal
        GExternalCacheDir = applicationContext.externalCacheDir.toString()
        MMKV.initialize(this)
        KV = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
    }


    fun saveLogs() {
        val cmd = "logcat -v time"
        var process: Process? = null
        var ls: DataInputStream? = null
        try {
            Log.i("LogCat", "Start")
            process = Runtime.getRuntime().exec(cmd)
            ls = DataInputStream(process.inputStream)
            File(
                externalCacheDir, "log_${
                    SimpleDateFormat(
                        "yyyy-MM-dd_HH-mm-ss", Locale.getDefault(
                            Locale.Category.FORMAT
                        )
                    ).format(Date())
                }.txt"
            ).outputStream().use {
                ls.copyTo(it)
            }

            process.waitFor()
        } catch (e: Exception) {
            Log.e("LogCat", "Exception: $e")
        } finally {
            try {
                ls?.close()
                process?.destroy()
            } catch (e: Exception) {
                Log.e("LogCat", "close stream exception: $e")
            }
            Log.i("LogCat", "End")
        }
    }
}