package com.dashboard.kotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ScrollView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dashboard.kotlin.clashhelper.ClashConfig
import com.dashboard.kotlin.clashhelper.ClashStatus
import com.dashboard.kotlin.clashhelper.CommandHelper
import com.dashboard.kotlin.suihelper.SuiHelper
import kotlinx.android.synthetic.main.fragment_main_page.*
import kotlinx.android.synthetic.main.fragment_main_page_buttons.*
import kotlinx.android.synthetic.main.fragment_main_page_log.*
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ViewCreated", "MainPageViewCreated")

        mToolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.menu_restart -> {
                    if ((clash_status_text.text == getText(R.string.clash_charging))
                        || (clash_status_text.text == getText(R.string.sui_disable)))
                        return@setOnMenuItemClickListener true

                    setStatusCmdRunning()
                    GlobalScope.async {
                        doAssestsShellFile("CFM_Update_GeoX.sh")
                    }
                }
                else -> return@setOnMenuItemClickListener false
            }
            true
        }

        //TODO 添加 app 图标
        mToolbar.title = getString(R.string.app_name) +
                "-V" +
                BuildConfig.VERSION_NAME.replace(Regex(".r.+$"),"")

        if (!SuiHelper.checkPermission()) {
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
                    if (SuiHelper.checkPermission(request = false)) {
                        restartApp()
                        break
                    }
                    delay(1 * 1000)
                }
            }

        } else {

            //这是一段屎一样的代码
            clashV = SuiHelper.suCmd("${ClashConfig.corePath} -v")
            cmd_result.text = "$clashV${SuiHelper.suCmd("cat ${ClashConfig.clashPath}/run/run.logs 2> /dev/null")}"
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    //val log = SuiHelper.suCmd("cat ${ClashConfig.clashPath}/run/run.logs 2> /dev/null")
                    val cmdRunning = SuiHelper.suCmd(
                        "if [ -f ${ClashConfig.clashPath}/run/cmdRunning ];then\necho 'true'\nelse\necho 'false'\nfi")
                    //"cat ${ClashConfig.clashPath}/run/cmdRunning 2>&1")
                    handler.post{

                        cmd_result.let {
                            runCatching {
                                if (clash_status_text?.text == getString(R.string.clash_charging))
                                    it.text = clashV + SuiHelper.suCmd("cat ${ClashConfig.clashPath}/run/run.logs 2> /dev/null")
                                when {
                                    cmdRunning == "true\n" -> {
                                        setStatusCmdRunning()
                                    }
                                    ClashStatus().runStatus() -> {
                                        setStatusRunning()
                                    }
                                    else ->
                                        setStatusStopped()
                                }
                            }
                        }

                    }
                }
            },0, 300)
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
    }

    override fun onDestroyView() {
        clashStatusClass.stopGetStatus()
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
                    SuiHelper.suCmd("sh '${context?.externalCacheDir}/${fileName}' true ${ClashConfig.scriptsPath} 2>&1")
                }
                else {
                    SuiHelper.suCmd("sh '${context?.externalCacheDir}/${fileName}' false ${ClashConfig.scriptsPath} 2>&1")
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

        clashStatusClass.getStatus()

        scrollView.fullScroll(ScrollView.FOCUS_DOWN)

        GlobalScope.launch(Dispatchers.IO) {
            while (clashStatusClass.statusThreadFlag) {
                try {
                    val jsonObject = JSONObject(clashStatusClass.statusRawText)
                    val upText: String = CommandHelper.autoUnitForSpeed(jsonObject.optString("up"))
                    val downText: String =
                        CommandHelper.autoUnitForSpeed(jsonObject.optString("down"))
                    val res = CommandHelper.autoUnitForSize(jsonObject.optString("RES"))
                    val cpu = jsonObject.optString("CPU")
                    withContext(Dispatchers.Main) {
                        netspeed_status_text.text =
                            getString(R.string.netspeed_status_text).format(
                                upText,
                                downText,
                                res,
                                cpu
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
        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        if (clash_status_text.text == getString(R.string.clash_charging))
            return
        clash_status.isClickable = false
        clash_status.setCardBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.gray,
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
        netspeed_status_text.visibility = View.INVISIBLE
        clashStatusClass.stopGetStatus()
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
        netspeed_status_text.visibility = View.INVISIBLE
        clashStatusClass.stopGetStatus()
    }
}