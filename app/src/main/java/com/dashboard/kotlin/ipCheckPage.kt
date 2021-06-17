package com.dashboard.kotlin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_ip_check_page.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class ipCheckPage : Fragment() {

    private lateinit var coroutineScope: Job
    private lateinit var sukkAPiThreadContext: ExecutorCoroutineDispatcher
    private lateinit var ipipNetThreadContext: ExecutorCoroutineDispatcher
    private lateinit var ipSbApiThreadContext: ExecutorCoroutineDispatcher
    private lateinit var sukkaGlobalThreadContext: ExecutorCoroutineDispatcher

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ip_check_page, container, false)
    }


    @ObsoleteCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sukkAPiThreadContext = newSingleThreadContext("sukkAPiThread")
        ipipNetThreadContext = newSingleThreadContext("ipipNetThread")
        ipSbApiThreadContext = newSingleThreadContext("ipSbApiThread")
        sukkaGlobalThreadContext = newSingleThreadContext("sukkaGlobalThread")


        coroutineScope = GlobalScope.launch(Dispatchers.IO) {
            async(sukkAPiThreadContext) {
                var tempStr: String
                try {
                    val sukkaApiObj =
                        JSONObject(URL("https://forge.speedtest.cn/api/location/info").readText())
                    tempStr = "${sukkaApiObj.optString("full_ip")}\n" +
                            "${sukkaApiObj.optString("country")} " +
                            "${sukkaApiObj.optString("province")} " +
                            "${sukkaApiObj.optString("city")} " +
                            "${sukkaApiObj.optString("distinct")} " +
                            "${sukkaApiObj.optString("isp")}"
                } catch (ex: Exception) {
                    tempStr = "error"
                }

                withContext(Dispatchers.Main) {
                    try {
                        sukka_api_result.text = tempStr
                    } catch (ex: Exception) {

                    }
                }

            }


            async(ipipNetThreadContext) {
                //IPIP.NET
                var tempStr: String
                try {
                    var ipipNetText = URL("https://myip.ipip.net").readText()
                    ipipNetText = ipipNetText.replace("当前 IP：", "")
                    ipipNetText = ipipNetText.replace("来自于：", "\n")
                    ipipNetText = ipipNetText.substring(0, ipipNetText.length - 1)
                    tempStr = ipipNetText
                } catch (ex: Exception) {
                    tempStr = "error"
                }
                withContext(Dispatchers.Main) {
                    try {
                        ipip_net_result.text = tempStr
                    } catch (ex: Exception) {

                    }
                }

            }


            //IP.SB Api
            async(ipSbApiThreadContext) {   // IP.SB API
                var tempStr: String
                try {
                    val ipsbObj = JSONObject(URL("https://api.ip.sb/geoip").readText())
                    tempStr = "${ipsbObj.optString("ip")}\n" +
                            "${ipsbObj.optString("country")} " +
                            "${ipsbObj.optString("organization")} " +
                            "${ipsbObj.optString("asn")} " +
                            "${ipsbObj.optString("asn_organization")} "
                } catch (ex: Exception) {
                    tempStr = "error"
                }
                withContext(Dispatchers.Main) {
                    try {
                        ip_sb_result.text = tempStr
                    } catch (ex: Exception) {

                    }
                }
            }


            //Sukka Global Api
            async(sukkaGlobalThreadContext) {
                var tempStr: String
                try {
                    var ipSkkRip = URL("https://ip.skk.moe/cdn-cgi/trace").readText()
                    ipSkkRip = ipSkkRip.replaceBefore("ip=", "")
                    ipSkkRip = ipSkkRip.replaceAfter("ts=", "")
                    ipSkkRip = ipSkkRip.replace("\nts=", "")
                    ipSkkRip = ipSkkRip.replace("ip=", "")

                    val conn = URL("https://qqwry.api.skk.moe/${ipSkkRip}").openConnection()
                    conn.setRequestProperty("referer", "https://ip.skk.moe")
                    conn.setRequestProperty(
                        "user-agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 " +
                                "(KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36"
                    )
                    val ipSkkGeoIpObj = JSONObject(conn.getInputStream().reader().readText())

                    tempStr = "${ipSkkRip}\n" +
                            "${ipSkkGeoIpObj.optString("geo")}"

                } catch (ex: Exception) {
                    tempStr = "error"
                }
                withContext(Dispatchers.Main) {
                    try {
                        sukka_api_global_result.text = tempStr
                    } catch (ex: Exception) {

                    }
                }
            }
        }
        super.onViewCreated(view, savedInstanceState)
        Log.d("ViewCreated", "ipCheckPageViewCreated")
    }

    override fun onDestroyView() {
        try {
            sukkAPiThreadContext.close()
            ipipNetThreadContext.close()
            ipSbApiThreadContext.close()
            sukkaGlobalThreadContext.close()

            coroutineScope.cancel()

            sukka_api_result.text = ""
            ipip_net_result.text = ""
            ip_sb_result.text = ""
            sukka_api_global_result.text = ""
        } finally {
            Log.d("ViewDestroy", "ipCheckPageDestroyView")
        }
        super.onDestroyView()
    }

}