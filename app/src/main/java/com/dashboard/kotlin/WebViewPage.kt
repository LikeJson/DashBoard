package com.dashboard.kotlin

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import com.dashboard.kotlin.databinding.FragmentWebviewPageBinding


class WebViewPage : Fragment() {
    private var _binding: FragmentWebviewPageBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWebviewPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("ViewCreated", "WebViewPageViewCreated")

        binding.toolbar.toolbarIn.navigationIcon = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_back,
            context?.theme
        )
        binding.toolbar.toolbarIn.setNavigationOnClickListener {
            val controller = it.findNavController()
            controller.popBackStack()
        }


        arguments?.getString("URL")?.let {
            binding.webView.settings.javaScriptEnabled = true
            binding.webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            binding.webView.settings.domStorageEnabled = true
            binding.webView.settings.databaseEnabled = true
            binding.webView.webViewClient = WebViewClient()
            binding.webView.loadUrl(it + run {
                if ((context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) == Configuration.UI_MODE_NIGHT_YES) {
                    "?theme=dark"
                }else{
                    "?theme=light"
                }
            })
        }
    }


    override fun onDestroyView() {
        _binding = null
        Log.d("Destroy", "WebViewPageDestroyView")
        super.onDestroyView()
    }
}