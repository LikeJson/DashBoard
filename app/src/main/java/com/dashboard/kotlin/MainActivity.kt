package com.dashboard.kotlin

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.dashboard.kotlin.suihelper.suihelper
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.toolbar.*


lateinit var GExternalCacheDir: String
lateinit var KV: MMKV

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        this.window.statusBarColor = resources.getColor(android.R.color.transparent)
        this.window.navigationBarColor = resources.getColor(android.R.color.transparent)

        //sui
        suihelper().init(packageName)

        //verbal
        GExternalCacheDir = applicationContext.externalCacheDir.toString()
        MMKV.initialize(this)
        KV = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null)
    }
}