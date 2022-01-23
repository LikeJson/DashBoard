package com.dashboard.kotlin

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dashboard.kotlin.clashhelper.ClashConfig
import com.dashboard.kotlin.clashhelper.ClashStatus
import com.dashboard.kotlin.clashhelper.commandhelper
import com.dashboard.kotlin.suihelper.suihelper
import kotlinx.android.synthetic.main.fragment_main_page.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.util.*


class MainPage : Fragment() {

    lateinit var clashV: String
    val handler = Handler(Looper.getMainLooper())
    lateinit var timer: Timer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_page, container, false)
    }

    private val clashStatusClass = ClashStatus()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ViewCreated", "MainPageViewCreated")

        toolbar.title = getString(R.string.app_name)
        //TODO 添加 app 图标

        if (!suihelper.checkPermission()) {
            clash_status.setCardBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.error, context?.theme)
            )
            clash_status_icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_service_not_running,
                    context?.theme
                )
            )
            clash_status_text.text = getString(R.string.sui_disable)
            netspeed_status_text.visibility = View.GONE

            GlobalScope.async {
                while (true) {
                    if (suihelper.checkPermission(request = false)) {
                        restartApp()
                        break
                    }
                    delay(1 * 1000)
                }
            }

        } else {
            if (ClashStatus().runStatus()) {
                setStatusRunning()
            } else {
                setStatusStopped()
            }
        }

        clash_status.setOnClickListener {
            //setStatusCmdRunning()

            GlobalScope.async {
                doAssestsShellFile(
                    "CFM_" +
                            (if (ClashStatus().runStatus()) {
                                "Stop"
                            } else {
                                "Start"
                            }) +
                            ".sh", !ClashStatus().runStatus()
                )
                //restartApp()
                true
            }
        }

        menu_ip_check.setOnClickListener {
            it.findNavController().navigate(R.id.action_mainPage_to_ipCheckPage)
        }

        menu_web_dashboard.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("URL", "http://127.0.0.1:9090/ui/" +
                    if ((context?.resources?.configuration?.uiMode
                            ?.and(Configuration.UI_MODE_NIGHT_MASK)) == Configuration.UI_MODE_NIGHT_YES) {
                        "?theme=dark"
                    }else{
                        "?theme=light"
                    })
            it.findNavController().navigate(R.id.action_mainPage_to_webViewPage, bundle)
        }

        menu_speed_test.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("URL", "https://fast.com/zh/cn/")
            it.findNavController().navigate(R.id.action_mainPage_to_webViewPage, bundle)
        }

        menu_setting.setOnClickListener {
            it.findNavController().navigate(R.id.action_manPage_to_settingPage)
        }

        //这是一段屎一样的代码
        clashV = suihelper.suCmd("${ClashConfig.corePath} -v")
        cmd_result.text = "$clashV${suihelper.suCmd("cat ${ClashConfig.clashPath}/run/run.logs 2> /dev/null")}"
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val log = suihelper.suCmd("cat ${ClashConfig.clashPath}/run/run.logs 2> /dev/null")
                val cmdRunning = suihelper.suCmd("cat ${ClashConfig.clashPath}/run/cmdRunning 2>&1")
                handler.post{

                    cmd_result.let {
                        if (clash_status_text?.text == getString(R.string.clash_charging))
                            it.text = "$clashV$log"

                        kotlin.runCatching {
                            when {
                                cmdRunning == "" -> {
                                    setStatusCmdRunning()
                                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                                }
                                ClashStatus().runStatus() ->
                                    setStatusRunning()
                                else ->
                                    setStatusStopped()
                            }
                        }
                    }

                }
            }
        },0, 300)

    }

    override fun onDestroyView() {
        clashStatusClass.stopGetTraffic()
        timer.cancel()
        Log.d("DestroyView", "MainPageDestroyView")
        super.onDestroyView()
    }


    private fun restartApp() {
        val intent: Intent? = activity?.baseContext?.packageManager
            ?.getLaunchIntentForPackage(activity?.baseContext!!.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent?.putExtra("REBOOT", "reboot")
        startActivity(intent)
    }

    private fun doAssestsShellFile(fileName: String, isStart: Boolean = false) {
        context?.assets?.open(fileName)?.let { op ->
            File(context?.externalCacheDir, fileName).let { fo ->
                fo.outputStream().let { ip ->
                    op.copyTo(ip)
                }

                if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.R) and (ClashConfig.scriptsPath == "/data/adb/modules/Clash_For_Magisk/scripts")) {
                    suihelper.suCmd("sh '${context?.externalCacheDir}/${fileName}' ${ClashConfig.scriptsPath}")
                }
                else {
                    suihelper.suCmd("sh '${context?.externalCacheDir}/${fileName}'")
                }

                fo.delete()
            }
        }
    }

    private fun setStatusRunning(){
        if (clash_status_text.text == getString(R.string.clash_enable))
            return
        clash_status.isClickable = true
        clash_status.setCardBackgroundColor(
            ResourcesCompat.getColor(resources, R.color.colorPrimary, context?.theme)
        )
        clash_status_icon.setImageDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_activited, context?.theme)
        )
        clash_status_text.text = getString(R.string.clash_enable)

        netspeed_status_text.visibility = View.VISIBLE

        clashStatusClass.getTraffic()

        GlobalScope.launch(Dispatchers.IO) {
            while (clashStatusClass.trafficThreadFlag) {
                try {
                    val jsonObject = JSONObject(clashStatusClass.trafficRawText)
                    val upText: String = commandhelper.autoUnit(jsonObject.optString("up"))
                    val downText: String =
                        commandhelper.autoUnit(jsonObject.optString("down"))

                    withContext(Dispatchers.Main) {
                        netspeed_status_text.text =
                            getString(R.string.netspeed_status_text).format(
                                upText,
                                downText
                            )
                    }
                } catch (ex: Exception) {
                    Log.w("trafficText", ex.toString())
                }
                delay(600)
            }
        }
    }

    private fun setStatusCmdRunning(){
        if (clash_status_text.text == getString(R.string.clash_charging))
            return
        clash_status.isClickable = false
        clash_status.setCardBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.colorPrimary,
                context?.theme
            )
        )
        clash_status_icon.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_refresh,
                context?.theme
            )
        )
        clash_status_text.text = getString(R.string.clash_charging)
        netspeed_status_text.visibility = View.GONE
        clashStatusClass.stopGetTraffic()
    }

    private fun setStatusStopped(){
        if (clash_status_text.text == getString(R.string.clash_disable))
            return
        clash_status.isClickable = true
        clash_status.setCardBackgroundColor(
            ResourcesCompat.getColor(resources, R.color.gray, context?.theme)
        )
        clash_status_icon.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.ic_service_not_running,
                context?.theme
            )
        )
        clash_status_text.text = getString(R.string.clash_disable)
        netspeed_status_text.visibility = View.GONE
        clashStatusClass.stopGetTraffic()
    }
}