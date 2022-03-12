package com.dashboard.kotlin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.dashboard.kotlin.clashhelper.ClashConfig
import com.dashboard.kotlin.clashhelper.CommandHelper
import com.dashboard.kotlin.suihelper.SuiHelper
import kotlinx.android.synthetic.main.fragment_log.*
import kotlinx.coroutines.*

@DelicateCoroutinesApi
class LogPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        GlobalScope.async {
            val clashV = SuiHelper.suCmd("${ClashConfig.corePath} -v")
            var log = SuiHelper.suCmd("cat ${ClashConfig.logPath} 2> /dev/null")
            withContext(Dispatchers.Main){
                log_cat.text = "$clashV\n$log"
            }
            while (true){
                if (CommandHelper.isCmdRunning()) {
                    log = SuiHelper.suCmd("cat ${ClashConfig.logPath} 2> /dev/null")
                    withContext(Dispatchers.Main) {
                        log_cat.text = "$clashV\n$log"
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                }
                delay(600)
            }
        }
    }
}