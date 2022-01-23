package com.dashboard.kotlin

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_webview_page.*
import kotlinx.android.synthetic.main.toolbar.*


class WebViewPage : Fragment() {
    var isDark = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_webview_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("ViewCreated", "WebViewPageViewCreated")

        toolbar.navigationIcon = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_back,
            context?.theme
        )
        toolbar.setNavigationOnClickListener {
            val controller = it.findNavController()
            controller.popBackStack()
        }


        arguments?.getString("URL")?.let {
            webView.settings.javaScriptEnabled = true
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            webView.settings.domStorageEnabled = true
            webView.settings.databaseEnabled = true
            webView.webViewClient = WebViewClient()
            webView.loadUrl(it)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        Log.e("www1", if ((context?.resources?.configuration?.uiMode
            ?.and(Configuration.UI_MODE_NIGHT_MASK)) == Configuration.UI_MODE_NIGHT_YES) "true" else "false" )
        if ((context?.resources?.configuration?.uiMode
                ?.and(Configuration.UI_MODE_NIGHT_MASK)) == Configuration.UI_MODE_NIGHT_YES) {
            webView.settings.forceDark = WebSettings.FORCE_DARK_ON
        }else{
            webView.settings.forceDark = WebSettings.FORCE_DARK_OFF
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Destroy", "WebViewPageDestroyView")

    }
}