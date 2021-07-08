package com.dashboard.kotlin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.numberprogressbar.NumberProgressBar
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
            "SUB" -> {
                val list = mutableListOf<DownloadPage.DownLoadDataClass>()
                val indexArray = KV.decodeBytes("SUB_INDEX") ?: byteArrayOf()
                for (index in indexArray) {
                    list.add(
                        DownloadPage.DownLoadDataClass(
                            KV.decodeString("SUB_${index}_TITLE") ?: "第${index}个订阅",
                            "$index",
                            KV.decodeString("SUB_${index}_URL") ?: "",
                            "SUB",
                            "config.yaml"
                        )
                    )
                }


                list.add(
                    DownloadPage.DownLoadDataClass(
                        "添加订阅",
                        "",
                        "",
                        "addSub",
                        ""
                    )
                )

//                if (KV.decodeString("ModuleType") == "CFM") {
//                    list.add(
//                        0, DownloadPage.DownLoadDataClass(
//                            "ClashForMagisk", "来自 CFM 自动订阅中的值", "", "", ""
//                        )
//                    )
//                }

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
                "addSub" -> {
                    holder.downloadButton.visibility = View.GONE
                    holder.description.visibility = View.GONE
                    holder.download_card_single.setOnClickListener {
                        showAddSubDialog()
                    }
                }

                else -> {
//                    holder.download_card_single.setOnLongClickListener {
//                        true
//                        TODO: 长按删除订阅
//                    }

                    when (downloadItem.type) {
                        "SUB" -> holder.description.visibility = View.INVISIBLE
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

        fun showAddSubDialog() {
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

                    val indexArray = KV.decodeBytes("SUB_INDEX") ?: byteArrayOf()
                    for (index in 0..127) {
                        if (index.toByte() in indexArray) {
                            if (index == 127) {
                                break
                            }
                            continue
                        }
                        KV.encode("SUB_INDEX", indexArray + byteArrayOf(index.toByte()))
                        KV.encode("SUB_${index}_TITLE", addSubDialogTitle.text.toString())
                        KV.encode("SUB_${index}_URL", addSubDialogURL.text.toString())

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
            downloadItem: DownloadPage.DownLoadDataClass,
            holder: DownloadPage.DownLoadPageViewHolder
        ) {
            downLoadThread = GlobalScope.launch(Dispatchers.IO) {
                try {
                    Log.d("NetWork", "DownLoadStart URL:${downloadItem.URL}")

                    withContext(Dispatchers.Main) {
                        //disable button
                        holder.downloadButton.isClickable = false

                        //init progressBar
                        holder.progressBar.progress = 0
                        holder.progressBar.visibility = View.VISIBLE
                    }

                    val downLoadConn = URL(downloadItem.URL).openConnection()

                    //set UA
                    if (downloadItem.type == "sub") {
                        downLoadConn.setRequestProperty("User-Agent", "ClashForMagisk/1.2.5")
                    } else {
                        downLoadConn.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36"
                        )
                    }

                    //getLength
                    val totalLength: Long = downLoadConn.contentLengthLong
                    if (totalLength == -1L) {
                        withContext(Dispatchers.Main) {
                            holder.description.visibility = View.VISIBLE
                            holder.description.text = "无法获取文件长度(-1)，进度条失效"
                        }
                    }

                    downLoadConn.getInputStream().let { input ->
                        var count: Long = 0
                        val file = File(
                            this@DownloadPage.context?.externalCacheDir,
                            "${downloadItem.title}"
                        )
                        if (file.exists()) {
                            file.delete()
                        }
                        FileOutputStream(file).use { output ->
                            while (true) {
                                val temp: Int = input.read()
                                if (temp != -1) {
                                    output.write(temp)
                                    count -= -1
                                    if (count % 1000 == 0L)
                                        async(Dispatchers.Main) {
                                            holder.progressBar.progress =
                                                (count * 100 / totalLength).toInt()
                                        }
                                } else {
                                    holder.progressBar.progress = 100
                                    break
                                }
                            }
                        }


                    }


                    // install

                    Log.d("Install", "StartInstall")
                    commandhelper.doInstall(
                        "${this@DownloadPage.context?.externalCacheDir}/${downloadItem.title}",
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
                        //disable button
                        holder.downloadButton.isClickable = true
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
        val progressBar: NumberProgressBar = itemView.findViewById(R.id.downloadProgressBar)
        val download_card_single: CardView = itemView.findViewById(R.id.download_card_single)
    }


    override fun onDestroyView() {
        Log.d("ViewDestroy", "DownLoadPageDestroyView")
        downLoadThread?.cancel()
        super.onDestroyView()
    }
}