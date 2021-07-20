package com.dashboard.kotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashboard.kotlin.clashhelper.commandhelper
import kotlinx.android.synthetic.main.fragment_download_page.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL


class getConfig {
    fun getConfig(type: String): List<DownloadPage.DownLoadDataClass> {
        when (type) {
            "MMDB", "SUB" -> {
                val list = mutableListOf<DownloadPage.DownLoadDataClass>()
                val indexArray = KV.decodeBytes("${type}_INDEX") ?: byteArrayOf()
                for (index in indexArray) {
                    list.add(
                        DownloadPage.DownLoadDataClass(
                            KV.decodeString("${type}_${index}_TITLE") ?: "第${index}个订阅",
                            "$index",
                            KV.decodeString("${type}_${index}_URL") ?: "",
                            type,
                            when (type) {
                                "MMDB" -> "Country.mmdb"
                                "SUB" -> "config.yaml"
                                else -> ""
                            }
                        )
                    )
                }


                list.add(
                    DownloadPage.DownLoadDataClass(
                        "添加链接",
                        "",
                        "",
                        "add${type}",
                        ""
                    )
                )

//                if (type == "SUB") {
//                    if (KV.decodeString("ModuleType") == "CFM") {
//                        list.add(
//                            0, DownloadPage.DownLoadDataClass(
//                                "ClashForMagisk", "来自 CFM 自动订阅中的值", "", type, "config.yaml"
//                            )
//                        )
//                    }
//                }

                if (type == "MMDB") {
                    list.add(
                        0, DownloadPage.DownLoadDataClass(
                            "GeoIP2 · CN",
                            "最小巧、最准确、最实用的 中国大陆 IP 段 + GeoIP2 数据库",
                            "https://github.com/Hackl0us/GeoIP2-CN/raw/release/Country.mmdb",
                            type,
                            "Country.mmdb"
                        )
                    )
                }

                return list


            }
            else -> return listOf(
                DownloadPage.DownLoadDataClass(
                    "clash-dashboard",
                    "Dreamacro 出品",
                    "https://github.com/Dreamacro/clash-dashboard/archive/refs/heads/gh-pages.zip",
                    "DASHBOARD",
                    "clash-dashboard-gh-pages"
                ),
                DownloadPage.DownLoadDataClass(
                    "YACD",
                    "Yet Another Clash Dashboard",
                    "https://github.com/haishanh/yacd/archive/gh-pages.zip",
                    "DASHBOARD",
                    "yacd-gh-pages"
                ),
//                DownloadPage.DownLoadDataClass("Test3", "Test3", "Test3", "zip", "/data/clash"),
            )
        }
    }

}

class DownloadPage : Fragment() {
    var originalDescription: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_download_page, container, false)
    }


    private var downLoadThread: Job? = null

    data class DownLoadDataClass(
        val title: String,
        val description: String,
        val URL: String,
        val type: String,
        val pathName: String = ""
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("ViewCreated", "DownLoadPageViewCreated")

        toolbar.navigationIcon = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_back,
            context?.theme
        )
        toolbar.setNavigationOnClickListener {
            val controller = it.findNavController()
            controller.popBackStack()
        }

        FlashView()

    }

    private fun FlashView() {
        val downloadType: String = arguments?.getString("TYPE").let {
            it ?: "DASHBOARD"
        }
        download_recycler_view.layoutManager = LinearLayoutManager(context)
        download_recycler_view.adapter = DownLoadPageAdapter(getConfig().getConfig(downloadType))
    }

    inner class DownLoadPageAdapter(private val downloadItemList: List<DownLoadDataClass>) :
        RecyclerView.Adapter<DownLoadPageViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownLoadPageViewHolder {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.download_single, parent, false)
            return DownLoadPageViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: DownLoadPageViewHolder, position: Int) {
            val downloadItem = downloadItemList[position]
            holder.title.text = downloadItem.title
            holder.description.text = downloadItem.description

            when (downloadItem.type) {
                "addMMDB", "addSUB" -> {
                    holder.downloadButton.visibility = View.GONE
                    holder.description.visibility = View.GONE
                    holder.download_card_single.setOnClickListener {
                        //delete "add"
                        showAddLinkDialog(downloadItem.type.replaceFirst("add",""))
                    }
                }

                else -> {
                    when (downloadItem.type) {
                        "MMDB", "SUB" -> {
                            holder.description.let {
                                it.visibility = it.text.toString().let { str ->
                                    try {
                                        str.toInt()
                                        holder.download_card_single.setOnLongClickListener {
                                            val diaLogView = LayoutInflater.from(this@DownloadPage.context)
                                                .inflate(R.layout.dialog_confirm, null, false)
                                            val confirmDialogTitle: TextView =
                                                diaLogView.findViewById(R.id.confirmDialog_title)
                                            val confirmDialogContext: TextView =
                                                diaLogView.findViewById(R.id.confirmDialog_text)
                                            val confirmDialogSureBtn: TextView =
                                                diaLogView.findViewById(R.id.confirmDialogBtnSure)
                                            val confirmDialogCancelBtn: TextView =
                                                diaLogView.findViewById(R.id.confirmDialogBtnCancel)

                                            val diaLogObj: AlertDialog? = activity?.let {
                                                AlertDialog.Builder(it).create()
                                            }
                                            diaLogObj?.show()
                                            diaLogObj?.window?.setContentView(diaLogView)
                                            diaLogObj?.window?.setBackgroundDrawableResource(android.R.color.transparent)
                                            diaLogObj?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

                                            confirmDialogTitle.text = "删除链接"
                                            confirmDialogContext.text = "警告⚠️\n此操作不可撤销"
                                            confirmDialogCancelBtn.setOnClickListener { diaLogObj?.dismiss() }
                                            confirmDialogSureBtn.setOnClickListener {
                                                try {
                                                    KV.encode(
                                                        "${downloadItem.type}_INDEX", (
                                                                (KV.decodeBytes("${downloadItem.type}_INDEX")
                                                                    ?: byteArrayOf())
                                                                    .toMutableSet() - (originalDescription
                                                                    ?: holder.description.text.toString())
                                                                    .toByte()
                                                                )
                                                            .toByteArray()
                                                    )
                                                } catch (ex: Exception) {
                                                }


                                                FlashView()

                                                diaLogObj?.dismiss()
                                            }

                                            true
                                        }

                                        View.GONE
                                    }catch (ex: Exception){
                                        View.VISIBLE
                                    }
                                }
                            }

                        }
                    }
                    holder.downloadButton.setOnClickListener {
                        doDownLoad(downloadItem, holder)
                    }
                }

            }
        }

        override fun getItemCount(): Int {
            return downloadItemList.size
        }

        private fun showAddLinkDialog(type: String) {
            val diaLogView = LayoutInflater.from(this@DownloadPage.context)
                .inflate(R.layout.dialog_add_sub, null, false)
            val addSubDialogTitle: EditText = diaLogView.findViewById(R.id.addSubDialogTitle)
            val addSubDialogURL: EditText = diaLogView.findViewById(R.id.addSubDialogURL)
            val addSubDialogSureBtn: TextView = diaLogView.findViewById(R.id.addSubDialogBtnSure)
            val addSubDialogCancelBtn: TextView =
                diaLogView.findViewById(R.id.addSubDialogBtnCancel)


            val diaLogObj: AlertDialog? = activity?.let {
                AlertDialog.Builder(it).create()
            }

            diaLogObj?.show()
            diaLogObj?.window?.setContentView(diaLogView)
            diaLogObj?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            diaLogObj?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

            addSubDialogCancelBtn.setOnClickListener { diaLogObj?.dismiss() }
            addSubDialogSureBtn.setOnClickListener {
                addSubDialogTitle.hint = "需要填写名称"
                addSubDialogTitle.setHintTextColor(resources.getColor(R.color.error))
                addSubDialogURL.hint = "需要填写链接"
                addSubDialogURL.setHintTextColor(resources.getColor(R.color.error))
                if (addSubDialogTitle.text.toString() != "" && addSubDialogURL.text.toString() != "") {

                    val indexArray = KV.decodeBytes("${type}_INDEX") ?: byteArrayOf()
                    for (index in 0..127) {
                        if (index.toByte() in indexArray) {
                            if (index == 127) {
                                break
                            }
                            continue
                        }
                        KV.encode("${type}_INDEX", indexArray + byteArrayOf(index.toByte()))
                        KV.encode("${type}_${index}_TITLE", addSubDialogTitle.text.toString())
                        KV.encode("${type}_${index}_URL", addSubDialogURL.text.toString())

                        diaLogObj?.dismiss()

                        //reflash view
                        FlashView()

                        return@setOnClickListener
                    }
                    Toast.makeText(this@DownloadPage.context, "订阅达到上限", Toast.LENGTH_LONG).show()
                }

            }


        }


        fun doDownLoad(
            downloadItem: DownLoadDataClass,
            holder: DownLoadPageViewHolder
        ) {
            downLoadThread = GlobalScope.launch(Dispatchers.IO) {
                try {
                    Log.d("NetWork", "DownLoadStart")

                    val randomName: String = (1..1000).random().toString()
                    originalDescription = holder.description.text.toString()
                    withContext(Dispatchers.Main) {
                        //disable button
                        holder.downloadButton.visibility = View.GONE

                        //init progressBar
                        holder.progressBar.visibility = View.VISIBLE

                        holder.description.visibility = View.VISIBLE
                        holder.description.text = "开始下载"

                    }

                    val downLoadConn = URL(downloadItem.URL).openConnection()

                    //set UA
                    if (downloadItem.type == "SUB") {
                        downLoadConn.setRequestProperty("User-Agent", "ClashForMagisk/1.2.5")
                    } else {
                        downLoadConn.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36"
                        )
                    }


                    downLoadConn.getInputStream().use { input ->
                        val file = File(
                            this@DownloadPage.context?.externalCacheDir,
                            randomName
                        )
                        if (file.exists()) {
                            file.delete()
                        }
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }


                    // install
                    Log.d("Install", "StartInstall")
                    commandhelper.doInstall(
                        "${this@DownloadPage.context?.externalCacheDir}/${randomName}",
                        downloadItem.type,
                        downloadItem.pathName
                    )
                    Log.d("Install", "ExitInstall")

                    withContext(Dispatchers.Main) {
                        holder.description.visibility = View.VISIBLE
                        holder.description.text = "下载完成"
                    }

                } catch (ex: Exception) {
                    withContext(Dispatchers.Main) {
                        holder.description.visibility = View.VISIBLE
                        holder.description.text = "下载发生错误"
                    }
                } finally {
                    Log.d("NetWork", "DownLoadEnd")

                    withContext(Dispatchers.Main) {
                        holder.downloadButton.visibility = View.VISIBLE
                        holder.progressBar.visibility = View.GONE
                    }
                }
            }
        }

    }


    class DownLoadPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.download_card_title)
        val description: TextView = itemView.findViewById(R.id.download_card_desc)
        val downloadButton: ImageView = itemView.findViewById(R.id.downloadButton)
        val progressBar: ProgressBar = itemView.findViewById(R.id.downloadProgressBar)
        val download_card_single: CardView = itemView.findViewById(R.id.download_card_single)
    }


    override fun onDestroyView() {
        Log.d("ViewDestroy", "DownLoadPageDestroyView")
        downLoadThread?.cancel()
        super.onDestroyView()
    }
}