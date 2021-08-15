package com.dashboard.kotlin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import com.dashboard.kotlin.databinding.FragmentIpCheckPageBinding
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class ipCheckPage : Fragment() {

    private lateinit var coroutineScope: Job
    private lateinit var sukkAPiThreadContext: ExecutorCoroutineDispatcher
    private lateinit var ipipNetThreadContext: ExecutorCoroutineDispatcher
    private lateinit var ipSbApiThreadContext: ExecutorCoroutineDispatcher
    private lateinit var sukkaGlobalThreadContext: ExecutorCoroutineDispatcher
    private lateinit var baiduCheckThreadContext: ExecutorCoroutineDispatcher
    private lateinit var netEaseCheckThreadContext: ExecutorCoroutineDispatcher
    private lateinit var githubCheckThreadContext: ExecutorCoroutineDispatcher
    private lateinit var youtubeCheckThreadContext: ExecutorCoroutineDispatcher

    private var _binding: FragmentIpCheckPageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIpCheckPageBinding.inflate(inflater, container, false)
        return binding.root
    }


    @ObsoleteCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sukkAPiThreadContext = newSingleThreadContext("sukkAPiThread")
        ipipNetThreadContext = newSingleThreadContext("ipipNetThread")
        ipSbApiThreadContext = newSingleThreadContext("ipSbApiThread")
        sukkaGlobalThreadContext = newSingleThreadContext("sukkaGlobalThread")
        baiduCheckThreadContext = newSingleThreadContext("baiduCheckThread")
        netEaseCheckThreadContext = newSingleThreadContext("netEaseCheckThread")
        githubCheckThreadContext = newSingleThreadContext("githubCheckThread")
        youtubeCheckThreadContext = newSingleThreadContext("youtubeCheckThread")


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
                        binding.sukkaApiResult.text = tempStr
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
                        binding.ipipNetResult.text = tempStr
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
                        binding.ipSbResult.text = tempStr
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
                        binding.sukkaApiGlobalResult.text = tempStr
                    } catch (ex: Exception) {

                    }
                }
            }


            async(baiduCheckThreadContext) {
                var tempStr: String
                try {
                    val conn = URL("https://baidu.com/").openConnection()
                    conn.connectTimeout = 1000 * 10
                    conn.readTimeout = 1000 * 10
                    if (conn.getInputStream().reader().readText() != "") tempStr =
                        "连接正常" else tempStr = "无法访问"
                } catch (ex: Exception) {
                    Log.d("ConnError", ex.toString())
                    tempStr = "无法访问"
                }
                withContext(Dispatchers.Main) {
                    try {
                        val color: Int
                        if (tempStr == "连接正常") color = ResourcesCompat.getColor(
                            resources,
                            R.color.green,
                            context?.theme
                        ) else color =
                            ResourcesCompat.getColor(resources, R.color.orange, context?.theme)
                        binding.baiduCheck.text = tempStr
                        binding.baiduCheck.setTextColor(color)
                    } catch (ex: Exception) {

                    }
                }

            }
            async(netEaseCheckThreadContext) {
                var tempStr: String
                try {
                    val conn = URL("https://music.163.com/").openConnection()
                    conn.connectTimeout = 1000 * 10
                    conn.readTimeout = 1000 * 10
                    if (conn.getInputStream().reader().readText() != "") tempStr =
                        "连接正常" else tempStr = "无法访问"
                } catch (ex: Exception) {
                    Log.d("ConnError", ex.toString())
                    tempStr = "无法访问"
                }
                withContext(Dispatchers.Main) {
                    try {
                        val color: Int
                        if (tempStr == "连接正常") color = ResourcesCompat.getColor(
                            resources,
                            R.color.green,
                            context?.theme
                        ) else color =
                            ResourcesCompat.getColor(resources, R.color.orange, context?.theme)
                        binding.neteaseMusicCheck.text = tempStr
                        binding.neteaseMusicCheck.setTextColor(color)
                    } catch (ex: Exception) {

                    }
                }

            }
            async(githubCheckThreadContext) {
                var tempStr: String
                try {
                    val conn = URL("https://github.com/").openConnection()
                    conn.connectTimeout = 1000 * 10
                    conn.readTimeout = 1000 * 10
                    if (conn.getInputStream().reader().readText() != "") tempStr =
                        "连接正常" else tempStr = "无法访问"
                } catch (ex: Exception) {
                    Log.d("ConnError", ex.toString())
                    tempStr = "无法访问"
                }
                withContext(Dispatchers.Main) {
                    try {
                        val color: Int
                        if (tempStr == "连接正常") color = ResourcesCompat.getColor(
                            resources,
                            R.color.green,
                            context?.theme
                        ) else color =
                            ResourcesCompat.getColor(resources, R.color.orange, context?.theme)
                        binding.githubCheck.text = tempStr
                        binding.githubCheck.setTextColor(color)
                    } catch (ex: Exception) {

                    }
                }

            }
            async(youtubeCheckThreadContext) {
                var tempStr: String
                try {
                    val conn = URL("https://www.youtube.com/").openConnection()
                    conn.connectTimeout = 1000 * 10
                    conn.readTimeout = 1000 * 10
                    if (conn.getInputStream().reader().readText() != "") tempStr =
                        "连接正常" else tempStr = "无法访问"
                } catch (ex: Exception) {
                    Log.d("ConnError", ex.toString())
                    tempStr = "无法访问"
                }
                withContext(Dispatchers.Main) {
                    try {
                        val color: Int
                        if (tempStr == "连接正常") color = ResourcesCompat.getColor(
                            resources,
                            R.color.green,
                            context?.theme
                        ) else color =
                            ResourcesCompat.getColor(resources, R.color.orange, context?.theme)
                        binding.youtubeCheck.text = tempStr
                        binding.youtubeCheck.setTextColor(color)
                    } catch (ex: Exception) {

                    }
                }

            }


        }
        super.onViewCreated(view, savedInstanceState)
        Log.d("ViewCreated", "ipCheckPageViewCreated")

        binding.toolbar.toolbarIn.navigationIcon = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_back,
            context?.theme
        )
        binding.toolbar.toolbarIn.setNavigationOnClickListener {
            val controller = it.findNavController()
            controller.popBackStack()
        }


    }

    override fun onDestroyView() {
        try {
            sukkAPiThreadContext.close()
            ipipNetThreadContext.close()
            ipSbApiThreadContext.close()
            sukkaGlobalThreadContext.close()
            baiduCheckThreadContext.close()
            netEaseCheckThreadContext.close()
            githubCheckThreadContext.close()
            youtubeCheckThreadContext.close()


            coroutineScope.cancel()

            binding.sukkaApiResult.text = ""
            binding.ipipNetResult.text = ""
            binding.ipSbResult.text = ""
            binding.sukkaApiGlobalResult.text = ""
        } finally {
            Log.d("ViewDestroy", "ipCheckPageDestroyView")
        }
        _binding = null
        super.onDestroyView()

    }

}