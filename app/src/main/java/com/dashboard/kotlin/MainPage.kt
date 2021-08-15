package com.dashboard.kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dashboard.kotlin.clashhelper.clashConfig
import com.dashboard.kotlin.clashhelper.clashStatus
import com.dashboard.kotlin.clashhelper.commandhelper
import com.dashboard.kotlin.databinding.FragmentMainPageBinding
import com.dashboard.kotlin.suihelper.suihelper
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File


class MainPage : Fragment() {
    private var _binding: FragmentMainPageBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainPageBinding.inflate(inflater, container, false)
        return binding.root
    }


    private val clashStatusClass = clashStatus()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ViewCreated", "MainPageViewCreated")

        binding.toolbar.toolbarIn.title = getString(R.string.app_name)
        //TODO 添加 app 图标

        if (!suihelper.checkPermission()) {


            binding.clashStatus.setCardBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.error, context?.theme)
            )
            binding.clashStatusIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_service_not_running,
                    context?.theme
                )
            )
            binding.clashStatusText.text = getString(R.string.sui_disable)
            binding.clashStatusText.visibility = View.GONE


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


            if (clashStatus().runStatus()) {
                binding.clashStatus.setCardBackgroundColor(
                    ResourcesCompat.getColor(resources, R.color.colorPrimary, context?.theme)
                )
                binding.clashStatusIcon.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_activited, context?.theme)
                )
                binding.clashStatusText.text =
                    getString(R.string.clash_enable).format(clashConfig.getClashType())

                binding.clashStatusText.visibility = View.VISIBLE


                clashStatusClass.getTraffic()

                GlobalScope.launch(Dispatchers.IO) {
                    while (clashStatusClass.trafficThreadFlag) {
                        try {
                            val jsonObject = JSONObject(clashStatusClass.trafficRawText)
                            val upText: String = commandhelper.autoUnit(jsonObject.optString("up"))
                            val downText: String =
                                commandhelper.autoUnit(jsonObject.optString("down"))

                            withContext(Dispatchers.Main) {
                                binding.netspeedStatusText.text =
                                    getString(R.string.netspeed_status_text).format(
                                        upText,
                                        downText
                                    )
                            }
                        } catch (ex: Exception) {
                            Log.w("trafficText", ex.toString())
                        }
                        delay(1000)
                    }
                }


            } else {
                binding.clashStatus.setCardBackgroundColor(
                    ResourcesCompat.getColor(resources, R.color.gray, context?.theme)
                )
                binding.clashStatusIcon.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_service_not_running,
                        context?.theme
                    )
                )
                binding.clashStatusText.text =
                    getString(R.string.clash_disable).format(clashConfig.getClashType())
                binding.netspeedStatusText.visibility = View.GONE

            }

        }

        binding.clashStatus.setOnClickListener {
            it.isClickable = false
            GlobalScope.async {

                doAssestsShellFile(
                    "${clashConfig.getClashType()}_" +
                            (if (clashStatus().runStatus()) {
                                "Stop"
                            } else {
                                "Start"
                            }) +
                            ".sh", !clashStatus().runStatus()
                )

                restartApp()
                true
            }
        }





        binding.menuIpCheck.setOnClickListener {

            val navController = it.findNavController()
            navController.navigate(R.id.action_mainPage_to_ipCheckPage)

        }


        binding.menuWebDashboard.setOnClickListener {


            val navController = it.findNavController()
            val bundle = Bundle()
            bundle.putString("URL", "http://127.0.0.1:9090/ui/")
            navController.navigate(R.id.action_mainPage_to_webViewPage, bundle)
        }


        binding.menuWebDashboardDownload.setOnClickListener {

            val bundle = Bundle()
            bundle.putString("TYPE", "DASHBOARD")
            val navController = it.findNavController()
            navController.navigate(R.id.action_mainPage_to_downloadPage, bundle)

        }

        binding.menuSubDownload.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("TYPE", "SUB")
            val navController = it.findNavController()
            navController.navigate(R.id.action_mainPage_to_downloadPage, bundle)
        }

        binding.menuMmdbDownload.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("TYPE", "MMDB")
            val navController = it.findNavController()
            navController.navigate(R.id.action_mainPage_to_downloadPage, bundle)
        }


        binding.menuVersionSwitch.setOnClickListener {
            val versionArray = arrayOf<CharSequence>("CFM", "CPFM")
            val diaLogObj: AlertDialog? = activity?.let { itD ->
                AlertDialog.Builder(itD).let { it ->
                    it.setItems(
                        versionArray
                    ) { _, which ->
                        KV.encode("ClashType", versionArray[which].toString())
                        restartApp()
                    }
                    it.create()
                }
            }
            diaLogObj?.show()
        }
    }


    override fun onDestroyView() {
        clashStatusClass.stopGetTraffic()
        _binding = null
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

    private suspend fun doAssestsShellFile(fileName: String, isStart: Boolean = false) {
        context?.assets?.open(fileName)?.let { op ->
            File(context?.externalCacheDir, fileName).let { fo ->
                fo.outputStream().let { ip ->
                    op.copyTo(ip)
                }

                withContext(Dispatchers.Main) {
                    binding.clashStatus.setCardBackgroundColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.colorPrimary,
                            context?.theme
                        )
                    )
                    binding.clashStatusIcon.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_refresh,
                            context?.theme
                        )
                    )
                    binding.clashStatusText.text =
                        getString(R.string.clash_charging).format(clashConfig.getClashType())
                    binding.netspeedStatusText.visibility = View.GONE
                }

                suihelper.suCmd("sh '${context?.externalCacheDir}/${fileName}'")

                fo.delete()
                delay(3000)
            }
        }
    }
}