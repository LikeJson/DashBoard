package com.dashboard.kotlin

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

            else -> return listOf(
                DownloadPage.DownLoadDataClass(
                    "clash-dashboard",
                    "Dreamacro 出品",
                    "https://github.com/Dreamacro/clash-dashboard/archive/refs/heads/gh-pages.zip",
                    "zip",
                    "clash-dashboard-gh-pages"
                ),
                DownloadPage.DownLoadDataClass(
                    "YACD",
                    "Yet Another Clash Dashboard",
                    "https://github.com/haishanh/yacd/archive/gh-pages.zip",
                    "zip",
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

        val downloadType: String = arguments?.getString("").let {
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

            holder.downloadButton.setOnClickListener {
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
                        //TODO: 处理 等于 -1 的情况

                        downLoadConn.getInputStream().let { input ->
                            var count: Long = 0
                            val file = File(
                                this@DownloadPage.context?.externalCacheDir,
                                "${downloadItem.title}.zip"
                            )
                            if (file.exists()) {
                                file.delete()
                            }
                            FileOutputStream(file).use { output ->
                                while (true) {
                                    val temp: Int = input.read()
                                    if (temp != -1) {
                                        output.write(temp)
                                        count -=-1
                                        if (count % 1000 == 0L)
                                        async(Dispatchers.Main) {
                                            holder.progressBar.progress =
                                                (count * 100 / totalLength).toInt()
                                        }
                                    } else {
                                        break
                                    }
                                }
                            }


                        }


                        // install

                        Log.d("Install", "StartInstall")
                        commandhelper.installZip(
                            "${this@DownloadPage.context?.externalCacheDir}/${downloadItem.title}.zip",
                            "DashBoard",
                            downloadItem.pathName
                        )
                        Log.d("Install", "ExitInstall")



                    } catch (ex: Exception) {

                    } finally {
                        Log.d("NetWork", "DownLoadEnd")

                        async(Dispatchers.Main) {
                            //disable button
                            holder.downloadButton.isClickable = true
                            holder.progressBar.visibility = View.GONE
                        }
                    }
                }


//
//                Toast.makeText(this@DownloadPage.context, downloadItem.URL, Toast.LENGTH_SHORT)
//                    .show()
            }
        }

        override fun getItemCount(): Int {
            return downloadItemList.size
        }

    }


    class DownLoadPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.download_card_title)
        val description: TextView = itemView.findViewById(R.id.download_card_desc)
        val downloadButton: ImageView = itemView.findViewById(R.id.downloadButton)
        val progressBar: NumberProgressBar = itemView.findViewById(R.id.downloadProgressBar)
    }


    override fun onDestroyView() {
        Log.d("ViewDestroy", "DownLoadPageDestroyView")
        downLoadThread?.cancel()
        super.onDestroyView()
    }
}