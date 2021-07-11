package com.dashboard.kotlin

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.dashboard.kotlin.suihelper.suihelper
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File


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
            suihelper.suCmd("su -c logcat | grep \$(su -c ps -A | grep  com.dashboard.kotlin | awk '{print \$2}') > \"${externalCacheDir}/log\$(date +\"%Y-%m-%d_%H-%M-%S\").txt\" &")
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
}