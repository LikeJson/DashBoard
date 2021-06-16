package com.dashboard.kotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dashboard.kotlin.clashhelper.clashStatus
import com.dashboard.kotlin.clashhelper.commandhelper
import kotlinx.android.synthetic.main.fragment_main_page.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.*
import org.json.JSONObject


class MainPage : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_page, container, false)
    }


    private val clashStatusClass = clashStatus()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (clashStatus().runStatus()){
            clash_status.setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
            clash_status_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_activited))
            clash_status_text.text = "Clash 运行正常"

            netspeed_status_text.visibility = View.VISIBLE


            clashStatusClass.getTraffic()

            GlobalScope.launch(Dispatchers.IO) {
                while (clashStatusClass.trafficThreadFlag){
                        try {
                            val jsonObject = JSONObject(clashStatusClass.trafficRawText)
                            val upText: String = commandhelper().autoUnit(jsonObject.optString("up"))
                            val downText: String = commandhelper().autoUnit(jsonObject.optString("down"))

                            withContext(Dispatchers.Main){
                                netspeed_status_text.text = "上传 ${upText} 下载 ${downText}"
                            }
                        }catch(ex: Exception){Log.w("trafficText",ex.toString())}
                delay(1000)
                }
            }



        }else{
            clash_status.setCardBackgroundColor(resources.getColor(R.color.error))
            clash_status_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_service_not_running))
            clash_status_text.text = "Clash 运行异常"
            netspeed_status_text.visibility = View.GONE

        }





        toolbar.title = getString(R.string.app_name)

        menu_ip_check.setOnClickListener {

            val navController = it.findNavController()
//            val bundle = Bundle()
//            bundle.putString("URL","https://ip.skk.moe/")
            navController.navigate(R.id.ipCheckPage)

        }


        menu_web_dashboard.setOnClickListener {


            val navController = it.findNavController()
            val bundle = Bundle()
            bundle.putString("URL","http://127.0.0.1:9090/ui")
            navController.navigate(R.id.webViewPage,bundle)
        }

    }


    override fun onDestroyView() {
        clashStatusClass.stopGetTraffic()
        Log.d("Destroy","MainPageDestroy")
        super.onDestroyView()
    }
}