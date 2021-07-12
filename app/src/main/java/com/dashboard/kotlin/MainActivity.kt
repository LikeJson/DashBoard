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

        //verbal
        GExternalCacheDir = applicationContext.externalCacheDir.toString()
        MMKV.initialize(this)
        KV = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
    }
}